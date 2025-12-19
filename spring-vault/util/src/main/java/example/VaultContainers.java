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

import java.util.function.Consumer;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

/**
 * Utility to create a {@link VaultContainer} for testing that uses SSL
 * certificates created via {@code create_certificates.sh}.
 * @author Mark Paluch
 */
public class VaultContainers {

	/**
	 * Create a {@link VaultContainer} with default {@link TestSettings} and
	 * register {@code spring.application.json} system property.
	 */
	public static VaultContainer<?> create() {
		return create(it -> {
		});
	}

	/**
	 * Create a {@link VaultContainer} with default {@link TestSettings} and
	 * register {@code spring.application.json} system property.
	 */
	public static VaultContainer<?> create(Consumer<VaultContainer<?>> containerConsumer) {

		MyVaultContainer container = new MyVaultContainer(DockerImageName.parse("hashicorp/vault:1.20.0"));
		container.withVaultToken(TestSettings.token().getToken());
		container.withReuse(true);
		containerConsumer.accept(container);

		return container;
	}

	static class MyVaultContainer extends VaultContainer {

		public MyVaultContainer(String dockerImageName) {
			super(dockerImageName);
		}

		public MyVaultContainer(DockerImageName dockerImageName) {
			super(dockerImageName);
		}

		@Override
		protected void containerIsStarted(InspectContainerResponse containerInfo) {
			super.containerIsStarted(containerInfo);
			System.setProperty("spring.application.json", """
					{
					  "spring.cloud.vault.uri": "%s",
					  "spring.cloud.vault.authentication": "token",
					  "spring.cloud.vault.token": "%s"
					}
					""".formatted(getHttpHostAddress(), TestSettings.tokenAsString()));

		}

	}

}
