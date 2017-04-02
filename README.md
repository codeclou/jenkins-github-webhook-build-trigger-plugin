# jenkins-github-webhook-build-trigger-plugin

Trigger Jenkins Jobs via GitHub Webhooks and provide Webhook Payload Information as Environment Variables inside your Job.

-----

&nbsp;

### Is this for me?

If you can agree with all statements, then this is for you.

 * [Bash](http://tiswww.case.edu/php/chet/bash/bashtop.html) is love. Bash is live.
 * Using the [git commandline client](https://git-scm.com/book/en/v2/Getting-Started-The-Command-Line) in Jenkins Jobs:
   * gives me the control I want and need.
   * replaces all other mostly defunct and/or bloated Git Plugins.
 * I only use GitHub.com for my repositorys.
 * I only 'git clone' via `https://` URLs.
 * I work with git branches and tags.
 * I want my Jenkins jobs triggered at every push (branch or tag).
 * I want my Jenkins jobs triggered exclusively via GitHub Webhook Push Events.
 * I use Linux to run Jenkins.
 * I want my Jenkins jobs triggered automatically by convention of configuration job naming.

Ok. Still here?! Then this might be for you :bowtie:

-----

&nbsp;

### How it works in three sentences

  * It parses the actual GitHub Webhook JSON Payload and extracts its information.
  * It triggers all Jenkins jobs matching `{repositoryOwner}---{repositoryName}.*`
  * It injects Environment Variables into the job run for you to determine what branch and revision is to clone.


-----

&nbsp;

### How to build the plugin

Jave Oracle Java 8 and Apache Maven 3 installed. And then build like this:

```bash
git clone https://github.com/codeclou/jenkins-github-webhook-build-trigger-plugin.git
cd jenkins-github-webhook-build-trigger-plugin
mvn clean
mvn compile
mvn hpi:hpi
```

Now you should have a file called `./target/github-webhook-notifier-plugin.hpi` which
you can upload manually to Jenkins under 'Manage Plugins' → 'Advanced' → 'Upload Plugin'.

-----

&nbsp;

### Why?

I needed something that forcefully triggers my Jenkins Jobs by passing the actual Git Commit ID.

The default behaviour of existing plugins is to "decide if it needs to rebuild". Which leads
to a state where if your job just build your `master` Branch and you create a tag of off the `master` branch
and push that tag, that the jenkins job will not be triggered, since the revisions are equal.
 

### NOTE

When using matrix-based security the 'Anonymous' user needs 'Job' `build,discover,read` rights.




### GitHub Webhook Configuration

 * **Payload URL**
   * `https://jenkins.foo/jenkins/github-webhook-notifier/receive`
   * Note: The endpoint can be called without authentication.
 * **Content type**
   * `application/json`
 * **Secret**
   * Choose a good secret, at best a random sha512 hash
 * **Which events ...**
   * Just the `push` event

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-notifier-plugin/img/github-webhook-settings.png" width="80%"></p>


### Jenkins Job Configuration


<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-notifier-plugin/img/jenkins-source-code-management.png" width="80%"></p>

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-notifier-plugin/img/jenkins-build-trigger.png" width="80%"></p>
	

### Example Trigger

We can see in the Response Tab of a GitHub Webhook Delivery that we notify the Git-Plugin with a specific sha1 commit id.

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-notifier-plugin/img/webhook-specific-commit-id.png" width="80%"></p>

And that triggers this exact id for the exact branch/tag that has been sent by the Webhook as JSON payload.

<p align="center"><img src="https://codeclou.github.io/jenkins-github-webhook-notifier-plugin/img/jenkins-build-by-commit-id.png" width="80%"></p>



-----

&nbsp;

### License

[MIT](./LICENSE) © [Bernhard Grünewaldt](https://github.com/clouless)
