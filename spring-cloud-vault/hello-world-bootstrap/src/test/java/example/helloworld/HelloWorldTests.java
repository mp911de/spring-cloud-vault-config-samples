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

import example.VaultContainers;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 * Spring Cloud Vault Config is active for Spring Boot applications and within tests.
 *
 * @author Mark Paluch
 */
@SpringBootTest
@Testcontainers
public class HelloWorldTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("kv put secret/my-spring-boot-app mykey=myvalue hello.world='Hello, World'");
		it.withInitCommand("kv put secret/my-spring-boot-app/cloud key_for_cloud_profile=value mykey=cloud");
	});

	@Autowired
	Environment environment;

	@Test
	public void shouldHaveVaultProperties() {

		assertThat(environment.containsProperty("mykey")).isTrue();
		assertThat(environment.getProperty("mykey")).isEqualTo("myvalue");

		assertThat(environment.containsProperty("key_for_cloud_profile")).isFalse();
	}
}
