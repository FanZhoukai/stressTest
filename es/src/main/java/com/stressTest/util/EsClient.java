package com.stressTest.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class EsClient {
    private static final ElasticsearchClient clint = clientWithPassword();

    private static ElasticsearchClient clint() {
        // Create the low-level client
        RestClient restClient = RestClient.builder(new HttpHost("", 9200)).build();
        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        // And create the API client
        return new ElasticsearchClient(transport);
    }

    private static ElasticsearchClient clientWithPassword() {
        // Example usage
        String host = "";
        int port = -1;
        String username = "";
        String password = "";
        // Set up credentials for Basic Auth
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY, // Apply to all scopes
                new UsernamePasswordCredentials(username, password)
        );

        // Create the low-level client with authentication
        RestClient restClient = RestClient.builder(new HttpHost(host, port))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                }).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }

    public static ElasticsearchClient getClint() {
        return clint;
    }
}
