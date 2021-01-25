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
package example.pki;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.vault.config.VaultSecretBackendDescriptor;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.vault.config.VaultSecretBackendDescriptor;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Vault using the PKI integration.
 *
 * @author Mark Paluch
 */
@ConfigurationProperties("pki")
@Data
@Validated
public class VaultPkiProperties implements VaultSecretBackendDescriptor {

	/**
	 * Enable pki backend usage.
	 */
	private boolean enabled = true;

	/**
	 * Role name for credentials.
	 */
	private String role;

	/**
	 * pki backend path.
	 */
	private String backend = "pki";

	/**
	 * The CN of the certificate. Should match the host name.
	 */
	private String commonName;

	/**
	 * Alternate CN names for additional host names.
	 */
	private List<String> altNames;

	/**
	 * Prevent certificate re-creation by storing the Valid certificate inside Vault.
	 */
	private boolean reuseValidCertificate = true;

	/**
	 * Startup/Locking timeout. Used to synchronize startup and to prevent multiple SSL
	 * certificate requests.
	 */
	private int startupLockTimeout = 10000;
}
