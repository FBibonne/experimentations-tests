package fr.insee.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Date;

import static java.time.LocalTime.now;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }

    @Bean
    IntegrationFlow readerFlow(){
        return IntegrationFlows.fromSupplier(()->{
            var httpClient = HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr",8080))).build();
            var request = HttpRequest.newBuilder().uri(URI.create("https://api.chucknorris.io/jokes/random")).build();
            try {
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (IOException | InterruptedException e) {
                return e.getMessage();
            }
        },
                        poller -> poller.poller(pm -> pm.trigger(context -> context.lastActualExecutionTime()==null ? Date.from(Instant.now().plusSeconds(1)) :null))
                )
                .handle(System.out::println)
                .get();
    }

    /**
     * Lire et persister une FA :
     * - lire un seul json depuis le WS
     * - créer un objet depuis le json
     * - faire un post vers autre WS
     *
     * COntraintes :
     * 1. persister le message (command store)
     * 2. retry sur ws si pas de réponse
     * 3. idempotence
     * 4. asynchrone (juste pour savoir comment on fait) ?
     * @return
     */
    //@Bean
    IntegrationFlow readAndPersistFa(){
        return null;
    }



}
