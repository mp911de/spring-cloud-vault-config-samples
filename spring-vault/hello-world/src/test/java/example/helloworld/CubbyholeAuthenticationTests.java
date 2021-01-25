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
package example.helloworld;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import example.ExamplesSslConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.CubbyholeAuthentication;
import org.springframework.vault.authentication.CubbyholeAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.RestOperationsCallback;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultToken;
import org.springframework.web.client.RestOperations;

import static example.WorkDirHelper.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Setup and use Cubbyhole authentication.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@Slf4j
public class CubbyholeAuthenticationTests {

	private static VaultToken initialToken;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	VaultOperations vaultOperations;

	/**
	 * Write some data to Vault before Vault can be used as {@link VaultPropertySource}.
	 */
	@BeforeClass
	public static void beforeClass() {


		VaultOperations vaultOperations = new VaultTestConfiguration().vaultTemplate();
		vaultOperations.write("secret/myapp/configuration",
				Collections.singletonMap("configuration.key", "value"));

		VaultResponse response = vaultOperations
				.doWithSession(new RestOperationsCallback<VaultResponse>() {

					@Override
					public VaultResponse doWithRestOperations(
							RestOperations restOperations) {

						HttpHeaders headers = new HttpHeaders();
						headers.add("X-Vault-Wrap-TTL", "10m");

						return restOperations.postForObject("auth/token/create",
								new HttpEntity<Object>(headers), VaultResponse.class);
					}
				});

		// Response Wrapping requires Vault 0.6.0+
		Map<String, String> wrapInfo = response.getWrapInfo();
		initialToken = VaultToken.of(wrapInfo.get("token"));
	}

	/**
	 * {@link org.springframework.vault.core.VaultTemplate},
	 * {@link CubbyholeAuthentication} retrieves the wrapped token from
	 * {@code cubbyhole/response} and uses the wrapped token to access Vault.
	 */
	@Test
	public void environmentShouldExposeVaultPropertySource() {

		vaultOperations.write("secret/key", Collections.singletonMap("key", "value"));
		assertThat(vaultOperations.read("secret/key").getData()).containsEntry("key",
				"value");
	}

	/**
	 * Java Configuration to bootstrap Spring Vault.
	 */
	@Configuration
	static class VaultConfiguration extends AbstractVaultConfiguration {

		@Override
		public VaultEndpoint vaultEndpoint() {
			return new VaultEndpoint();
		}

		@Override
		public ClientAuthentication clientAuthentication() {

			CubbyholeAuthenticationOptions options = CubbyholeAuthenticationOptions
					.builder() //
					.wrapped() //
					.initialToken(initialToken) //
					.build();

			return new CubbyholeAuthentication(options, restOperations());
		}

		@Override
		public SslConfiguration sslConfiguration() {
			return ExamplesSslConfiguration.create();
		}
	}
}
