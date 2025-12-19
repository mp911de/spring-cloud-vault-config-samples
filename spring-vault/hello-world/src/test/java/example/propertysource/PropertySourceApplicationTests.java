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
package example.propertysource;

import example.TestSettings;
import example.VaultContainers;
import example.helloworld.VaultTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.client.VaultEndpoint;

import static org.assertj.core.api.Java6Assertions.*;

/**
 * Integration test using {@link VaultPropertySource}.
 *
 * @author Mark Paluch
 */
@ContextConfiguration
@ExtendWith(SpringExtension.class)
@Slf4j
@Testcontainers
public class PropertySourceApplicationTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("kv put secret/my-spring-app database.username=myuser database.password=mypassword");
	});

	@ComponentScan
	@VaultPropertySource("secret/my-spring-app")
	static class Config extends VaultTestConfiguration {

		@Override
		public VaultEndpoint vaultEndpoint() {
			return TestSettings.endpoint(vaultContainer);
		}

	}

	@Autowired
	Environment environment;

	@Autowired
	DatabaseConfiguration databaseConfiguration;

	/**
	 * Retrieve properties stored in Vault through Spring's {@link Environment}.
	 */
	@Test
	public void shouldProvideConfigurationThroughEnvironment() {

		assertThat(environment.containsProperty("database.username")).isTrue();
		assertThat(environment.containsProperty("database.password")).isTrue();

		assertThat(environment.getProperty("database.username")).isEqualTo("myuser");
		assertThat(environment.getProperty("database.password")).isEqualTo("mypassword");
	}

	/**
	 * Use {@link Value} injection of properties stored in Vault with Spring Beans.
	 */
	@Test
	public void shouldProvideConfigurationThroughBean() {

		assertThat(databaseConfiguration.getUsername()).isEqualTo("myuser");
		assertThat(databaseConfiguration.getPassword()).isEqualTo("mypassword");
	}

	@Component
	static class DatabaseConfiguration {

		@Value("${database.username}")
		String username;

		@Value("${database.password}")
		String password;

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

	}

}
