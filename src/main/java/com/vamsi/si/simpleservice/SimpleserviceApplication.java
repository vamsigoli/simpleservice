package com.vamsi.si.simpleservice;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.support.MessageBuilder;

@SpringBootApplication
//marking this annotation is important. spring integration magic happens through this annotation only.
public class SimpleserviceApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(SimpleserviceApplication.class, args);
        Thread.currentThread().join();
    }

    @Bean
    public ApplicationRunner runner(DirectChannel inboundChannel) {
        return event -> {
            var message = MessageBuilder.withPayload("World").build();
            inboundChannel.send(message);
        };
    }

    //channel is the MessageChannel in the EIP book. pipe in the pipe and filter style.
    //and implementation provided in handle is the filter
    @Bean
    public DirectChannelSpec inboundChannel() {
        //check the documentation change from 6.0 to 6.1
        //the repo by josh long in his spring integration video has .get method on the direct channel spec which was being sent
        //to the integration flow.
        https://docs.spring.io/spring-integration/reference/changes-6.0-6.1.html#x6.1-general
        /*The IntegrationComponentSpec.get() method has been deprecated with removal planned for the next version.
        Since IntegrationComponentSpec is a FactoryBean, its bean definition must stay as is without any target object resolutions.
        The Java DSL and the framework by itself will manage the IntegrationComponentSpec lifecycle. See Java DSL for more information.*/
        return MessageChannels.direct();
    }

    @Bean
    public IntegrationFlow outBoundFlow() {
        return //IntegrationFlow.from(inboundChannel())
                //we can use the bean method as well as --
                IntegrationFlow.from("inboundChannel")
                .handle(helloService(), "sayHello")
                //handle is same as having @ServiceActivator present on a method
                //https://docs.spring.io/spring-integration/reference/service-activator.html#service-activator-namespace
                /*
                public class SomeService {
                       @ServiceActivator(inputChannel = "exampleChannel")
                       public void exampleHandler(SomeData payload) {
                               ...
                       }
                }
                //when a message is sent to the exampleChannel, the exampleHandler method gets invoked.
                 */
                .handle((payload, headers) -> {
                    System.out.println(payload);
                    return null;
                })
                .get();
    }

    @Bean
    public HelloService helloService() {
        return new HelloService();
    }


}

class HelloService {
    public String sayHello(String name) {
        return "Hello " + name;
    }

}
