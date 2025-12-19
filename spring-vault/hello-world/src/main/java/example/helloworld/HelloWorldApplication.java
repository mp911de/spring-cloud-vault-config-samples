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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.org.bouncycastle.util.test.Test;
import org.testcontainers.vault.VaultContainer;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

/**
 * Sample Application using Spring Vault with Token authentication.
 *
 * @author Mark Paluch
 */
@Slf4j
public class HelloWorldApplication {

	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("secrets disable secret/");
		it.withInitCommand("secrets enable -path=secret -version=1 kv");
	});

	public static void main(String[] args) {

		vaultContainer.start();

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				VaultConfiguration.class);

		context.start();

		VaultTemplate vaultTemplate = context.getBean(VaultTemplate.class);

		MySecretData mySecretData = new MySecretData();
		mySecretData.setUsername("walter");
		mySecretData.setPassword("white");

		vaultTemplate.write("secret/myapplication/user/3128", mySecretData);
		log.info("Wrote data to Vault");

		VaultResponseSupport<MySecretData> response = vaultTemplate
				.read("secret/myapplication/user/3128", MySecretData.class);

		log.info("Retrieved data {} from Vault", response.getData().getUsername());

		context.stop();
	}

	@Configuration
	static class VaultConfiguration extends AbstractVaultConfiguration {

		@Override
		public VaultEndpoint vaultEndpoint() {
			return TestSettings.endpoint(vaultContainer);
		}

		@Override
		public ClientAuthentication clientAuthentication() {
			return TestSettings.authentication();
		}

	}

	@Data
	static class MySecretData {

		String username;

		String password;

	}

}
