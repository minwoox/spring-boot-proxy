package example.springframework.boot.proxy;

import javax.inject.Named;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;

/**
 * A reverse proxy service.
 */
@Named
public class ProxyService {

    private final HttpClient httpClient;

    ProxyService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Get("/hello/{name}")
    public HttpResponse hello(@Param("name") String name) {
        final HttpHeaders headers = HttpHeaders.of(HttpMethod.GET, "/hello/" + name);
        // Add additional headers
        return httpClient.execute(headers);
    }
}
