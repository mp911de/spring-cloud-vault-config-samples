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

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Launcher for {@link CloudFoundryApplication} with environment variable setup from
 * {@code VCAP_SERVICES.json} and {@code VCAP_APPLICATION.json} files.
 *
 * @author Mark Paluch
 * @see CloudFoundryApplication
 */
public class CloudFoundryApplicationStarter {

	public static void main(String[] args) throws IOException, InterruptedException {

		ProcessBuilder processBuilder = getProcessBuilder();

		System.out.println("##############################################");
		System.out.println("# Starting CloudFoundryApplication...        #");
		System.out.println("##############################################");
		System.out.println();

		Process process = processBuilder.inheritIO().start();

		int ret = process.waitFor();

		System.out.println();
		System.out.println("##############################################");
		System.out.println("# CloudFoundryApplication terminated         #");
		System.out.println("##############################################");

		System.exit(ret);
	}

	private static ProcessBuilder getProcessBuilder() throws IOException {

		String javaHome = System.getProperty("java.home");
		String userDir = System.getProperty("user.dir");
		String pathSeparator = System.getProperty("path.separator");

		Class<CloudFoundryApplicationStarter> cls = CloudFoundryApplicationStarter.class;
		URLClassLoader loader = (URLClassLoader) cls.getClassLoader();

		String classPath = StringUtils.arrayToDelimitedString(loader.getURLs(),
				pathSeparator);

		String application = new String(FileCopyUtils
				.copyToByteArray(cls.getResourceAsStream("/VCAP_APPLICATION.json")));
		String services = new String(FileCopyUtils
				.copyToByteArray(cls.getResourceAsStream("/VCAP_SERVICES.json")));
		ProcessBuilder processBuilder = new ProcessBuilder()
				.command(String.format("%s/bin/java", javaHome), "-cp", classPath,
						CloudFoundryApplication.class.getName())
				.directory(new File(userDir));

		Map<String, String> env = processBuilder.environment();
		env.put("VCAP_SERVICES", services);
		env.put("VCAP_APPLICATION", application);

		return processBuilder;
	}
}
