/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.pki;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.CertificateBundle;

/**
 * {@link Configuration} to request SSL certificates and register a
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} to configure SSL
 * certificates in the
 * {@link org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory}
 *
 * @author Mark Paluch
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "server.ssl", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(VaultPkiProperties.class)
public class VaultPkiConfiguration {

	/**
	 * Obtain SSL certificate (cached/request new certificate) with startup locking.
	 *
	 * @param vaultProperties
	 * @param vaultOperations
	 * @param pkiProperties
	 * @param serverProperties
	 * @param synchronizationProvider
	 * @return
	 * @throws Exception
	 */
	@Bean
	@ConditionalOnProperty(prefix = "server.ssl", name = "enabled", havingValue = "true")
	public static SslCertificateEmbeddedServletContainerCustomizer sslCertificateRequestingPostProcessor(
			VaultProperties vaultProperties, VaultOperations vaultOperations,
			VaultPkiProperties pkiProperties, ServerProperties serverProperties,
			SynchronizationProvider synchronizationProvider) throws Exception {

		Lock lock = synchronizationProvider.getLock();

		CertificateBundle certificateBundle = CertificateUtil
				.findValidCertificate(vaultProperties, vaultOperations, pkiProperties);

		if (certificateBundle != null) {
			return createCustomizer(serverProperties, certificateBundle);
		}

		boolean locked = lock.tryLock(pkiProperties.getStartupLockTimeout(),
				TimeUnit.MILLISECONDS);

		if (!locked) {
			throw new IllegalStateException(String.format(
					"Could not obtain SSL synchronization lock within %d %s",
					pkiProperties.getStartupLockTimeout(), TimeUnit.MILLISECONDS));
		}

		try {
			certificateBundle = CertificateUtil.getOrRequestCertificate(vaultProperties,
					vaultOperations, pkiProperties);

			return createCustomizer(serverProperties, certificateBundle);
		}
		finally {
			lock.unlock();
		}

	}

	private static SslCertificateEmbeddedServletContainerCustomizer createCustomizer(
			ServerProperties serverProperties, CertificateBundle certificateBundle) {

		Ssl ssl = serverProperties.getSsl();

		if (ssl != null) {
			ssl.setKeyAlias("vault");
			ssl.setKeyPassword("");
			ssl.setKeyStorePassword("");
		}

		return new SslCertificateEmbeddedServletContainerCustomizer(certificateBundle);
	}

	/**
	 * Trivial local implementation suitable for our sample. The underlying {@link Lock}
	 * should synchronize on a shared resource like Cluster Leader Election, a Redis Lock,
	 * Zookeeper Lock, ...
	 *
	 * @return
	 */
	@Bean
	public static SynchronizationProvider synchronizationProvider() {

		final Lock lock = new ReentrantLock();

		return new SynchronizationProvider() {
			@Override
			public Lock getLock() {
				return lock;
			}
		};
	}

	/**
	 * {@link WebServerFactoryCustomizer} to configure SSL certificate use from
	 * {@link CertificateBundle}.
	 */
	private static class SslCertificateEmbeddedServletContainerCustomizer
			implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

		private final CertificateBundle certificateBundle;

		SslCertificateEmbeddedServletContainerCustomizer(
				CertificateBundle certificateBundle) {
			this.certificateBundle = certificateBundle;
		}

		@Override
		public void customize(ConfigurableServletWebServerFactory container) {

			try {

				final KeyStore keyStore = certificateBundle.createKeyStore("vault");
				final KeyStore trustStore = KeyStore
						.getInstance(KeyStore.getDefaultType());

				trustStore.load(null, null);
				trustStore.setCertificateEntry("ca",
						certificateBundle.getX509IssuerCertificate());

				trustStore.setCertificateEntry("cert",
						certificateBundle.getX509Certificate());

				container.setSslStoreProvider(new SslStoreProvider() {
					@Override
					public KeyStore getKeyStore() throws Exception {
						return keyStore;
					}

					@Override
					public KeyStore getTrustStore() throws Exception {
						return trustStore;
					}
				});

			}
			catch (IOException | GeneralSecurityException e) {
				throw new IllegalStateException(
						"Cannot configure Vault SSL certificate in ConfigurableEmbeddedServletContainer",
						e);
			}
		}
	}

	/**
	 * Strategy interface to provide a synchronization lock. If multiple services start up
	 * at the same time, they might request multiple certificates at the same time
	 * concerning the same subject. <br>
	 * To avoid races and enforce one certificate, {@link SynchronizationProvider} exposes
	 * a {@link Lock} to synchronize startup.
	 */
	interface SynchronizationProvider {

		/**
		 * Returns the {@link Lock}. Implementors should perform a lock on an external
		 * resources that is accessible from all service instances to prevent races. The
		 * lock should support {@link Lock#tryLock(long, TimeUnit)} and
		 * {@link Lock#unlock()} to acquire a Lock with timeout and unlock the resource.
		 * <p>
		 * A lock could use Cluster Leader Election, Redis Lock with TTL or any other
		 * resource that is suitable for mutually exclusive locking.
		 *
		 * @return the {@link Lock}.
		 */
		Lock getLock();
	}
}
