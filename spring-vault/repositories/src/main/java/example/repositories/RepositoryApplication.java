/*
 * Copyright 2018 the original author or authors.
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
package example.repositories;

import java.io.File;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.repository.configuration.EnableVaultRepositories;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.VaultResponse;

import static example.util.WorkDirHelper.*;

/**
 * Sample Application using Spring Vault repositories.
 *
 * @author Mark Paluch
 */
@Slf4j
public class RepositoryApplication {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				VaultConfiguration.class);

		context.start();

		PersonRepository repository = context.getBean(PersonRepository.class);

		Person person = new Person();
		person.setId("heisenberg");
		person.setFirstname("Walter");
		person.setLastname("White");
		person.setSsn("1234");

		repository.save(person);

		log.info("Wrote data to Vault");

		VaultTemplate template = context.getBean(VaultTemplate.class);

		VaultResponse response = template.read("secret/person/heisenberg");

		log.info("Retrieved data {} from Vault via Template API", response.getData());

		Optional<Person> loaded = repository.findById(person.getId());
		log.info("Retrieved data {} from Vault via Repository", loaded.get());

		context.stop();
	}

	@Configuration
	@EnableVaultRepositories
	static class VaultConfiguration extends AbstractVaultConfiguration {

		@Override
		public VaultEndpoint vaultEndpoint() {
			return new VaultEndpoint();
		}

		@Override
		public ClientAuthentication clientAuthentication() {
			return new TokenAuthentication("00000000-0000-0000-0000-000000000000");
		}

		@Override
		public SslConfiguration sslConfiguration() {

			return SslConfiguration.forTrustStore(
					new FileSystemResource(new File(findWorkDir(), "keystore.jks")),
					"changeit".toCharArray());
		}
	}
}
