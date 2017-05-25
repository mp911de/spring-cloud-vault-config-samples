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
package example.cloudfoundry;

import io.pivotal.spring.cloud.vault.config.java.VaultConnectorsConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.config.java.ServiceScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultOperations;

/**
 * Sample application using local config properties for testing Vault without VCAP env
 * variables. Requires {@code spring.cloud.appId} and a number of services to be
 * configured.
 * <p>
 * This sample uses {@link ServiceScan} to create beans for all discovered services so
 * {@link VaultOperations} can be injected without further service creation.
 *
 * @author Mark Paluch
 */
@Slf4j
public class LocalConfigCloudApplication {

	public static void main(String[] args) {

		System.setProperty("spring.cloud.appId", "my-cloud-app");
		System.setProperty("spring.cloud.my-vault-service",
				"https://localhost:8200?token=my-token");

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				MyConfig.class, ClientComponent.class);

		context.start();

		ClientComponent component = context.getBean(ClientComponent.class);

		log.info("VaultOperations (hash): {}", component.getVaultOperations());

		context.stop();
	}

	@Configuration
	@ServiceScan
	static class MyConfig extends VaultConnectorsConfig {

	}

	@Component
	static class ClientComponent {

		private final VaultOperations vaultOperations;

		ClientComponent(VaultOperations vaultOperations) {
			this.vaultOperations = vaultOperations;
		}

		public VaultOperations getVaultOperations() {
			return vaultOperations;
		}
	}
}
