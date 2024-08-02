# Solrer

Quarkus extension with a provider for
the [Solrj](https://solr.apache.org/guide/solr/latest/deployment-guide/solrj.html) `SolrClient` and a devservice for the
Solr container.

## Configuration

The devservice is enabled by default in development mode. However, the name of the core the service should create in the
Solr instance needs to be set and the Solr version can be configured in your `application.properties`:

```properties
# enabled by default
quarkus.solr.devservices.enabled=true
# required
quarkus.solr.devservices.core=<your core name>
# default version is the latest stable one
quarkus.solr.devservices.version=9.6.1
```

Also, the extension expects the configuration for the Solr core in the `solr` directory in the resources of your
project.

### Prod

For production usage the configurations above do not matter. Only the URL of the Solr instance to connect to needs to be
provided:

```properties
quarkus.solr.url=https://mydomain.fun/solr/mycore
```

> In development mode this URL configuration is done automatically and filled with the host, port and core name of the
> devservice container

## Usage

The extension comes with a provider for a `SolrClient` bean, which is connected to the configured Solr
server. After that, follow
the [usage guide of SolrJ](https://solr.apache.org/guide/solr/latest/deployment-guide/solrj.html).

## Contribute

Fork, push changes to your fork, create PR to upstream (here) :)
