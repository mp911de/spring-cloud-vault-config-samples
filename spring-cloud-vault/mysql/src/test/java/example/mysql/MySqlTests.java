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

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.SocketUtils;

/**
 * Spring Cloud Vault Config can obtain credentials for MySQL datasources. Credentials are
 * picked up by Spring Boot's auto-configuration for data sources.
 *
 * @author Mark Paluch
 */
@SpringBootTest
public class MySqlTests {

	@Autowired
	DataSource dataSource;

	@BeforeAll
	static void beforeAll() {

		// A service is listening on 3306
		assumeThat(SocketUtils.findAvailableTcpPorts(1, 3306, 3307)).isEmpty();
	}

	@Test
	public void shouldConnectToMySql() throws Exception {

		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement()) {
			{
				try (ResultSet resultSet = statement
						.executeQuery("SELECT CURRENT_USER();")) {

					assertThat(resultSet.next()).isTrue();
				}
			}
		}
	}
}
