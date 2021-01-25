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
package example.encryption;

import java.nio.charset.StandardCharsets;

import example.helloworld.VaultTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultTransitContext;

import static org.assertj.core.api.Java6Assertions.*;

import example.helloworld.VaultTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultTransitContext;

/**
 * Integration test using {@link VaultTransitOperations}.
 *
 * @author Mark Paluch
 */
@ContextConfiguration
@Slf4j
public class VaultTransitOperationsIntegrationTest {

	@Configuration
	static class Config extends VaultTestConfiguration {
	}

	@Autowired
	VaultOperations vaultOperations;

	VaultTransitOperations transitOperations;

	@BeforeEach
	public void before() {
		transitOperations = vaultOperations.opsForTransit();
	}

	/**
	 * Encrypt and decrypt String data with default encoding.
	 */
	@Test
	public void shouldEncryptAndDecryptStringData() {

		String ciphertext = transitOperations.encrypt("foo-key",
				"This is my secret string. Better not use umlauts here. "
						+ "This string is converted to bytes using String.getBytes() so "
						+ "make sure to set -Dfile.encoding=... properly.");

		log.info("Encrypted: {}" + ciphertext);

		String plaintext = transitOperations.decrypt("foo-key", ciphertext);

		assertThat(plaintext).contains("This is my secret string");
	}

	/**
	 * Encrypt and decrypt binary data with default encoding.
	 */
	@Test
	public void shouldEncryptAndDecryptBinaryData() {

		byte[] input = "That's just some random data. Move on, nothing to see here."
				.getBytes(StandardCharsets.UTF_8);

		String ciphertext = transitOperations.encrypt("foo-key", input,
				VaultTransitContext.builder().build());

		log.info("Encrypted: {}" + ciphertext);

		byte[] plaintext = transitOperations.decrypt("foo-key", ciphertext,
				VaultTransitContext.builder().build());

		assertThat(plaintext).hasSize(input.length).isEqualTo(plaintext);
	}
}
