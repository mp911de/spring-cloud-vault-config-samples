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
package example.pki;

import javax.net.ssl.SSLHandshakeException;

import example.VaultContainers;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Fail.fail;

/**
 * Spring Cloud Vault Config is active for Spring Boot applications and within
 * tests.
 *
 * @author Mark Paluch
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = CertificateOnDemandApplication.class)
@Testcontainers
public class SslPkiTests {

	@Container
	static VaultContainer<?> vaultContainer = VaultContainers.create(it -> {
		it.withInitCommand("secrets enable pki");
		it.withInitCommand("write pki/root/generate/internal common_name=integration-test");
		it.withInitCommand(
				"write pki/roles/test-role allowed_domains=localhost,example.com allow_localhost=true max_ttl=72h");
	});

	@Autowired
	RestClient client;

	@LocalServerPort
	private int port;

	/**
	 * The configured client truststore contains just the Root CA certificate. The
	 * intermediate and server certificates are provided by the server SSL
	 * configuration.
	 */
	@Test
	public void shouldWorkWithGeneratedSslCertificate() {

		ResponseEntity<String> response = client.get()
				.uri("https://localhost:" + port)
				.retrieve().toEntity(String.class);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isEqualTo("Hello, World");
	}

	/**
	 * Plain {@link RestClient} without the Root CA configured. Assuming the Root CA
	 * certificate is unknown to the default truststore, the request should fail.
	 */
	@Test
	public void clientShouldRejectUnknownRootCertificate() {

		RestClient vanilla = RestClient.create();

		try {
			vanilla.get()
					.uri("https://localhost:" + port)
					.retrieve().toEntity(String.class);
			fail("Missing ResourceAccessException that wraps SSLHandshakeException");
		} catch (ResourceAccessException e) {
			assertThat(e).hasCauseInstanceOf(SSLHandshakeException.class);
		}
	}

}
