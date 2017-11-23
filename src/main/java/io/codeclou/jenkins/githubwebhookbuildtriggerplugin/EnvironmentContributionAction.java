/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhookbuildtriggerplugin;

import hudson.EnvVars;
import hudson.model.*;

import java.util.*;

/*
 * Inject Environment Variables into the triggered job
 */
public class EnvironmentContributionAction implements EnvironmentContributingAction {

    private transient Map<String, String> environmentVariables = new HashMap<>();
    private transient String envVarInfo;

    public EnvironmentContributionAction(GithubWebhookPayload payload) {
        String normalizedBranch = this.normalizeBranchNameOrEmptyString(payload);
        String normalizedTag = this.normalizeTagNameOrEmptyString(payload);
        StringBuilder info = new StringBuilder();
        info.append("   ref\n      -> $GWBT_REF            : ").append(payload.getRef()).append("\n");
        info.append("      -> $GWBT_TAG            : ").append(normalizedTag).append("\n");
        info.append("      -> $GWBT_BRANCH         : ").append(normalizedBranch).append("\n\n");
        info.append("   before\n      -> $GWBT_COMMIT_BEFORE  : ").append(payload.getBefore()).append("\n\n");
        info.append("   after\n      -> $GWBT_COMMIT_AFTER   : ").append(payload.getAfter()).append("\n\n");
        info.append("   repository.clone_url\n      -> $GWBT_REPO_CLONE_URL : ").append(payload.getRepository().getClone_url()).append("\n\n");
        info.append("   repository.html_url\n      -> $GWBT_REPO_HTML_URL  : ").append(payload.getRepository().getHtml_url()).append("\n\n");
        info.append("   repository.full_name\n      -> $GWBT_REPO_FULL_NAME : ").append(payload.getRepository().getFull_name()).append("\n\n");
        info.append("   repository.name\n      -> $GWBT_REPO_NAME      : ").append(payload.getRepository().getName()).append("\n\n");
        this.envVarInfo = info.toString();
        this.environmentVariables.put("GWBT_REF", payload.getRef());
        this.environmentVariables.put("GWBT_TAG", normalizedTag);
        this.environmentVariables.put("GWBT_BRANCH", normalizedBranch);
        this.environmentVariables.put("GWBT_COMMIT_BEFORE", payload.getBefore());
        this.environmentVariables.put("GWBT_COMMIT_AFTER", payload.getAfter());
        this.environmentVariables.put("GWBT_REPO_CLONE_URL", payload.getRepository().getClone_url());
        this.environmentVariables.put("GWBT_REPO_HTML_URL", payload.getRepository().getHtml_url());
        this.environmentVariables.put("GWBT_REPO_FULL_NAME", payload.getRepository().getFull_name());
        this.environmentVariables.put("GWBT_REPO_NAME", payload.getRepository().getName());
    }

    /*
    * converts "refs/heads/develop" to "develop"
    */
    private String normalizeBranchNameOrEmptyString(GithubWebhookPayload payload) {
    //        if (branchname != null && branchname.startsWith("refs/heads/")) {
    //            return branchname.replace("refs/heads/", "");
    //        }
    //        return "";
    return Optional.ofNullable(payload)
        .filter(hook -> hook.getRef_type().equals("branch"))
        .map(GithubWebhookPayload::getRef)
        .orElse("");
    }

    /*
    * converts "refs/tags/1.0.0" to "1.0.0"
    */
    private String normalizeTagNameOrEmptyString(GithubWebhookPayload payload) {
    //        if (tagname != null && tagname.startsWith("refs/tags/")) {
    //            return tagname.replace("refs/tags/", "");
    //        }
    //        return "";
    return Optional.ofNullable(payload)
        .filter(hook -> hook.getRef_type().equals("tag"))
        .map(GithubWebhookPayload::getRef)
        .orElse("");
    }

    protected String getEnvVarInfo() {
        return this.envVarInfo;
    }


    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "EnvironmentContributionAction";
    }

    public String getUrlName() {
        return "EnvironmentContributionAction";
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

    /**
     * Since WorkflowJob does not support EnvironmentContributionAction yet,
     * we need a ParametersAction filled with List ParameterValue
     * See: https://github.com/jenkinsci/workflow-job-plugin/blob/124b171b76394728f9c8504829cf6857abc8bdb5/src/main/java/org/jenkinsci/plugins/workflow/job/WorkflowRun.java#L435
     */
    public ParametersAction transform() {
        List<ParameterValue> paramValues = new ArrayList<>();
        List<String> safeParams = new ArrayList<>();
        for (Map.Entry<String, String> envVar : environmentVariables.entrySet()) {
            paramValues.add(new StringParameterValue(envVar.getKey(), envVar.getValue(), envVar.getValue()));
            safeParams.add(envVar.getKey());
        }
        return new ParametersAction(paramValues, safeParams);
    }
}
