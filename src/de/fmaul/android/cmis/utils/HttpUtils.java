/*
 * Copyright (C) 2010 Florian Maul
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fmaul.android.cmis.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Wrapper against commons http client which is included in android.
 * Unfortunately it is an older version with an old syntax. Better encapsulate
 * all everything here to support easier migration.
 * 
 * @author Florian Maul
 * 
 */
public class HttpUtils {

	public static HttpResponse getWebRessource(String url, String user,
			String password) throws IOException, ClientProtocolException {
		HttpGet get = new HttpGet(url);
		HttpClient client = createClient(user, password);
		return client.execute(get);
	}

	static InputStream getWebRessourceAsStream(String url, String user,
			String password) throws IOException, ClientProtocolException {

		return getWebRessource(url, user, password).getEntity().getContent();
	}

	private static HttpClient createClient(String user, String password) {
		DefaultHttpClient client = new DefaultHttpClient();

		if (user != null && user.length() > 0) {
			Credentials defaultcreds = new UsernamePasswordCredentials(user,
					password);
			client.getCredentialsProvider().setCredentials(AuthScope.ANY,
					defaultcreds);
		}

		return client;
	}

}
