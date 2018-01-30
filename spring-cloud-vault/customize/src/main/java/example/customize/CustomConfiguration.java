/*
 * Copyright 2017-2018 the original author or authors.
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
package example.customize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.vault.config.SecretBackendConfigurer;
import org.springframework.cloud.vault.config.VaultConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.core.lease.event.LeaseListener;
import org.springframework.vault.core.lease.event.SecretLeaseEvent;

/**
 * Example configuration for Spring Cloud Vault customization. This class applies two
 * customizations:
 *
 * <ul>
 * <li>Low-level customization using {@link SecretLeaseContainer}</li>
 * <li>High-level customization of {@link org.springframework.core.env.PropertySource
 * property sources} using {@link VaultConfigurer}</li>
 * </ul>
 *
 * @author Mark Paluch
 */
@Slf4j
@RequiredArgsConstructor
public class CustomConfiguration implements InitializingBean {

	private final SecretLeaseContainer leaseContainer;

	// Low-level, SecretLeaseContainer-based customization
	@Override
	public void afterPropertiesSet() throws Exception {

		final RequestedSecret secret = RequestedSecret.renewable("mysql/creds/readonly");

		leaseContainer.addLeaseListener(new LeaseListener() {
			@Override
			public void onLeaseEvent(SecretLeaseEvent secretLeaseEvent) {

				if (secretLeaseEvent.getSource() == secret) {
					log.info("Secret event for " + secret + ": " + secretLeaseEvent);
				}
			}
		});

		leaseContainer.addRequestedSecret(secret);
	}

	// Customization of property sources.
	@Bean
	public VaultConfigurer configurer() {

		return new VaultConfigurer() {
			@Override
			public void addSecretBackends(
					SecretBackendConfigurer secretBackendConfigurer) {

				secretBackendConfigurer
						.add("cf/1a558498-59ad-488c-b395-8b983aacb7da/secret/my-cf-app");
			}
		};
	}
}
