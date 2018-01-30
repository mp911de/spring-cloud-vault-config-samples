/*
 * Copyright 2018-2018 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for Spring Cloud Vault Config with Spring Data repositories.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RepositoryTests {

	@Autowired
	PersonRepository repository;

	@Autowired
	VaultTemplate template;

	@Test
	public void shouldWriteAndReadObjects() {

		Person person = new Person();
		person.setId("heisenberg");
		person.setFirstname("Walter");
		person.setLastname("White");
		person.setSsn("1234");

		repository.save(person);

		VaultResponse response = template.read("secret/person/heisenberg");

		assertThat(response.getRequiredData())
				.containsEntry("_class", "example.repositories.Person")
				.containsEntry("firstname", "Walter").doesNotContainKey("id");
	}
}
