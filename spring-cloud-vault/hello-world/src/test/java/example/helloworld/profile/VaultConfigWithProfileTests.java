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
package example.helloworld.profile;

import example.VaultContainers;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Spring Cloud Vault Config can use profiles to retrieve properties from
 * different contexts.
 *
 * @author Mark Paluch
 */
@SpringBootTest(classes = VaultConfigWithProfileTests.Config.class)
@ActiveProfiles("cloud")
@Testcontainers
public class VaultConfigWithProfileTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("kv put secret/my-spring-boot-app mykey=myvalue hello.world='Hello, World'");
		it.withInitCommand("kv put secret/my-spring-boot-app/cloud key_for_cloud_profile=value mykey=cloud");
	});

	@SpringBootApplication
	@Configuration
	static class Config {
	}

	@Autowired
	Environment environment;

	@Test
	public void shouldHaveVaultProperties() {

		assertThat(environment.getProperty("mykey")).isEqualTo("cloud");
		assertThat(environment.getProperty("key_for_cloud_profile")).isEqualTo("value");
	}

}
