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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TOTP Controller providing views with model variables and a TOTP verification endpoint.
 *
 * @author Mark Paluch
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TotpController {

	final VaultOperations vaultOperations;

	@GetMapping("/")
	String index(Model model) {
		return "index";
	}

	@PostMapping("/totp/generate")
	String createToken(String username, Model model) {

		TotpKeyRequest request = new TotpKeyRequest(username, "Spring Vault TOTP Demo");

		String keyId = createKeyId(username);

		VaultResponse response = vaultOperations
				.write(String.format("totp/keys/%s", keyId), request);

		model.addAttribute("username", username);
		model.addAttribute("barcode", response.getData().get("barcode"));

		log.info("Created key {}", keyId);
		return "token";
	}

	static String createKeyId(String username) {

		byte[] digest = DigestUtils.getSha512Digest()
				.digest(username.getBytes(StandardCharsets.UTF_8));
		return new String(Hex.encodeHex(digest));
	}

	@PostMapping(path = "/totp/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	boolean verify(@RequestBody Map<String, String> payload) {

		String username = payload.get("username");
		String code = payload.get("code");
		String keyId = createKeyId(username);

		VaultResponse response = vaultOperations.write(
				String.format("totp/code/%s", keyId),
				Collections.singletonMap("code", code));

		return (Boolean) response.getData().get("valid");
	}

	@Value
	@RequiredArgsConstructor
	static class TotpKeyRequest {

		@JsonProperty("account_name")
		private final String name;
		private final String issuer;
		private final boolean generate = true;
	}
}
