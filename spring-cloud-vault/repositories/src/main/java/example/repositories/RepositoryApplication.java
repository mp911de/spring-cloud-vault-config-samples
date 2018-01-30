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

import java.util.Optional;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.repository.configuration.EnableVaultRepositories;
import org.springframework.vault.support.VaultResponse;

@SpringBootApplication
@Slf4j
@EnableVaultRepositories
public class RepositoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepositoryApplication.class, args);
	}

	@Autowired
	PersonRepository repository;

	@Autowired
	VaultTemplate template;

	@PostConstruct
	private void postConstruct() {

		System.out.println("##########################");

		Person person = new Person();
		person.setId("heisenberg");
		person.setFirstname("Walter");
		person.setLastname("White");
		person.setSsn("1234");

		repository.save(person);

		log.info("Wrote data to Vault");

		VaultResponse response = template.read("secret/person/heisenberg");

		log.info("Retrieved data {} from Vault via Template API", response.getData());

		Optional<Person> loaded = repository.findById(person.getId());
		log.info("Retrieved data {} from Vault via Repository", loaded.get());

		System.out.println("##########################");
	}
}
