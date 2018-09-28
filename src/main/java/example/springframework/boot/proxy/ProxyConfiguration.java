package example.springframework.boot.proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.AnnotatedServiceRegistrationBean;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

@Configuration
public class ProxyConfiguration {

    @Bean
    public AnnotatedServiceRegistrationBean proxy(ProxyService proxyService) {
        return new AnnotatedServiceRegistrationBean()
                .setServiceName("proxyService")
                .setPathPrefix("/")
                .setService(proxyService)
                .setDecorators(ImmutableList.of(LoggingService.newDecorator()));
    }

    @Bean
    public ArmeriaServerConfigurator ArmeriaServerConfigurator(GatewayService gatewayService) {
        return sb -> sb.serviceUnder("/gateway/", gatewayService);
    }
}
