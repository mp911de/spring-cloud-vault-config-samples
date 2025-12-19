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

import example.VaultContainers;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.*;

/**
 * Spring Cloud Vault Config can obtain credentials for MySQL datasources.
 * Credentials are picked up by Spring Boot's auto-configuration for data
 * sources.
 *
 * @author Mark Paluch
 */
@SpringBootTest
@Testcontainers
public class MySqlTests {

	@Container
	static MySQLContainer mysqlContainer = new MySQLContainer(DockerImageName.parse("mysql"))
			.withNetwork(Network.SHARED)
			.withUsername("root")
			.withPassword("my-secret-pw")
			.withNetworkAliases("mysql")
			.withInitScript("privileges.sql");

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {

		mysqlContainer.start();
		it.withReuse(false);
		it.withInitCommand("secrets enable database");
		it.withInitCommand("write database/config/docker-mysql plugin_name=mysql-database-plugin allowed_roles=my-mysql-role " +
						   "connection_url=\"root:my-secret-pw@tcp(mysql:3306)/test\"");
		it.withInitCommand("write database/roles/my-mysql-role db_name=docker-mysql default_ttl=1h " +
						   "creation_statements=\"CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON *.* TO '{{name}}'@'%';\"");
		it.withInitCommand("read database/creds/my-mysql-role");
		it.withNetwork(Network.SHARED);
	});

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
	}

	@Autowired
	DataSource dataSource;

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
