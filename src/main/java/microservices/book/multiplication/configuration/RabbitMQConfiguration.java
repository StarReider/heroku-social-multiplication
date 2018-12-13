package microservices.book.multiplication.configuration;

import static java.lang.System.getenv;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude=RabbitAutoConfiguration.class)
public class RabbitMQConfiguration {

	@Bean
	public TopicExchange multiplicationExchange(@Value("${multiplication.exchange}") final String exchangeName) {
		return new TopicExchange(exchangeName);
	}
	
	@Bean
	public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}
	
	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	
	@Bean
	 public ConnectionFactory connectionFactory() {
	     final URI rabbitMqUrl;
	     try {
	         rabbitMqUrl = new URI(getEnvOrThrow("CLOUDAMQP_URL"));
	     } catch (URISyntaxException e) {
	         throw new RuntimeException(e);
	     }

	     final CachingConnectionFactory factory = new CachingConnectionFactory();
	     factory.setUsername(rabbitMqUrl.getUserInfo().split(":")[0]);
	     factory.setPassword(rabbitMqUrl.getUserInfo().split(":")[1]);
	     factory.setHost(rabbitMqUrl.getHost());
	     factory.setPort(rabbitMqUrl.getPort());
	     factory.setVirtualHost(rabbitMqUrl.getPath().substring(1));

	     return factory;
	 }
	
	private static String getEnvOrThrow(String name) {
       final String env = getenv(name);
       if (env == null) {
           throw new IllegalStateException("Environment variable [" + name + "] is not set.");
       }
       return env;
   }
}