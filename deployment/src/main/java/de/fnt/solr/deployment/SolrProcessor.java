package de.fnt.solr.deployment;

import de.fnt.solr.runtime.SolrClientProducer;
import de.fnt.solr.runtime.SolrDevserviceConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.runtime.LaunchMode;
import org.eclipse.microprofile.config.ConfigProvider;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.statement.MultiArgsStatement;
import java.io.IOException;
import java.util.Map;
import java.util.function.BooleanSupplier;

class SolrProcessor {
    SolrDevserviceConfig config;
    static volatile DevServicesResultBuildItem.RunningDevService devService;

    private static final String FEATURE = "solr";

    @BuildStep
    public AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(SolrClientProducer.class);
    }

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = WantsSolrDevService.class)
    public DevServicesResultBuildItem createContainer(CuratedApplicationShutdownBuildItem closeBuildItem) {
        if (devService != null) {
            return null;
        }
        Runnable closeTask = () -> {
            if (devService != null) {
                try {
                    devService.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            devService = null;
        };
        closeBuildItem.addCloseTask(closeTask, false);
        ImageFromDockerfile image = new ImageFromDockerfile("quarkus/devservices/solr")
                .withFileFromClasspath(".", "solr").withDockerfileFromBuilder(builder -> {
                    builder.from("solr:" + config.version()).withStatement(
                            new MultiArgsStatement("COPY --chown=solr:solr", ".", "/var/solr/data/" + config.core()));
                });
        SolrContainer container = new SolrContainer(image);
        container.start();
        Map<String, String> props = Map.of("quarkus.solr.url", "http://" + container.getHost() + ":"
                + container.getMappedPort(container.getPort()) + "/solr/" + config.core());
        devService = new DevServicesResultBuildItem.RunningDevService(FEATURE, container.getContainerId(),
                container::close, props);
        return devService.toBuildItem();
    }

    static class WantsSolrDevService implements BooleanSupplier {
        LaunchMode launchMode;
        SolrDevserviceConfig config;

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
            super.configure();
            addExposedPort(PORT);
            waitingFor(Wait.forLogMessage(".*Started Server.*", 1));
        }
    }
}
