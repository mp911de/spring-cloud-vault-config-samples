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

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.vault.config.VaultBootstrapper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.vault.config.VaultBootstrapper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Integration test verifying customization.
 *
 * @author Mark Paluch
 */
public class CustomizedTests {

	@Test
	public void shouldHaveVaultProperties() {

		SpringApplication application = new SpringApplication(
				CustomizedApplication.class);
		application.addBootstrapper(
				VaultBootstrapper.fromConfigurer(secretBackendConfigurer -> {
					secretBackendConfigurer.add(
							"cf/1a558498-59ad-488c-b395-8b983aacb7da/secret/my-cf-app");
				}));

		ConfigurableApplicationContext context = application.run();

		Environment environment = context.getEnvironment();

		assertThat(environment.containsProperty("org-key")).isTrue();
		assertThat(environment.getProperty("org-key")).isEqualTo("hello-world-org");
	}
}
