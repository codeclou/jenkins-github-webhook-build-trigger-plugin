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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;


public class GithubWebhookNotifierPlugin extends Plugin {

    /*
     * http://jenkins.foo/plugin/github-webhook-notifier-plugin/receive
     */
    @RequirePOST
    public HttpResponse doReceive(HttpServletRequest request, StaplerRequest staplerRequest) throws IOException, ServletException {
        String jeninsRootUrl = Jenkins.getInstance().getRootUrl(); // will return something like: http://localhost:8080/jenkins/
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        try {
            GithubWebhookPayload githubWebhookPayload = gson.fromJson(reader, GithubWebhookPayload.class);
            String gitPluginNotifyUrl = jeninsRootUrl +
                    "git/notifyCommit?" +
                    githubWebhookPayload.getRepository().getClone_url() +
                    "&branches=" +
                    githubWebhookPayload.getRef() +
                    "&sha1=" +
                    githubWebhookPayload.getAfter();
            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(gitPluginNotifyUrl);
            int statusCode = client.executeMethod(method);
            String responseText = "triggered: " + gitPluginNotifyUrl + "\nstatus: " + statusCode;
            if (statusCode != 200) {
                return HttpResponses.error(400, responseText);
            }
            return HttpResponses.plainText(responseText);
        } catch (JsonSyntaxException ex) {
            return HttpResponses.error(500, "json invalid");
        }
    }
}
