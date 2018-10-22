package example.springframework.boot.proxy;

import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.metric.MetricCollectingService;

import javax.net.ssl.SSLException;

import java.io.File;
import java.security.cert.CertificateException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A factory class which creates an Armeria {@link Server} instance.
 */
public final class ServerFactory {
    /**
     * Returns a new {@link Server} instance configured with annotated HTTP services.
     *
     * @param port the port that the server is to be bound to
     */
    public static Server of(int port) throws Exception {
        checkArgument(port >= 0 && port <= 65535, "port: %s (expected: 0-65535)");
        final ServerBuilder sb = new ServerBuilder();

        return sb.https(port)
                 .tlsSelfSigned() // Please use your certificate and like below
                 //.tls(new File("certificate.crt"), new File("private.key"), "myPassphrase")
                 .serviceUnder("/", new ReverseProxyService(null)
                         .decorate(MetricCollectingService.newDecorator(
                                 MeterIdPrefixFunction.ofDefault("proxy"))))

                 .build();
    }

    private ServerFactory() {}
}