/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhooknotifierplugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import hudson.Extension;
import hudson.model.*;
import hudson.scm.SCMRevisionState;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        try {
            GithubWebhookPayload githubWebhookPayload = gson.fromJson(reader, GithubWebhookPayload.class);
            GithubWebhookEnvironmentContributionAction environmentContributionAction = new GithubWebhookEnvironmentContributionAction(githubWebhookPayload);
            String jobNamePrefix = this.normalizeRepoFullName(githubWebhookPayload.getRepository().getFull_name());
            StringBuilder jobsTriggered = new StringBuilder();
            ArrayList<String> jobsAlreadyTriggered = new ArrayList<>();
            StringBuilder causeNote = new StringBuilder();
            causeNote.append("github-webhook-notifier-plugin:\n");
            causeNote.append(githubWebhookPayload.getAfter()).append("\n");
            causeNote.append(githubWebhookPayload.getRef()).append("\n");
            causeNote.append(githubWebhookPayload.getRepository().getClone_url());
            Cause cause = new Cause.RemoteCause("github.com", causeNote.toString());
            Collection<Job> jobs = Jenkins.getInstance().getAllItems(Job.class);
            if (jobs.isEmpty()) {
                jobsTriggered.append("WARNING NO JOBS FOUND!\n");
                jobsTriggered.append("If you are using matrix-based security, please give the following rights to 'Anonymous'.\n");
                jobsTriggered.append("'Job' -> build, discover, read.\n");
            }
            for (Job job: jobs) {
                if (job.getName().startsWith(jobNamePrefix) && ! jobsAlreadyTriggered.contains(job.getName())) {
                    jobsTriggered.append("   ").append(job.getName()).append("\n");
                    jobsAlreadyTriggered.add(job.getName());
                    AbstractProject projectScheduable = (AbstractProject) job;
                    projectScheduable.scheduleBuild(0, cause, environmentContributionAction);
                }
            }
            StringBuilder info = new StringBuilder();
            info.append(">> webhook content to env vars").append("\n");
            info.append(environmentContributionAction.getEnvVarInfo());
            info.append("\n");
            info.append(">> jobs triggered with name matching '").append(jobNamePrefix).append("*'").append("\n");
            info.append(jobsTriggered.toString());
            return HttpResponses.plainText(this.getTextEnvelopedInBanner(info.toString()));
        } catch (JsonSyntaxException ex) {
            return HttpResponses.error(500, this.getTextEnvelopedInBanner("github webhook json invalid"));
        }
    }

    /*
     * converts "codeclou/foo" to "codeclou---foo"
     */
    private String normalizeRepoFullName(String reponame) {
        return reponame.replace("/", "---");
    }

    private String getTextEnvelopedInBanner(String text) {
        StringBuilder banner = new StringBuilder();
        banner.append("----------------------------------------------------------------------------------\n");
        banner.append("github-webhook-notifier-plugin").append("\n");
        banner.append("----------------------------------------------------------------------------------\n");
        banner.append(text);
        banner.append("\n----------------------------------------------------------------------------------\n");
        return banner.toString();
    }
}
