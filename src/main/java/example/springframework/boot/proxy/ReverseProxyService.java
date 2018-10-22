package example.springframework.boot.proxy;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Service;
import com.linecorp.armeria.server.ServiceRequestContext;
import io.netty.util.AsciiString;

import java.net.InetSocketAddress;

import static java.util.Objects.requireNonNull;

public class ReverseProxyService implements Service<HttpRequest, HttpResponse> {

    /**
     * {@code "forwarded"}.
     */
    public static final AsciiString FORWARDED = AsciiString.cached("forwarded");

    private final HttpClient client;

    public ReverseProxyService(HttpClient client) {
        this.client = requireNonNull(client, "client");
    }

    @Override
    public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
        final InetSocketAddress raddr = ctx.remoteAddress();
        req.headers().add(FORWARDED, raddr.getAddress().getHostAddress());
        return client.execute(req);
    }
}
