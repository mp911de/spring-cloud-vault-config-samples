/*
 * Copyright 2016-2018 the original author or authors.
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

import example.TestSettings;
import example.VaultContainers;
import example.helloworld.HelloWorldTests.Config;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

import static org.assertj.core.api.Assertions.*;

/**
 * Simple interaction with {@link VaultOperations}.
 *
 * @author Mark Paluch
 */
@ContextConfiguration(classes = Config.class)
@ExtendWith(SpringExtension.class)
@Slf4j
@Testcontainers
public class HelloWorldTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("secrets disable secret/");
		it.withInitCommand("secrets enable -path=secret -version=1 kv");
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

	@Test
	public void shouldWriteAndReadData() throws Exception {

		MySecretData mySecretData = new MySecretData();
		mySecretData.setSecurityQuestion("Say my name");
		mySecretData.setAnswer("Heisenberg");

		vaultOperations.write("secret/myapplication/user/3128", mySecretData);
		log.info("Wrote data to Vault");

		VaultResponseSupport<MySecretData> response = vaultOperations.read(
				"secret/myapplication/user/3128", MySecretData.class);

		MySecretData data = response.getData();
		assertThat(data.getSecurityQuestion()).isEqualTo(
				mySecretData.getSecurityQuestion());
		assertThat(data.getAnswer()).isEqualTo(mySecretData.getAnswer());
	}

	@Data
	static class MySecretData {

		String securityQuestion;

		String answer;

	}

}
