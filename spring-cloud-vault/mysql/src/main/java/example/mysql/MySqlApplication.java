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
package example.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample Application using Spring Cloud Vault with Token authentication. Vault will
 * obtain MySQL credentials to be used with a {@link javax.activation.DataSource}.
 *
 * @author Mark Paluch
 */
@SpringBootApplication
public class MySqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(MySqlApplication.class, args);
	}

	@Autowired
	DataSource dataSource;

	@PostConstruct
	private void postConstruct() throws Exception {

		System.out.println("##########################");

		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement()) {

			ResultSet resultSet = statement.executeQuery("SELECT CURRENT_USER();");
			resultSet.next();

			System.out.println("Connection works with User: " + resultSet.getString(1));

			resultSet.close();
		}

		System.out.println("##########################");
	}
}
