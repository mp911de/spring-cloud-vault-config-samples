/*
 * Copyright 2025-present the original author or authors.
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
package example;

import org.testcontainers.vault.VaultContainer;

import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.support.VaultToken;

/**
 * Configuration utility for Vault.
 *
 * @author Mark Paluch
 */
public class TestSettings {

	private TestSettings() {
	}

	/**
	 * @return the token to use during tests.
	 */
	public static VaultToken token() {
		return VaultToken.of(System.getProperty("vault.token", "00000000-0000-0000-0000-000000000000").toCharArray());
	}

	/**
	 * @return the token to use during tests as {@link String}.
	 */
	public static String tokenAsString() {
		return token().getToken();
	}

	/**
	 * @return the token authentication to use during tests.
	 */
	public static TokenAuthentication authentication() {
		return new TokenAuthentication(token());
	}

	public static VaultEndpoint endpoint(VaultContainer<?> vaultContainer) {
		return VaultEndpoint.from(vaultContainer.getHttpHostAddress());
	}


}
