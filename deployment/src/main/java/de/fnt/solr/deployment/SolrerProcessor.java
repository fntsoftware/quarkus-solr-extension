package de.fnt.solr.deployment;

import de.fnt.solr.runtime.SolrClientProducer;
import de.fnt.solr.runtime.SolrerDevserviceConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.ConfigProvider;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.statement.MultiArgsStatement;

import java.util.Map;
import java.util.function.BooleanSupplier;

class SolrerProcessor {
    SolrerDevserviceConfig config;

    private static final String FEATURE = "solr";

    @BuildStep
    public AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(SolrClientProducer.class);
    }

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = WantsSolrDevService.class)
    public DevServicesResultBuildItem createContainer(LaunchModeBuildItem launchMode) {
        ImageFromDockerfile image = new ImageFromDockerfile().withFileFromClasspath(".", "solr")
                .withDockerfileFromBuilder(builder -> {
                    builder.from("solr:" + config.version()).withStatement(
                            new MultiArgsStatement("COPY --chown=solr:solr", ".", "/var/solr/data/" + config.core()));
                });
        SolrContainer container = new SolrContainer(image);
        container.start();
        Map<String, String> props = Map.of("quarkus.solr.url", "http://" + container.getHost() + ":"
                + container.getMappedPort(container.getPort()) + "/solr/" + config.core());
        return new DevServicesResultBuildItem.RunningDevService(FEATURE, container.getContainerId(), container::close,
                props).toBuildItem();
    }

    static class WantsSolrDevService implements BooleanSupplier {
        LaunchMode launchMode;
        SolrerDevserviceConfig config;

        public boolean getAsBoolean() {
            Boolean devServicesActive = ConfigProvider.getConfig().getValue("quarkus.devservices.enabled",
                    Boolean.class);
            return launchMode.isDevOrTest() && devServicesActive && config.enabled();
        }
    }

    private static class SolrContainer extends GenericContainer<SolrContainer> {
        static final int PORT = 8983;

        public SolrContainer(ImageFromDockerfile image) {
            super(image);
        }

        public int getPort() {
            return PORT;
        }

        @Override
        protected void configure() {
            addExposedPort(PORT);
            waitingFor(Wait.forLogMessage(".*Started Server.*", 1));
        }
    }
}
