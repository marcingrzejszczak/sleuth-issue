package org.springframework.sample;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

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
      Flux<Integer> response = one.flatMap(size -> getGoogle.getAll()).map(string -> string.length());
      return ServerResponse.ok().body(response, Integer.class);
    });
  }

}
