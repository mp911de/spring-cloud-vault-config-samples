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
package example.totp;

import java.util.Collections;

import example.totp.TotpController.TotpKeyRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * Tests showing Vault's TOTP backend usage with key generation, code generation and code
 * verification.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TotpTests {

	private static final String keyId = TotpController.createKeyId("foo");

	@Autowired
	VaultOperations vaultOperations;

	@Before
	public void before() throws Exception {

		String version = vaultOperations.opsForSys().health().getVersion();

		assumeTrue("TOTP is only available for Vault 0.7.2 and newer",
				version != null && version.compareTo("0.7.1") >= 0);

		TotpKeyRequest request = new TotpKeyRequest("foo", "Spring Vault TOTP Demo");

		vaultOperations.write(String.format("totp/keys/%s", keyId), request);
	}

	@Test
	public void shouldCreateCodeAndValidateCode() {

		VaultResponse codeResponse = vaultOperations
				.read(String.format("totp/code/%s", keyId));

		String code = (String) codeResponse.getData().get("code");

		VaultResponse verificationResponse = vaultOperations.write(
				String.format("totp/code/%s", keyId),
				Collections.singletonMap("code", code));

		assertThat(verificationResponse.getData().get("valid")).isEqualTo(true);
	}
}
