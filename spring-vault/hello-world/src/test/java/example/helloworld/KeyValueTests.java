/*
 * Copyright 2018 the original author or authors.
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
package example.helloworld;

import java.util.Collections;
import java.util.Map;

import example.TestSettings;
import example.VaultContainers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.vault.VaultException;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.VaultResponseSupport;
import org.springframework.vault.support.Versioned;
import org.springframework.vault.support.Versioned.Metadata;
import org.springframework.vault.support.Versioned.Version;

import static org.assertj.core.api.Assertions.*;

/**
 * Simple interaction with {@link VaultOperations}.
 *
 * @author Mark Paluch
 */
@ContextConfiguration
@ExtendWith(SpringExtension.class)
@Slf4j
@Testcontainers
public class KeyValueTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("secrets disable secret/");
		it.withInitCommand("secrets enable -path=secret -version=1 kv");
		it.withInitCommand("secrets enable -path=versioned -version=2 kv");
	});

	@Configuration
	static class Config extends VaultTestConfiguration {

		@Override
		public VaultEndpoint vaultEndpoint() {
			return TestSettings.endpoint(vaultContainer);
		}

	}

	@Autowired
	VaultOperations vaultOperations;

	/**
	 * Use the unversioned (key-value backend version 1) Key-Value backend.
	 */
	@Test
	public void shouldWriteAndReadUnversionedData() {

		VaultKeyValueOperations unversioned = vaultOperations.opsForKeyValue("secret",
				KeyValueBackend.KV_1);

		MySecretData mySecretData = new MySecretData();
		mySecretData.setSecurityQuestion("Say my name");
		mySecretData.setAnswer("Heisenberg");

		unversioned.put("myapplication/user/3128", mySecretData);
		log.info("Wrote data to Vault");

		VaultResponseSupport<MySecretData> response = unversioned
				.get("myapplication/user/3128", MySecretData.class);

		MySecretData data = response.getData();
		assertThat(data.getSecurityQuestion())
				.isEqualTo(mySecretData.getSecurityQuestion());
		assertThat(data.getAnswer()).isEqualTo(mySecretData.getAnswer());
	}

	/**
	 * Use the versioned (key-value backend version 2) Key-Value backend without
	 * interacting with version metadata.
	 */
	@Test
	public void shouldWriteAndReadVersionAgnosticData() {

		VaultKeyValueOperations versioned = vaultOperations.opsForKeyValue("versioned",
				KeyValueBackend.KV_2);

		MySecretData mySecretData = new MySecretData();
		mySecretData.setSecurityQuestion("Say my name");
		mySecretData.setAnswer("Heisenberg");

		versioned.put("myapplication/user/3128", mySecretData);
		log.info("Wrote data to Vault");

		VaultResponseSupport<MySecretData> response = versioned
				.get("myapplication/user/3128", MySecretData.class);

		MySecretData data = response.getData();
		assertThat(data.getSecurityQuestion())
				.isEqualTo(mySecretData.getSecurityQuestion());
		assertThat(data.getAnswer()).isEqualTo(mySecretData.getAnswer());
	}

	/**
	 * Use the versioned (key-value backend version 2) Key-Value backend without
	 * interacting with version metadata.
	 */
	@Test
	public void shouldWriteAndReadVersionedData() {

		VaultVersionedKeyValueOperations versioned = vaultOperations
				.opsForVersionedKeyValue("versioned");

		MySecretData mySecretData = new MySecretData();
		mySecretData.setSecurityQuestion("Say my name");
		mySecretData.setAnswer("Heisenberg");

		Metadata version1 = versioned.put("myapplication/user/3128", mySecretData);
		log.info(String.format("Wrote data to Vault, created version %s",
				version1.getVersion()));

		MySecretData update = new MySecretData();
		update.setSecurityQuestion("Huh?");
		update.setAnswer("Heisenberg");

		Metadata version2 = versioned.put("myapplication/user/3128", update);

		Versioned<MySecretData> v1 = versioned.get("myapplication/user/3128",
				version1.getVersion(), MySecretData.class);

		Versioned<MySecretData> v2 = versioned.get("myapplication/user/3128",
				version2.getVersion(), MySecretData.class);

		assertThat(v1.getRequiredData().getSecurityQuestion()).isEqualTo("Say my name");
		assertThat(v2.getRequiredData().getSecurityQuestion()).isEqualTo("Huh?");
	}

	/**
	 * Use the versioned (key-value backend version 2) Key-Value backend without
	 * interacting with version metadata.
	 */
	@Test
	public void shouldApplyCompareAndSwap() {

		VaultVersionedKeyValueOperations versioned = vaultOperations
				.opsForVersionedKeyValue("versioned");

		Map<String, String> secret = Collections.singletonMap("key", "value");

		Metadata version1 = versioned.put("my-secret", secret);
		log.info(String.format("Wrote data to Vault, created version %s",
				version1.getVersion()));

		// Compare and set succeeds
		versioned.put("my-secret",
				Versioned.create(Collections.singletonMap("key", "value"), version1));

		// Fails because invalid expected version
		ThrowingCallable callable = () -> versioned.put("my-secret", Versioned
				.create(Collections.singletonMap("key", "value"), Version.from(999)));

		assertThatThrownBy(callable).isInstanceOf(VaultException.class);
	}

	@Data
	static class MySecretData {

		String securityQuestion;

		String answer;

	}

}
