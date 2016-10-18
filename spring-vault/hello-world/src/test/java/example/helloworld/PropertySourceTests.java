/*
 * Copyright 2016 the original author or authors.
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

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests showing {@code @VaultPropertySource} usage.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@Slf4j
public class PropertySourceTests {

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	VaultTemplate vaultTemplate;

	/**
	 * Write some data to Vault before Vault can be used as
	 * {@link org.springframework.vault.annotation.VaultPropertySource}.
	 */
	@BeforeClass
	public static void beforeClass() {

		VaultOperations vaultOperations = new VaultTestConfiguration().vaultTemplate();
		vaultOperations.write("secret/myapp/configuration",
				Collections.singletonMap("configuration.key", "value"));
	}

	/**
	 * {@code @VaultPropertySource("secret/myapp/configuration")} will register a property
	 * source and expose its properties through {@link Environment}.
	 */
	@Test
	public void environmentShouldExposeVaultPropertySource() {

		Environment env = applicationContext.getEnvironment();

		assertThat(env.getProperty("configuration.key")).isEqualTo("value");
	}

	/**
	 * {@code @VaultPropertySource("secret/myapp/configuration")} will register a property
	 * source and expose its properties through {@link Environment}.
	 */
	@Test
	public void vaultPropertySourceShouldContainProperties() {

		org.springframework.vault.core.env.VaultPropertySource propertySource = new org.springframework.vault.core.env.VaultPropertySource(
				vaultTemplate, "secret/myapp/configuration");

		assertThat(propertySource.getProperty("configuration.key")).isEqualTo("value");
	}

	/**
	 * Java Configuration to bootstrap Spring Vault.
	 */
	@Configuration
	@VaultPropertySource("secret/myapp/configuration")
	static class VaultPropertySourceConfiguration extends VaultTestConfiguration {
	}
}
