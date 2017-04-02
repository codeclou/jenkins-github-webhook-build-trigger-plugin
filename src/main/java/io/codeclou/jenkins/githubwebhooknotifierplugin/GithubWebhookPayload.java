/*
 * Licensed under MIT License
 * Copyright (c) 2017 Bernhard Grünewaldt
 */
package io.codeclou.jenkins.githubwebhooknotifierplugin;

/**
 * GitHub Webhook JSON Pojo with only the parts that are interesting for us.
 * See: https://developer.github.com/webhooks/#payloads
 */
public class GithubWebhookPayload {

    private String ref;
    private String before;
    private String after;
    private GithubWebhookPayloadRepository repository;

    public GithubWebhookPayload() {

    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public GithubWebhookPayloadRepository getRepository() {
        return repository;
    }

    public void setRepository(GithubWebhookPayloadRepository repository) {
        this.repository = repository;
    }

    public class GithubWebhookPayloadRepository {
        private String clone_url;
        private String html_url;
        private String name;
        private String full_name;

        public GithubWebhookPayloadRepository() {

        }

        public String getClone_url() {
            return clone_url;
        }

        public void setClone_url(String clone_url) {
            this.clone_url = clone_url;
        }

        public String getHtml_url() {
            return html_url;
        }

        public void setHtml_url(String html_url) {
            this.html_url = html_url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFull_name() {
            return full_name;
        }

        public void setFull_name(String full_name) {
            this.full_name = full_name;
        }
    }
}
