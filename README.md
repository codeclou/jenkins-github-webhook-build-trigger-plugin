# Jenkins Github Webhook Build Trigger Plugin 

[![](https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/github-product-logo-jenkins-webhook.png)](https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin)

Trigger Jenkins Jobs via GitHub Webhooks and provide Webhook Payload Information as Environment Variables inside your Job.
This plugin is made by codeclou. We love [GitHub](https://github.com) and [Jenkins](https://jenkins.io/).

-----

&nbsp;

### Quickstart

* (1) Install the Pipeline plugins via the [Pipeline Bundle](https://plugins.jenkins.io/workflow-aggregator).
* (2) Download: [`github-webhook-notifier-plugin-1.2.0.hpi.zip`](https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin/releases/download/1.2.0/github-webhook-notifier-plugin-1.2.0.hpi.zip) and install the plugin.
* (3) In Jenkins global configuration define a "Webhook Secret" (see below)
* (4) Create a job called `{gitHubRepoOwner}---{repoName}` like e.g. `codeclou---myapp`
* (5) Configure a GitHub Webhook for push event. (see below)
* (6) Push a code change and job should be triggered

-----

&nbsp;

### Is this for me?

If you can agree with all statements, then this is for you.

 * [Bash](http://tiswww.case.edu/php/chet/bash/bashtop.html) is love. Bash is live.
 * Using the [git commandline client](https://git-scm.com/book/en/v2/Getting-Started-The-Command-Line) in Jenkins Jobs:
   * gives me the control I want and need.
   * replaces all other mostly defunct and/or bloated Git Plugins.
 * I only use GitHub.com for my repositories.
 * I only 'git clone' via `https://` URLs.
 * I work with git branches and tags.
 * I want my Jenkins jobs triggered at every push (branch or tag).
 * I want my Jenkins jobs triggered exclusively via GitHub Webhook Push Events.
 * I use Linux to run Jenkins.
 * I want my Jenkins jobs triggered automatically by convention over configuration job naming.

Ok. Still here?! Then this might be for you :bowtie:

-----

&nbsp;

### How it works in three sentences and one picture

  * Plugin REST Endpoint parses the actual GitHub Webhook JSON Payload and extracts its information.
  * It then triggers all Jenkins jobs matching `{repositoryOwner}---{repositoryName}.*`
  * Lastly it injects Environment Variables into the job run for you to determine what branch and revision is to clone.

![](https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/webhook-payload---with-overlays.png?v2)

-----

&nbsp;

### GitHub Webhook Configuration

This is how you need to configure the GitHub Webhook in your repository 'Settings'.

 * **Payload URL**
   * `https://jenkins/github-webhook-build-trigger/receive`
   * Note: 
     * The endpoint can be called without authentication.
 * **Content type**
   * `application/json`
 * **Secret**
   * Choose a good secret, at best a random sha512 hash. Use that secret for all webhooks of all your repositories.
 * **Which events ...**
   * Just the `push` event

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/github-webhook-settings---with-overlays.png?v4" width="80%"></p>


-----

&nbsp;

### Jenkins Global Configuration

You can configure the **Webhook Secret** Globally via 'Manage Jenkins' → 'Configure System' → 'Github Webhook Notifier Plugin'. If you do not configure it, HTTP 500 will be the response from the API.

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/jenkins-global-config---with-overlay.png" width="80%"></p>

&nbsp;

Concerning 'Manage Jenkins' → **'Configure Global Security'**, 'Prevent Cross Site Request Forgery exploits' can be enabled (the plugin will still work).
When using matrix-based security 'Anonymous' needs 'Job' → `build,discover,read` permissions.


-----

&nbsp;

### Jenkins Job Configuration

Configure your Jenkins Job like this so that it gets triggered by the Webhook events.

First of all the **naming conventions** is `{repositoryOwner}---{repositoryName}.*`. 
That means if your repository is `https://github.com/codeclou/test-webhook.git` then your job must be called
`codeclou---test-webhook`. You can have multiple jobs if you want for example a job that handles releases, just call it `codeclou---test-webhook-release`.

We do not use 'Source Code Management' and we do not need to specify some 'Build Triggers' since it is all done
magically by convention over configuration.

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/jenkins-job-config---with-overlays.png?v2" width="80%"></p>

&nbsp;

**Available Environment Variables from Webhook**

| Variable | Description | Example |
|----------|-------------|---------|
| `$GWBT_COMMIT_BEFORE` | `before` commit id as sha1 hash from Webhook Payload, specifying the commit revision the repository was in before the event happened.  | `3be1cb4b6b86533b5dab2b0083fa9fb8b401b430` or <br> `0000000000000000000000000000000000000000` if push event was a tag |
| `$GWBT_COMMIT_AFTER` | `after` commit id as sha1 hash from Webhook Payload, specifying the commit revision the repository is now in. Meaning the current revision. | `2c9522c9618864808eaaede8353dbeafb996c605`  or <br> `0000000000000000000000000000000000000000` if push event was deletion of a branch e.g. after pr merge |
| `$GWBT_REF` | `ref` from Webhook Payload representing the branch or tag that was pushed | `refs/heads/{branchname}` or <br> `refs/tags/{tagname}` |
| `$GWBT_TAG` | short tag name derived from `ref` and stripped of clutter. | When `ref` is `refs/tags/1.0.0` then it is `1.0.0`. <br>When `ref` is not a tag, it is empty! |
| `$GWBT_BRANCH` | short branch name derived from `ref` and stripped of clutter. | When `ref` is `refs/heads/master` then it is `master`. <br>When `ref` is not a branch, it is empty! |
| `$GWBT_REPO_CLONE_URL` | GitHub repository clone url. |  `https://github.com/{repoOwner}/{repoName}.git` <br> e.g. `https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin.git` |
| `$GWBT_REPO_HTML_URL` |  GitHub repository browser url. |   `https://github.com/{repoOwner}/{repoName}` <br> e.g. `https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin` |
| `$GWBT_REPO_FULL_NAME` | GitHub repository full name | `{repoOwner}/{repoName}` <br> e.g. `codeclou/jenkins-github-webhook-build-trigger-plugin` |
| `$GWBT_REPO_NAME` | GitHub repository full name |  `{repoName}` <br> e.g. `jenkins-github-webhook-build-trigger-plugin` |


&nbsp;

**Example Build Script Snippet**

```bash
#!/bin/bash

set -e

echo "GWBT_COMMIT_BEFORE:  $GWBT_COMMIT_BEFORE"
echo "GWBT_COMMIT_AFTER:   $GWBT_COMMIT_AFTER"
echo "GWBT_REF:            $GWBT_REF"
echo "GWBT_TAG:            $GWBT_TAG"
echo "GWBT_BRANCH:         $GWBT_BRANCH"
echo "GWBT_REPO_CLONE_URL: $GWBT_REPO_CLONE_URL"
echo "GWBT_REPO_HTML_URL:  $GWBT_REPO_HTML_URL"
echo "GWBT_REPO_FULL_NAME: $GWBT_REPO_FULL_NAME"
echo "GWBT_REPO_NAME:      $GWBT_REPO_NAME"

#
# Cleanup before run
#
rm -rf $WORKSPACE/\.git || true
rm -rf $WORKSPACE/* || true
cd $WORKSPACE

#
# Prevent manual Job starts
#
if [[ -z "$GWBT_COMMIT_AFTER" ]]
then
    echo "I DON'T WANT JOBS STARTED MANUALLY! ONLY VIA GITHUB WEBHOOK!"
    exit 1
fi

#
# Do not build "delete branch push event"
#
if [ "$GWBT_COMMIT_AFTER" == "0000000000000000000000000000000000000000" ]
then
    echo "DO NOT REACT ON BRANCH DELETE PUSHES"
    exit 0
fi

#
# Only Build Branches
#
if [ -z "$GWBT_BRANCH" ]
then
    echo "THIS PUSH IS NOT INSIDE A BRANCH. I DON'T LIKE IT!"
    exit 1
fi

#
# Clone specific branch
#
git clone --single-branch \
          --branch $GWBT_BRANCH \
          https://github.com/${GWBT_REPO_FULL_NAME}.git \
          source

#
# Switch to specific revision
#
cd source
git reset --hard $GWBT_COMMIT_AFTER

#
# Trigger build script inside cloned repository
#
bash jenkins.sh
```


&nbsp;

**Example Build Script Snippet for Cloning Private Repositories**

It is best to use [Personal Access Tokens](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/). 
Put a Global Environment Variable named `GITHUB_AUTH_TOKEN` in your Jenkins Configuration or specify at Job level.

Then you can clone a private repository like this:

```bash
#
# Clone specific branch
#
git clone --single-branch \
          --branch $GWBT_BRANCH \
          https://${GITHUB_AUTH_TOKEN}@github.com/${GWBT_REPO_FULL_NAME}.git \
          source
```

&nbsp;

**Usage with Pipeline Jobs**

Since version 1.1.0 [Pipeline Job Types](https://jenkins.io/doc/book/pipeline/) (NOT MultiBranch Pipeline) are supported.

Make sure you have at least the following Plugins installed.

:bangbang: **At best have the [Pipeline Bundle](https://plugins.jenkins.io/workflow-aggregator) installed** otherwise you might get `java.lang.ClassNotFoundException: org.jenkinsci.plugins.workflow.job.WorkflowJob` Exceptions of some sort.

```
Pipeline: Groovy
Pipeline: Job
Pipeline: API
Pipeline: Step API
Pipeline: Stage Step
Pipeline: Basic Steps
Pipeline: Model API
```

A simple **`Jenkinsfile`** could look like this:

```groovy
node {
  stage('foo') {
    sh 'git clone --single-branch --branch ${env.GWBT_BRANCH}  https://github.com/${env.GWBT_REPO_FULL_NAME}.git source'
    dir('source') {
      sh 'npm install'
    }
  }
}
```

&nbsp;

To **trigger build status of Pull Requests on GitHub.com** you can do this via the [GitHub Statuses REST API](https://developer.github.com/v3/repos/statuses/) and your personal access token.

```groovy
node {
  try {
    stage('inform github') {
      sh 'curl -X POST -H \'Authorization: token ' + env.GITHUB_AUTH_TOKEN + '\' ' +
         ' https://api.github.com/repos/${GWBT_REPO_FULL_NAME}/statuses/${GWBT_COMMIT_AFTER} ' +
         ' -d \'{ "state": "pending", "context": "Jenkins", "target_url": "' + env.BUILD_URL + '", "description": "The build has started" }\' '
    }
    stage('foo') {
      sh 'git clone --single-branch --branch ${env.GWBT_BRANCH}  https://github.com/${env.GWBT_REPO_FULL_NAME}.git source'
      dir('source') {
        sh 'npm install'
        sh 'npm build'
      }
    }
  } catch (Exception err) {
    currentBuild.result = 'FAILURE'
  } finally {
    if (currentBuild.result == 'FAILURE') {
      sh 'curl -X POST -H \'Authorization: token ' + env.GITHUB_AUTH_TOKEN + '\' ' +
         ' https://api.github.com/repos/${GWBT_REPO_FULL_NAME}/statuses/${GWBT_COMMIT_AFTER} ' +
         ' -d \'{ "state": "failure", "context": "Jenkins", "target_url": "' + env.BUILD_URL + '", "description": "The build has failed" }\' '
    } else {
      sh 'curl -X POST -H \'Authorization: token ' + env.GITHUB_AUTH_TOKEN + '\' ' +
         ' https://api.github.com/repos/${GWBT_REPO_FULL_NAME}/statuses/${GWBT_COMMIT_AFTER} ' +
         ' -d \'{ "state": "success", "context": "Jenkins", "target_url": "' + env.BUILD_URL + '", "description": "The build was a success" }\' '
    }
  }
}
```

A pull request will now get status updates for each commit.

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/pull-request-checks---with-overlays.png?v2" width="80%"></p>

You can further enforce that a status check has to be successful in order to merge a PR. Go to  via 'Settings' → 'Branches' → 'Add Branch protection rule'.

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/pull-request-checks-2---with-overlays.png?v2" width="80%"></p>

-----

&nbsp;




### Jenkins Job Example Triggered by Webhook Push

This is how it looks, when a Job gets triggered by a GitHub Webhook push.
 
<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-build-trigger-plugin/img/job-triggered---with-overlay.png" width="80%"></p>

&nbsp;

-----

&nbsp;

### Appendix

**Build Plugin**

Have Jave Oracle Java 8 and Apache Maven 3 installed. And then build like this:

```bash
git clone https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin.git
cd jenkins-github-webhook-build-trigger-plugin
mvn clean
mvn compile
mvn hpi:hpi
```

Now you should have a file called `./target/github-webhook-notifier-plugin.hpi` which
you can upload manually to Jenkins under 'Manage Plugins' → 'Advanced' → 'Upload Plugin'.


&nbsp;

&nbsp;

**What's the story behind it?**

I needed something that forcefully triggers my Jenkins Jobs by passing the actual git revision and branch or tag information.

The default behaviour of existing plugins is to receive the GitHub Webhook Payload, but 
only using the `after` commit id and "deciding if it needs to rebuild the job". 

Example: You are on your `master` Branch and you create a tag of off the `master` branch
and called `1.0.0`. When pushing `1.0.0` tag, the jenkins job will not trigger an actual build.
What happens? It will do some strange `git fetch` requests and comes to the result, that the revision
was already built with the previous push done by `master` branch. And partly he is right. 
Until further commits happen, the `master` branch has the same revision as the `1.0.0` tag.
But **I want tag pushes to trigger a build anyway**. And since I hate 'API-wrappers' of stuff,
I decided to create a single purpose tool that just passes the information of the webhook payload
through to the job. And it is the jobs logic that can now decide what to do.


&nbsp;


-----

&nbsp;

### License

[MIT](./LICENSE) © [Bernhard Grünewaldt](https://github.com/clouless)
