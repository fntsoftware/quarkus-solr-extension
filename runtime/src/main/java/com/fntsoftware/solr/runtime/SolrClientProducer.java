package com.fntsoftware.solr.runtime;

import io.quarkus.arc.lookup.LookupUnlessProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SolrClientProducer {
    @Inject
    SolrConnectionConfig config;

    @Inject
    ManagedExecutor executor;

    @Produces
    @ApplicationScoped
    @LookupUnlessProperty(name = "quarkus.solr.enabled", stringValue = "false")
    public SolrClient getClient() throws SolrServerException, IOException {
        HttpJdkSolrClient.Builder builder = new HttpJdkSolrClient.Builder(config.url())
                .withExecutor(executor)
                .useHttp1_1(config.useHttp1_1())
                .withRequestTimeout(config.requestTimeout(), TimeUnit.MILLISECONDS)
                .withConnectionTimeout(config.connectionTimeout(), TimeUnit.MILLISECONDS)
                .withIdleTimeout(config.idleTimeout(), TimeUnit.MILLISECONDS)
                .withFollowRedirects(config.followRedirects());

        if (config.auth().isPresent()) {
            builder.withBasicAuthCredentials(config.auth().get().username(), config.auth().get().password());
        }
        if (config.defaultCollection().isPresent()) {
            builder.withDefaultCollection(config.defaultCollection().get());
        }

        SolrClient c = builder.build();
        c.ping();
        return c;
    }
}
