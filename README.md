# sykmelding-proxy 🚦
Proxies for applications owned by team-sykmelding.

Uses [flex-proxy](https://github.com/navikt/flex-proxy) as a base docker image for each proxy application. The base image is an node/express proxy server

## Zone crossing
Crossing between GCP and FSS requires the use of an [api-gw](https://github.com/navikt/api-management#add--update-request)