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
package example.helloworld;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Cloud Vault Config configured with Spring Cloud Connectors localconfig.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloWorldOrderingTests {

	static {

		System.setProperty("spring.cloud.vault.generic.backends", "organization,generic");
		System.setProperty("spring.cloud.appId", "my-cf-app");
		System.setProperty("spring.cloud.my-vault-service",
				"https://localhost:8200/?token=00000000-0000-0000-0000-000000000000"
						+ "&backend.generic=cf/20fffe9d-d8d1-4825-9977-1426840a13db/secret"
						+ "&shared_backend.space=cf/d007583f-5617-4b02-a5a7-550648827cfa/secret"
						+ "&shared_backend.organization=cf/1a558498-59ad-488c-b395-8b983aacb7da/secret");
	}

	@Autowired
	Environment environment;

	@Autowired
	ApplicationContext applicationContext;

	@AfterClass
	public static void afterClass() {

		System.getProperties().remove("spring.cloud.vault.generic.backends");
		System.getProperties().remove("spring.cloud.appId");
		System.getProperties().remove("spring.cloud.my-vault-service");
	}

	@Test
	public void environmentShouldContainProperties() {

		assertThat(environment.getProperty("app-key")).isEqualTo("hello-world-app");
		assertThat(environment.getProperty("space-key")).isNull();
		assertThat(environment.getProperty("org-key")).isEqualTo("hello-world-org");
		assertThat(environment.getProperty("index")).isEqualTo("3");
	}
}
