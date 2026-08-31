# Repository access troubleshooting

The reported sandbox failure occurs before the repository is checked out:

```text
fatal: unable to access 'https://github.com/amirabbashashemi/n8n-coding-agent.git/': Failed to connect to github.com port 443
```

This is a network-connectivity failure, not a project build or source-code failure. The sandbox must be granted outbound HTTPS access to GitHub (including TCP port 443), or the repository must be made available through an approved mirror/cache before cloning is retried. No application code can remediate a failed Git transport connection that happens before checkout.
