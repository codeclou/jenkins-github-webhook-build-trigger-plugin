/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhooknotifierplugin;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

import java.util.HashMap;
import java.util.Map;

public class GithubWebhookEnvironmentContributionAction implements EnvironmentContributingAction {

    private transient Map<String, String> environmentVariables = new HashMap<>();
    private transient String envVarInfo;

    public GithubWebhookEnvironmentContributionAction(GithubWebhookPayload payload) {
        String normalizedBranch = this.normalizeBranchNameOrEmptyString(payload.getRef());
        String normalizedTag = this.normalizeTagNameOrEmptyString(payload.getRef());
        StringBuilder info = new StringBuilder();
        info.append("   ref\n      -> $GWNP_REF            : ").append(payload.getRef()).append("\n");
        info.append("      -> $GWNP_TAG            : ").append(normalizedTag).append("\n");
        info.append("      -> $GWNP_BRANCH         : ").append(normalizedBranch).append("\n\n");
        info.append("   before\n      -> $GWNP_COMMIT_BEFORE  : ").append(payload.getBefore()).append("\n\n");
        info.append("   after\n      -> $GWNP_COMMIT_AFTER   : ").append(payload.getAfter()).append("\n\n");
        info.append("   repository.clone_url\n      -> $GWNP_REPO_CLONE_URL : ").append(payload.getRepository().getClone_url()).append("\n\n");
        info.append("   repository.html_url\n      -> $GWNP_REPO_HTML_URL  : ").append(payload.getRepository().getHtml_url()).append("\n\n");
        info.append("   repository.full_name\n      -> $GWNP_REPO_FULL_NAME : ").append(payload.getRepository().getFull_name()).append("\n\n");
        info.append("   repository.name\n      -> $GWNP_REPO_NAME      : ").append(payload.getRepository().getName()).append("\n\n");
        this.envVarInfo = info.toString();
        this.environmentVariables.put("GWNP_REF", payload.getRef());
        this.environmentVariables.put("GWNP_TAG", normalizedTag);
        this.environmentVariables.put("GWNP_BRANCH", normalizedBranch);
        this.environmentVariables.put("GWNP_COMMIT_BEFORE", payload.getBefore());
        this.environmentVariables.put("GWNP_COMMIT_AFTER", payload.getAfter());
        this.environmentVariables.put("GWNP_REPO_CLONE_URL", payload.getRepository().getClone_url());
        this.environmentVariables.put("GWNP_REPO_HTML_URL", payload.getRepository().getHtml_url());
        this.environmentVariables.put("GWNP_REPO_FULL_NAME", payload.getRepository().getFull_name());
        this.environmentVariables.put("GWNP_REPO_NAME", payload.getRepository().getName());
    }

    /*
     * converts "refs/heads/develop" to "develop"
     */
    private String normalizeBranchNameOrEmptyString(String branchname) {
        if (branchname.startsWith("refs/heads/")) {
            return branchname.replace("refs/heads/", "");
        }
        return "";
    }

    /*
     * converts "refs/tags/1.0.0" to "1.0.0"
     */
    private String normalizeTagNameOrEmptyString(String tagname) {
        if (tagname.startsWith("refs/tags/")) {
            return tagname.replace("refs/tags/", "");
        }
        return "";
    }

    protected String getEnvVarInfo() {
        return this.envVarInfo;
    }


    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "GithubWebhookEnvironmentContributionAction";
    }

    public String getUrlName() {
        return "GithubWebhookEnvironmentContributionAction";
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        if (env == null) {
            return;
        }
        if (environmentVariables != null) {
            env.putAll(environmentVariables);
        }
    }
}
