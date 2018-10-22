package example.springframework.boot.proxy;

import static java.util.Objects.requireNonNull;

import java.time.Duration;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.HttpClientBuilder;
import com.linecorp.armeria.client.endpoint.EndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HttpHealthCheckedEndpointGroupBuilder;
import com.linecorp.armeria.client.retry.RetryStrategy;
import com.linecorp.armeria.client.retry.RetryingHttpClient;
import com.linecorp.armeria.common.SessionProtocol;

public class ProxyClientFactory {

    public static HttpClient of(EndpointGroup backendGroup) {
        requireNonNull(backendGroup, "backendGroup");

        EndpointGroupRegistry.register("backends", backendGroup,
                                       EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN);
        return new HttpClientBuilder("http://group:backends")
                .decorator(RetryingHttpClient.newDecorator(RetryStrategy.onUnprocessed()))
                .build();
    }

    public static HttpClient of(EndpointGroup backendGroup,
                                String healthCheckPath) throws InterruptedException {
        requireNonNull(backendGroup, "backendGroup");
        requireNonNull(healthCheckPath, "healthCheckPath");

        final HttpHealthCheckedEndpointGroup healthCheckedGroup =
                new HttpHealthCheckedEndpointGroupBuilder(backendGroup, healthCheckPath)
                        .protocol(SessionProtocol.HTTP)
                        .retryInterval(Duration.ofSeconds(3))
                        .build();

        // Wait until the initial health check is finished.
        healthCheckedGroup.awaitInitialEndpoints();

        return ProxyClientFactory.of(healthCheckedGroup);
    }

    private ProxyClientFactory() {}
}
