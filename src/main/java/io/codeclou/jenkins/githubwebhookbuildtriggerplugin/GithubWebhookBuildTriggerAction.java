/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhookbuildtriggerplugin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import hudson.Extension;
import hudson.model.*;
import hudson.util.HttpResponses;
import io.codeclou.jenkins.githubwebhookbuildtriggerplugin.config.GithubWebhookBuildTriggerPluginBuilder;
import io.codeclou.jenkins.githubwebhookbuildtriggerplugin.webhooksecret.GitHubWebhookUtility;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

@Extension
public class GithubWebhookBuildTriggerAction implements UnprotectedRootAction {

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
        StringWriter writer = new StringWriter();
        IOUtils.copy(request.getInputStream(), writer, "UTF-8");
        String requestBody = writer.toString();
        Gson gson = new Gson();
        StringBuilder info = new StringBuilder();
        try {
            //
            // WEBHOOK SECRET
            //
            String githubSignature = request.getHeader("x-hub-signature");
            String webhookSecretAsConfiguredByUser = GithubWebhookBuildTriggerPluginBuilder.DescriptorImpl.getDescriptor().getWebhookSecret();
            String webhookSecretMessage ="validating webhook payload against wevhook secret.";
            info.append(">> webhook secret validation").append("\n");
            if (webhookSecretAsConfiguredByUser == null) {
                webhookSecretMessage = "   skipping validation since no webhook secret is configured in \n" +
                                       "   'Jenkins' -> 'Configure' tab under 'Github Webhook Build Trigger' section.";
            } else {
                Boolean isValid = GitHubWebhookUtility.verifySignature(requestBody, githubSignature, webhookSecretAsConfiguredByUser);
                if (!isValid) {
                    info.append(webhookSecretMessage).append("\n");
                    return HttpResponses.error(500, this.getTextEnvelopedInBanner(info.toString() + "   ERROR: github webhook secret signature check failed. Check your webhook secret."));
                }
                webhookSecretMessage = "   ok. Webhook secret validates against " +  githubSignature + "\n";
            }
            info.append(webhookSecretMessage).append("\n");
            //
            // PAYLOAD TO ENVVARS
            //
            GithubWebhookPayload githubWebhookPayload = gson.fromJson(requestBody, GithubWebhookPayload.class);
            EnvironmentContributionAction environmentContributionAction = new EnvironmentContributionAction(githubWebhookPayload);
            //
            // TRIGGER JOBS
            //
            String jobNamePrefix = this.normalizeRepoFullName(githubWebhookPayload.getRepository().getFull_name());
            StringBuilder jobsTriggered = new StringBuilder();
            ArrayList<String> jobsAlreadyTriggered = new ArrayList<>();
            StringBuilder causeNote = new StringBuilder();
            causeNote.append("github-webhook-build-trigger-plugin:\n");
            causeNote.append(githubWebhookPayload.getAfter()).append("\n");
            causeNote.append(githubWebhookPayload.getRef()).append("\n");
            causeNote.append(githubWebhookPayload.getRepository().getClone_url());
            Cause cause = new Cause.RemoteCause("github.com", causeNote.toString());
            Collection<Job> jobs = Jenkins.getInstance().getAllItems(Job.class);
            if (jobs.isEmpty()) {
                jobsTriggered.append("   WARNING NO JOBS FOUND!\n");
                jobsTriggered.append("      If you are using matrix-based security, please give the following rights to 'Anonymous'.\n");
                jobsTriggered.append("      'Job' -> build, discover, read.\n");
            }
            for (Job job: jobs) {
                if (job.getName().startsWith(jobNamePrefix) && ! jobsAlreadyTriggered.contains(job.getName())) {
                    jobsTriggered.append("   ").append(job.getName()).append("\n");
                    jobsAlreadyTriggered.add(job.getName());
                    AbstractProject projectScheduable = (AbstractProject) job;
                    projectScheduable.scheduleBuild(0, cause, environmentContributionAction);
                }
            }
            //
            // WRITE ADDITONAL INFO
            //
            info.append(">> webhook content to env vars").append("\n");
            info.append(environmentContributionAction.getEnvVarInfo());
            info.append("\n");
            info.append(">> jobs triggered with name matching '").append(jobNamePrefix).append("*'").append("\n");
            info.append(jobsTriggered.toString());
            return HttpResponses.plainText(this.getTextEnvelopedInBanner(info.toString()));
        } catch (JsonSyntaxException ex) {
            return HttpResponses.error(500, this.getTextEnvelopedInBanner(info.toString() + "   ERROR: github webhook json invalid"));
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
        banner.append("github-webhook-build-trigger-plugin").append("\n");
        banner.append("----------------------------------------------------------------------------------\n");
        banner.append(text);
        banner.append("\n----------------------------------------------------------------------------------\n");
        return banner.toString();
    }
}
