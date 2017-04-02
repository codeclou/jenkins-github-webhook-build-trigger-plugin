# jenkins-github-webhook-build-trigger-plugin



### What it does

  * It parses the actual GitHub Webhook JSON Payload
  * Extracts the Repository Clone URL and the Git Commit ID
  * It triggers the Git-Plugins notifyTrigger with the fixed commit id
  * That way tags and branches are build even if they have the same revision
    * (a git push of a tag has a unique commit id but the same revision)


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
