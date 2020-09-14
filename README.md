# sykmelding-proxy 🚦
Proxies for applications owned by team-sykmelding.

Uses [flex-proxy](https://github.com/navikt/flex-proxy) as a base docker image for each proxy application. The base image is an node/express proxy server

## How does it work? 
Read the [flex-proxy readme](https://github.com/navikt/flex-proxy). Each folder corresponds to one proxy application.
You need the following files withing the folder:
- Dockerfile
- nais-dev-gcp.yml
- routes.yaml

### Do you need to cross zones? 🛫
If the application crosses into FFS (on-premises) from GCP the requst needs to go through an [api-gw](https://github.com/navikt/api-management). Keep in mind that api-gw will be obsolete at some point, and that "old" applications in FSS will need to migrate to GCP.

TODO: deploy api-gw config to prod!