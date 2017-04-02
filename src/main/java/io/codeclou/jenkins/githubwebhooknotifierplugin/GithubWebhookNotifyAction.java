/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhooknotifierplugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
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

@Extension
public class GithubWebhookNotifyAction implements UnprotectedRootAction {

    @Override
    public String getUrlName() {
        return "github-webhook-notifier";
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "github-webhook-notifier";
    }

    /*
     * http://jenkins.foo/github-webhook-notifier/receive
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
                    this.normalizeBranchNameForJenkins(githubWebhookPayload.getRef()) +
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
            StringBuilder responseText = new StringBuilder();
            responseText.append("----------------------------------------------------------------------------------\n");
            responseText.append("github-webhook-notifier-plugin - parsed webhook payload:\n");
            responseText.append("   ref:       ").append(githubWebhookPayload.getRef()).append("\n");
            responseText.append("   before:    ").append(githubWebhookPayload.getBefore()).append("\n");
            responseText.append("   after:     ").append(githubWebhookPayload.getAfter()).append("\n");
            responseText.append("   clone_url: ").append(githubWebhookPayload.getRepository().getClone_url()).append("\n");
            responseText.append("----------------------------------------------------------------------------------\n");
            responseText.append(">> REQUEST\n").append(gitPluginNotifyUrl).append("\n\n");
            responseText.append("<< RESPONSE HTTP ").append(statusCode).append("\n");
            responseText.append(gitNotificationResponse);
            if (statusCode != 200) {
                return HttpResponses.error(400, responseText.toString());
            }
            return HttpResponses.plainText(responseText.toString());
        } catch (JsonSyntaxException ex) {
            return HttpResponses.error(500, "github-webhook-notifier-plugin: github webhook json invalid");
        } catch (NoSuchAlgorithmException ex) {
            return HttpResponses.error(500, "github-webhook-notifier-plugin: internal error NoSuchAlgorithmException");
        } catch (KeyStoreException ex) {
            return HttpResponses.error(500, "github-webhook-notifier-plugin: internal error KeyStoreException");
        } catch (KeyManagementException ex) {
            return HttpResponses.error(500, "github-webhook-notifier-plugin: internal error KeyManagementException");
        }
    }

    /*
     * converts "refs/heads/develop" to "origin/develop"
     */
    private String normalizeBranchNameForJenkins(String branchname) {
        return branchname.replace("refs/heads/", "origin/");
    }

}
