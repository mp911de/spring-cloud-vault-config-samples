/*
 * Copyright 2018 the original author or authors.
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

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.vault.support.SslConfiguration;

import static example.WorkDirHelper.*;

/**
 * Configuration utility for SSL usage within the examples.
 *
 * @author Mark Paluch
 */
public class ExamplesSslConfiguration {

	private ExamplesSslConfiguration() {
	}

	/**
	 * @return the default {@link SslConfiguration}.
	 */
	public static SslConfiguration create() {
		return SslConfiguration.forTrustStore(
				new FileSystemResource(new File(findWorkDir(), "keystore.jks")),
				"changeit");
	}
}
