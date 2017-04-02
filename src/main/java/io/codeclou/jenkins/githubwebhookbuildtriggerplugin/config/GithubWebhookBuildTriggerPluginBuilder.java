/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhookbuildtriggerplugin.config;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/*
 * Enable Plugin to store global config under "Jenkins" => "configure" tab.
 */
public class GithubWebhookBuildTriggerPluginBuilder extends Builder {

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String webhookSecret;
        private static DescriptorImpl descriptor=null;

        public DescriptorImpl() {
            load();
            descriptor=this;
        }
        public DescriptorImpl(String webhookSecret) {
            load();
            this.webhookSecret=webhookSecret;
            descriptor=this;
        }
        public static DescriptorImpl getDescriptor() {
            return descriptor;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "GithubWebhookBuildTriggerPlugin";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            json = json.getJSONObject("config");
            webhookSecret = json.getString("webhookSecret");
            save();
            return true;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

    }

}
