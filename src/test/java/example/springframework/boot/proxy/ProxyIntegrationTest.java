package example.springframework.boot.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.testing.server.ServerRule;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@EnableAutoConfiguration
public class ProxyIntegrationTest {

    @Inject
    private BackendInfo backendInfo;

    @Rule
    public ServerRule backend1 = new ServerRule() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.http(backendInfo.getPorts().get(0).getPort());
            sb.service("/gateway/foo", (ctx, req) -> HttpResponse.of("Response from backend1"));

            sb.annotatedService("/", new Object() {
                @Get("/hello/{name}")
                public HttpResponse hello(@Param("name") String name) {
                    return HttpResponse.of("hello " + name + '!');
                }
            });
        }
    };

    @Rule
    public ServerRule backend2 = new ServerRule() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.http(backendInfo.getPorts().get(1).getPort());
            sb.service("/gateway/foo", (ctx, req) -> HttpResponse.of("Response from backend2"));

            sb.annotatedService("/", new Object() {
                @Get("/hello/{name}")
                public HttpResponse hello(@Param("name") String name) {
                    return HttpResponse.of("hello " + name + '!');
                }
            });
        }
    };

    @Test
    public void testGatewayService() throws Exception {
        final HttpClient client = HttpClient.of("http://localhost:8080");
        AggregatedHttpMessage res = client.get("/gateway/foo").aggregate().get();
        assertThat(res.content().toStringUtf8()).isEqualTo("Response from backend1");

        res = client.get("/gateway/foo").aggregate().get();
        assertThat(res.content().toStringUtf8()).isEqualTo("Response from backend2");
    }

    @Test
    public void restApiProxy() throws Exception {
        final HttpClient client = HttpClient.of("http://localhost:8080");
        AggregatedHttpMessage res = client.get("/hello/armeria").aggregate().get();
        assertThat(res.content().toStringUtf8()).isEqualTo("hello armeria!");
    }
}
