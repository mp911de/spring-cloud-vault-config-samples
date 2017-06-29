/*
 * Copyright 2017 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.config.AbstractVaultConfiguration.ClientFactoryWrapper;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Spring Cloud Vault Config is active for Spring Boot applications and within tests.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = CertificateOnDemandApplication.class)
public class SslPkiTests {

	@Autowired
	Environment environment;

	@Autowired
	ClientFactoryWrapper configuredWrapper;

	@LocalServerPort
	private int port;

	/**
	 * The configured client truststore contains just the Root CA certificate. The
	 * intermediate and server certificates are provided by the server SSL configuration.
	 */
	@Test
	public void shouldWorkWithGeneratedSslCertificate() {

		RestTemplate restTemplate = new RestTemplate(
				configuredWrapper.getClientHttpRequestFactory());

		ResponseEntity<String> response = restTemplate
				.getForEntity("https://localhost:" + port, String.class);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody()).isEqualTo("Hello, World");
	}

	/**
	 * Plain {@link RestTemplate} without the Root CA configured. Assuming the Root CA
	 * certificate is unknown to the default truststore, the request should fail.
	 */
	@Test
	public void clientShouldRejectUnknownRootCertificate() {

		RestTemplate restTemplate = new RestTemplate();

		try {
			restTemplate.getForEntity("https://localhost:" + port, String.class);
			fail("Missing ResourceAccessException that wraps SSLHandshakeException");
		}
		catch (ResourceAccessException e) {
			assertThat(e).hasCauseInstanceOf(SSLHandshakeException.class);
		}
	}
}
