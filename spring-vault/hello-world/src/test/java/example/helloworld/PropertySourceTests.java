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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests showing {@code @VaultPropertySource} usage.
 *
 * @author Mark Paluch
 */
@ContextConfiguration
@ExtendWith(SpringExtension.class)
@Slf4j
@Testcontainers
public class PropertySourceTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("kv put secret/myapp/configuration configuration.key=value");
	});

	@Configuration
	@VaultPropertySource("secret/myapp/configuration")
	static class Config extends VaultTestConfiguration {

		@Override
		public VaultEndpoint vaultEndpoint() {
			return TestSettings.endpoint(vaultContainer);
		}

	}

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	VaultTemplate vaultTemplate;

	/**
	 * {@code @VaultPropertySource("secret/myapp/configuration")} will register a
	 * property source and expose its properties through {@link Environment}.
	 */
	@Test
	public void environmentShouldExposeVaultPropertySource() {

		Environment env = applicationContext.getEnvironment();

		assertThat(env.getProperty("configuration.key")).isEqualTo("value");
	}

	/**
	 * {@code @VaultPropertySource("secret/myapp/configuration")} will register a
	 * property source and expose its properties through {@link Environment}.
	 */
	@Test
	public void vaultPropertySourceShouldContainProperties() {

		org.springframework.vault.core.env.VaultPropertySource propertySource = new org.springframework.vault.core.env.VaultPropertySource(
				vaultTemplate, "secret/myapp/configuration");

		assertThat(propertySource.getProperty("configuration.key")).isEqualTo("value");
	}


}
