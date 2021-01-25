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
package example.cloudfoundry;

import example.ExamplesSslConfiguration;
import io.pivotal.spring.cloud.vault.config.java.VaultConnectorsConfig;
import io.pivotal.spring.cloud.vault.service.VaultServiceConnectorConfig;
import io.pivotal.spring.cloud.vault.service.common.VaultServiceInfo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.Cloud;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.VaultResponse;

import example.ExamplesSslConfiguration;
import io.pivotal.spring.cloud.vault.config.java.VaultConnectorsConfig;
import io.pivotal.spring.cloud.vault.service.VaultServiceConnectorConfig;
import io.pivotal.spring.cloud.vault.service.common.VaultServiceInfo;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.Cloud;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.VaultResponse;

/**
 * Sample application expecting {@code VCAP_APPLICATION} and {@code VCAP_SERVICES} env
 * variables to be set.
 * <p>
 * Application retrieves its config from the CloudFoundry environment config thus it does
 * not require any token/address details here.
 * <p>
 * See {@code VCAP_APPLICATION.json} and {@code VCAP_SERVICES.json} files.
 * <p>
 * Use {@link CloudFoundryApplicationStarter} as launcher that takes care of env variable
 * setup.
 *
 * @author Mark Paluch
 * @see CloudFoundryApplicationStarter
 */
@Slf4j
public class CloudFoundryApplication {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				VaultConnectorsConfig.class, VaultConfig.class);

		context.start();

		Cloud cloud = context.getBean(Cloud.class);
		SslConfiguration sslConfiguration = context.getBean(SslConfiguration.class);

		VaultServiceInfo vaultServiceInfo = (VaultServiceInfo) cloud
				.getServiceInfos(VaultOperations.class).get(0);

		VaultServiceConnectorConfig config = VaultServiceConnectorConfig.builder()
				.sslConfiguration(sslConfiguration).build();

		VaultOperations vaultOperations = cloud
				.getSingletonServiceConnector(VaultOperations.class, config);

		VaultResponse response = vaultOperations
				.read(vaultServiceInfo.getBackends().get("generic") + "/application");

		log.info("Retrieved app-key: {}", response.getData().get("app-key"));

		context.stop();
	}

	@Configuration
	static class VaultConfig {

		@Bean
		public SslConfiguration sslConfiguration() {
			return ExamplesSslConfiguration.create();
		}
	}
}
