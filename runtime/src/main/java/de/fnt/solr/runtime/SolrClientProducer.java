package de.fnt.solr.runtime;

import io.quarkus.arc.lookup.LookupUnlessProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

@ApplicationScoped
public class SolrClientProducer {
    @ConfigProperty(name = "quarkus.solr.url")
    private String url;

    @Produces
    @ApplicationScoped
    @LookupUnlessProperty(name = "quarkus.solr.enabled", stringValue = "false")
    public SolrClient getClient() throws SolrServerException, IOException {
        SolrClient c = (new HttpJdkSolrClient.Builder(url)).build();
        c.ping();
        return c;
    }
}
