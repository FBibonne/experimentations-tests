package fr.insee.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jdbc.store.JdbcMessageStore;
import org.springframework.integration.store.MessageStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App
{

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Value("${fr.insee.test.proxy.host:#{null}}")
    private Optional<String> proxyHost;

    @Value("${fr.insee.test.proxy.port:8080}")
    private int proxyPort;
    private boolean runned=false;
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }

    @Bean
    IntegrationFlow readerFlow(MessageStore messageStore){
       //https://stackoverflow.com/questions/60690354/how-to-stop-polling-after-a-message-is-received-spring-integration
        return IntegrationFlows.from(new MessageSource<String>() {
                    @Override
                    public Message<String> receive() {
                        if (!runned) {
                            runned = true;
                            var httpClientBuilder = HttpClient.newBuilder();
                            proxyHost.ifPresent(s -> httpClientBuilder.proxy(ProxySelector.of(new InetSocketAddress(s, proxyPort))));
                            var httpClient = httpClientBuilder.build();
                            var request = HttpRequest.newBuilder().uri(URI.create("https://api.chucknorris.io/jokes/random")).build();
                            try {
                                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                                return MessageBuilder.withPayload(response.body()).build();
                            } catch (IOException | InterruptedException e) {
                                logger.error("Error while fetching external WS", e);
                                return MessageBuilder.withPayload(e.getClass()+" : "+e.getMessage()).build();
                            }
                        } else {
                            return null;
                        }
                    }
                })
                .claimCheckIn(messageStore)
                .handle(System.out::println)
                .get();
    }

    @Bean
    public MessageStore messageStore(DataSource dataSource) {
        return new JdbcMessageStore(dataSource);
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
     * 3. idempotence =>
     * 4. Transactions distribuées
     * 5. asynchrone (juste pour savoir comment on fait) ?
     * 6. delayer (pour le DelayScheduling du CommandStore)
     * @return
     */
    //@Bean
    IntegrationFlow readAndPersistFa(){
        return null;
    }



}
