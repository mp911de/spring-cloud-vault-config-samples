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

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.CertificateBundle;
import org.springframework.vault.support.VaultCertificateRequest;
import org.springframework.vault.support.VaultCertificateResponse;
import org.springframework.vault.support.VaultHealth;
import org.springframework.vault.support.VaultResponseSupport;

/**
 * Utility class to store and retrieve Certificates from Vault.
 *
 * @author Mark Paluch
 */
@UtilityClass
class CertificateUtil {

	private final static Logger logger = LoggerFactory.getLogger(CertificateUtil.class);

	// Refresh period in seconds before certificate expires.
	private final static long REFRESH_PERIOD_BEFORE_EXPIRY = 60;

	/**
	 * Request SSL Certificate from Vault or retrieve cached certificate.
	 * <p>
	 * If {@link VaultPkiProperties#isReuseValidCertificate()} is enabled this method
	 * attempts to read a cached Certificate from Vault at {@code secret/$
	 * spring.application.name}/cert/${spring.cloud.vault.pki.commonName}}. Valid
	 * certificates will be reused until they expire. A new certificate is requested and
	 * cached if no valid certificate is found.
	 *
	 * @param vaultProperties
	 * @param vaultOperations
	 * @param pkiProperties
	 * @return the {@link CertificateBundle}.
	 */
	public static CertificateBundle getOrRequestCertificate(
			VaultProperties vaultProperties, VaultOperations vaultOperations,
			VaultPkiProperties pkiProperties) {

		CertificateBundle validCertificate = findValidCertificate(vaultProperties,
				vaultOperations, pkiProperties);

		if (!pkiProperties.isReuseValidCertificate()) {
			return validCertificate;
		}

		String cacheKey = createCacheKey(vaultProperties, pkiProperties);
		vaultOperations.delete(cacheKey);

		VaultCertificateResponse certificateResponse = requestCertificate(
				vaultOperations, pkiProperties);

		VaultHealth health = vaultOperations.opsForSys().health();
		storeCertificate(cacheKey, vaultOperations, health, certificateResponse);

		return certificateResponse.getData();
	}

	/**
	 * Find a valid, possibly cached, {@link CertificateBundle}.
	 *
	 * @param vaultProperties
	 * @param vaultOperations
	 * @param pkiProperties
	 * @return the {@link CertificateBundle} or {@literal null}.
	 */
	public static CertificateBundle findValidCertificate(VaultProperties vaultProperties,
			VaultOperations vaultOperations, VaultPkiProperties pkiProperties) {

		if (!pkiProperties.isReuseValidCertificate()) {
			return requestCertificate(vaultOperations, pkiProperties).getData();
		}

		String cacheKey = createCacheKey(vaultProperties, pkiProperties);

		VaultResponseSupport<CachedCertificateBundle> readResponse = vaultOperations
				.read(cacheKey, CachedCertificateBundle.class);

		VaultHealth health = vaultOperations.opsForSys().health();
		if (isValid(health, readResponse)) {

			logger.info("Found valid SSL certificate in Vault for: {}",
					pkiProperties.getCommonName());

			return getCertificateBundle(readResponse);
		}

		return null;
	}

	private static void storeCertificate(String cacheKey,
			VaultOperations vaultOperations, VaultHealth health,
			VaultCertificateResponse certificateResponse) {

		CertificateBundle certificateBundle = certificateResponse.getData();
		long expires = (health.getServerTimeUtc() + certificateResponse
				.getLeaseDuration()) - REFRESH_PERIOD_BEFORE_EXPIRY;

		CachedCertificateBundle cachedCertificateBundle = new CachedCertificateBundle();

		cachedCertificateBundle.setExpires(expires);
		cachedCertificateBundle.setTimeRequested(health.getServerTimeUtc());
		cachedCertificateBundle.setPrivateKey(certificateBundle.getPrivateKey());
		cachedCertificateBundle.setCertificate(certificateBundle.getCertificate());
		cachedCertificateBundle.setIssuingCaCertificate(certificateBundle
				.getIssuingCaCertificate());
		cachedCertificateBundle.setSerialNumber(certificateBundle.getSerialNumber());

		vaultOperations.write(cacheKey, cachedCertificateBundle);
	}

	private static String createCacheKey(VaultProperties vaultProperties,
			VaultPkiProperties pkiProperties) {

		return String.format("secret/%s/cert/%s", vaultProperties.getApplicationName(),
				pkiProperties.getCommonName());
	}

	private static CertificateBundle getCertificateBundle(
			VaultResponseSupport<CachedCertificateBundle> readResponse) {

		CachedCertificateBundle cachedCertificateBundle = readResponse.getData();

		return CertificateBundle.of(cachedCertificateBundle.getSerialNumber(),
				cachedCertificateBundle.getCertificate(),
				cachedCertificateBundle.getIssuingCaCertificate(),
				cachedCertificateBundle.getPrivateKey());
	}

	private static boolean isValid(VaultHealth health,
			VaultResponseSupport<CachedCertificateBundle> readResponse) {

		if (readResponse != null) {

			CachedCertificateBundle cachedCertificateBundle = readResponse.getData();
			if (health.getServerTimeUtc() < cachedCertificateBundle.getExpires()) {
				return true;
			}
		}

		return false;
	}

	private static VaultCertificateResponse requestCertificate(
			VaultOperations vaultOperations, VaultPkiProperties pkiProperties) {

		logger.info("Requesting SSL certificate from Vault for: {}",
				pkiProperties.getCommonName());

		VaultCertificateRequest certificateRequest = VaultCertificateRequest
				.builder()
				.commonName(pkiProperties.getCommonName())
				.altNames(
						pkiProperties.getAltNames() != null ? pkiProperties.getAltNames()
								: Collections.<String> emptyList()).build();

		VaultCertificateResponse certificateResponse = vaultOperations.opsForPki(
				pkiProperties.getBackend()).issueCertificate(pkiProperties.getRole(),
				certificateRequest);

		return certificateResponse;
	}

	@Data
	static class CachedCertificateBundle {

		private String certificate;

		@JsonProperty("serial_number")
		private String serialNumber;

		@JsonProperty("issuing_ca")
		private String issuingCaCertificate;

		@JsonProperty("private_key")
		private String privateKey;

		@JsonProperty("time_requested")
		private long timeRequested;

		@JsonProperty("expires")
		private long expires;
	}
}
