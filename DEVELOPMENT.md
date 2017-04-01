### Tests

```bash
mvn hpi:run

curl -X POST \
    -H "Content-Type: application/json" \
    -d @test-webhook-payload.json \
    http://localhost:8080/jenkins/plugin/github-webhook-notifier-plugin/receive
```
