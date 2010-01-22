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

public class HttpUtils {

	public static HttpResponse getWebRessource(String url, String user, String password) throws IOException,
			ClientProtocolException {
		HttpGet get = new HttpGet(url);
		HttpClient client = createClient(user, password);
		return client.execute(get);
	}

	static InputStream getWebRessourceAsStream(String url, String user, String password) throws IOException,
			ClientProtocolException {

		return getWebRessource(url, user, password).getEntity().getContent();
	}

	private static HttpClient createClient(String user, String password) {
		DefaultHttpClient client = new DefaultHttpClient();

		if (user != null && user.length() > 0) {
			Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
			client.getCredentialsProvider().setCredentials(AuthScope.ANY,
					defaultcreds);
		}

		return client;
	}

}
