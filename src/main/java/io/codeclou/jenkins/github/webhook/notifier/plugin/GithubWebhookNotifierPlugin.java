/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.github.webhook.notifier.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import hudson.Plugin;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class GithubWebhookNotifierPlugin extends Plugin {

    /*
     * http://jenkins.foo/plugin/github-webhook-notifier-plugin/receive
     */
    @RequirePOST
    public HttpResponse doReceive(HttpServletRequest request, StaplerRequest staplerRequest) throws IOException, ServletException {
        String jenkinsRootUrl = Jenkins.getInstance().getRootUrl(); // will return something like: http://localhost:8080/jenkins/
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        try {
            GithubWebhookPayload githubWebhookPayload = gson.fromJson(reader, GithubWebhookPayload.class);
            // Trigger Git-Plugins notify push SCM Polling Endpoint
            String gitPluginNotifyUrl = jenkinsRootUrl +
                    "git/notifyCommit?url=" +
                    githubWebhookPayload.getRepository().getClone_url() +
                    "&branches=" +
                    githubWebhookPayload.getRef() +
                    "&sha1=" +
                    githubWebhookPayload.getAfter();
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] certificate, String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            HttpGet httpGet = new HttpGet(gitPluginNotifyUrl);
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String gitNotificationResponse = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            String responseText = "ok triggered: " + gitPluginNotifyUrl + "\nstatus: " + statusCode + "\n\n" + gitNotificationResponse;
            if (statusCode != 200) {
                return HttpResponses.error(400, responseText);
            }
            return HttpResponses.plainText(responseText);
        } catch (JsonSyntaxException ex) {
            return HttpResponses.error(500, "json invalid");
        } catch (NoSuchAlgorithmException ex) {
            return HttpResponses.error(500, "NoSuchAlgorithmException");
        } catch (KeyStoreException ex) {
            return HttpResponses.error(500, "KeyStoreException");
        } catch (KeyManagementException ex) {
            return HttpResponses.error(500, "KeyManagementException");
        }
    }
}
