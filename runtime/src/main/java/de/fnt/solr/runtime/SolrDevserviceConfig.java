package de.fnt.solr.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.solr.devservices")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface SolrDevserviceConfig {
    /**
     * Enable the Solr dev service
     *
     * @return
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Which Core the devservice should create
     *
     * @return
     */
    String core();

    /**
     * Which Solr version to use
     *
     * @return
     */
    @WithDefault("9.6.1")
    String version();
}
