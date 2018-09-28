package example.springframework.boot.proxy;

import javax.inject.Named;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Service;
import com.linecorp.armeria.server.ServiceRequestContext;

/**
 * A proxy service that passes unmodified requests and responses.
 */
@Named
public class GatewayService implements Service<HttpRequest, HttpResponse> {

    private final HttpClient httpClient;

    GatewayService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
        return httpClient.execute(req);
    }
}
