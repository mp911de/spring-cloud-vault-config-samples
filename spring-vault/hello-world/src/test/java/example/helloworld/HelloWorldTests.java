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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

/**
 * Simple interaction with {@link VaultOperations}.
 *
 * @author Mark Paluch
 */
@ContextConfiguration(classes = VaultTestConfiguration.class)
@Slf4j
public class HelloWorldTests {

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
