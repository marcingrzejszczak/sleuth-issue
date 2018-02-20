package org.springframework.sample;


import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import brave.Tracing;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
@SpringBootApplication
public class Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  public static void main(String[] args) {
    SpringApplication.run(Server.class);
  }

  @Bean
  RouterFunction<ServerResponse> handlers(GetGoogle getGoogle) {
    return route(GET("/noFlatMap"), request -> {
      LOGGER.info("noFlatMap");
      Flux<Integer> one = getGoogle.getAll().map(string -> string.length());
      return ServerResponse.ok().body(one, Integer.class);
    }).andRoute(GET("/withFlatMap"), request -> {
      LOGGER.info("withFlatMap");
      Flux<Integer> one = getGoogle.getAll().map(string -> string.length());
      Flux<Integer> response = one.flatMap(size -> getGoogle.getAll()
              .doOnEach(sig -> LOGGER.info(sig.getContext().toString())))
              .map(string -> {
                LOGGER.info("WHATEVER YEAH");
                return string.length();
              });
      return ServerResponse.ok().body(response, Integer.class);
    });
  }

  @Bean
   WebClient webClient() {
    return WebClient.create("http://google.de");
  }

  private static final String SLEUTH_TRACE_REACTOR_KEY = Server.class.getName();

  @Autowired Tracing tracing;
  @Autowired BeanFactory beanFactory;

  @PostConstruct
  public void setupHooks() {
    Hooks.onEachOperator(SLEUTH_TRACE_REACTOR_KEY, ReactorSleuth.spanOperator(this.tracing));
  }

  @PreDestroy
  public void cleanupHooks() {
    Hooks.resetOnEachOperator(SLEUTH_TRACE_REACTOR_KEY);
  }
}
