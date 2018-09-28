package example.springframework.boot.proxy;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;

import javax.inject.Named;

import org.springframework.context.annotation.Bean;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.HttpClientBuilder;
import com.linecorp.armeria.client.endpoint.EndpointGroupRegistry;
import com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy;
import com.linecorp.armeria.client.endpoint.StaticEndpointGroup;

@Named
public class ProxyClient {

    private BackendInfo backendInfo;

    ProxyClient(BackendInfo backendInfo) {
        this.backendInfo = backendInfo;
    }

    @Bean
    HttpClient httpClient() {
        final List<Endpoint> endpoints = backendInfo.getPorts().stream()
                                                    .map(port -> Endpoint.of(port.getHost(), port.getPort()))
                                                    .collect(toImmutableList());
        final StaticEndpointGroup group = new StaticEndpointGroup(endpoints);
        EndpointGroupRegistry.register("backend", group, EndpointSelectionStrategy.WEIGHTED_ROUND_ROBIN);
        return new HttpClientBuilder("http://group:backend/")
                //.decorator(RetryingHttpClient.newDecorator(RetryStrategy.onServerErrorStatus()))
                //.decorator(new LoggingClientBuilder().newDecorator())
                .build();
    }
}
