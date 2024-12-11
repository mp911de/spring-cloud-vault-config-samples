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

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.vault.config.VaultBootstrapper;

/**
 * Sample Application that customizes Spring Cloud Vault behavior.
 *
 * @author Mark Paluch
 */
@SpringBootApplication
public class CustomizedApplication {

	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(
				CustomizedApplication.class);
		application.addBootstrapRegistryInitializer(
				VaultBootstrapper.fromConfigurer(secretBackendConfigurer -> {
					secretBackendConfigurer.add(
							"cf/1a558498-59ad-488c-b395-8b983aacb7da/secret/my-cf-app");
				}));

		application.run(args);
	}

	@Value("${my-key}")
	String orgKey;

	@PostConstruct
	private void postConstruct() {
		System.out.println("##########################");
		System.out.println(orgKey);
		System.out.println("##########################");
	}
}
