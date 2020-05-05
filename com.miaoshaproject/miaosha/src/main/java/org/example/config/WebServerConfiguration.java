package org.example.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.undertow.ConfigurableUndertowWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/***************************
 *Author:ct
 *Time:2020/4/16 23:43
 *Dec:定制化Tomcat配置
 ****************************/

//当spring容器内没有TomcatEmbeddedServletContainerFactory这个bean时，会把此bean加载进来
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {

        //使用对应工厂类提供的接口定制化我们的tomcat connector
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {

                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                protocol.setKeepAliveTimeout(30000);
                protocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
