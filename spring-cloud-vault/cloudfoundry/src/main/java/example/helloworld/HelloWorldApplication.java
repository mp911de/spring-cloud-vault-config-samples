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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

/**
 * Sample Application using Spring Cloud Vault on CloudFoundry
 *
 * @author Mark Paluch
 */
@SpringBootApplication
public class HelloWorldApplication {

	public static void main(String[] args) {

		// Compatibility code for CloudFoundry: If we're on CloudFoundry, then we should
		// not be required to provide a key store.
		if (StringUtils.isEmpty(System.getenv("VCAP_SERVICES"))) {
			System.setProperty("spring.cloud.vault.ssl.trust-store",
					"file:../../work/keystore.jks");
		}

		SpringApplication.run(HelloWorldApplication.class, args);
	}

	@Value("${app-key:}")
	String appKey;

	@Value("${space-key:}")
	String spaceKey;

	@Value("${org-key:}")
	String orgKey;

	@PostConstruct
	private void postConstruct() {

		System.out.println("##########################");
		System.out.println("App-key: " + appKey);
		System.out.println("Space-key: " + spaceKey);
		System.out.println("Org-key: " + orgKey);
		System.out.println("##########################");
	}
}
