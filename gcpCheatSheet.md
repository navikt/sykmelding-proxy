# How to GCP + Kubernetes
Make sure you have set up [naisdevice](https://doc.nais.io/device) and are connected to the clusters, and have installed gcloud according to the [documentation](https://doc.nais.io/basics/access#authenticate-kubectl).

To see all clusters
```bash
kubectl config view
```

Change to gcp (or similarly for prod-gcp)
```bash
kubectl config use-context dev-gcp
```

The kubernetes clusters in GCP uses `namespaces`, where each team gets their own namespace. The namespace encapsulates the environment in which the teams applications run. To check if your team have a namespace:
```bash
kubectl get namespaces | grep <your-team>
```

To set the default namespace for the cluster, such that you can execute commands in the right context
```bash
# Make sure you're in the right cluster
Kubectl config current-context

# Set the default namespace
kubectl config set-context --current --namespace=<insert-namespace-name-here>

# Check the default namespace. Should only be <your-team>
kubectl config view --minify | grep namespace:
```

Namespaces allow the team to set kubernetes secrets to the namespace allowing every app running in the namespace to import secrets as environment variables. The guide at [nais docs](https://doc.nais.io/addons/secrets) explains the details.

To check if the the secret is available within you application pod
```bash
# Get pods from your namespace
kubectl get pods | grep <application-name>

# Look for the secret enbedded as an environment variable
kubectl exec <pod-id> env | grep <key-of-your-secret>
```