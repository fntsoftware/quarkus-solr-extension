package de.fnt.solr.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.solr")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SolrConnectionConfig {
    /**
     * The URL to the Solr server instance
     *
     * @return
     */
    String url();
}
