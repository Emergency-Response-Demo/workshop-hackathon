package com.redhat.cajun.navy.incident.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResetRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:reset")
            .to("sql:delete from incident");
    }
}
