package org.springframework.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GetGoogle {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetGoogle.class);

    private final WebClient webClient;

    public GetGoogle(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> get(Integer someParameterNotUsedNow){
        LOGGER.info("getting for paramtere {}", someParameterNotUsedNow);
        return webClient
                .method(HttpMethod.GET)
                .retrieve().bodyToMono(String.class);
    }

    public Flux<String> getAll(){
        LOGGER.info("Before merge");
        Flux<String> merge = Flux.merge(get(1), get(2), get(3));
        LOGGER.info("after merge");
        return merge;
    }


}
