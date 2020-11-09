package ca.bloberry.mobile.core.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import ca.bloberry.mobile.core.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;
import ca.bloberry.mobile.core.kafka.request_reply_util.CompletableFutureReplyingKafkaTemplate;
import ca.bloberry.mobile.core.kafka.restclient.RequestObject;
import ca.bloberry.mobile.core.kafka.restclient.ResponseObject;


@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Value("${kafka.topic.request}")
  private String requestTopic;

  @Value("${kafka.topic.reply}")
  private String replyTopic;

  @Value("${kafka.request-reply.timeout-ms}")
  private Long replyTimeout;

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    return props;
  }

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public ConsumerFactory<String, RequestObject> requestConsumerFactory() {
	    JsonDeserializer<RequestObject> jsonDeserializer = new JsonDeserializer<>();
	    jsonDeserializer.addTrustedPackages(ResponseObject.class.getPackage().getName());
    return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
    		jsonDeserializer);
  }

  @Bean
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, RequestObject>> requestReplyListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, RequestObject> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(requestConsumerFactory());
    factory.setReplyTemplate(replyTemplate());
    return factory;
  }

  @Bean
  public ProducerFactory<String, Object> replyProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, Object> replyTemplate() {
    return new KafkaTemplate<>(replyProducerFactory());
  }

  @Bean
  public ConsumerRestCaller consumerRestCaller() {
    return new ConsumerRestCaller();
  }

  
  @Bean
  public CompletableFutureReplyingKafkaOperations<String, RequestObject, ResponseObject> replyKafkaTemplate() {
    CompletableFutureReplyingKafkaTemplate<String, RequestObject, ResponseObject> requestReplyKafkaTemplate =
        new CompletableFutureReplyingKafkaTemplate<>(requestProducerFactory(),
            replyListenerContainer());
    requestReplyKafkaTemplate.setDefaultTopic(requestTopic);
    requestReplyKafkaTemplate.setReplyTimeout(replyTimeout);
    return requestReplyKafkaTemplate;
  }

  @Bean
  public ProducerFactory<String, RequestObject> requestProducerFactory() {
	  JsonSerializer<RequestObject> jsonSerializer = new JsonSerializer<>();
	  //jsonSerializer. addTrustedPackages(RequestObject.class.getPackage().getName());
    return new DefaultKafkaProducerFactory<>(producerConfigs(), new StringSerializer(),
    		jsonSerializer);
  }

  @Bean
  public ConsumerFactory<String, ResponseObject> replyConsumerFactory() {
    JsonDeserializer<ResponseObject> jsonDeserializer = new JsonDeserializer<>();
    jsonDeserializer.addTrustedPackages(ResponseObject.class.getPackage().getName());
    return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
        jsonDeserializer);
  }

  @Bean
  public KafkaMessageListenerContainer<String, ResponseObject> replyListenerContainer() {
    ContainerProperties containerProperties = new ContainerProperties(replyTopic);
    return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
  }
  
  @Bean
  public KafkaAdmin admin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic requestTopic() {
    Map<String, String> configs = new HashMap<>();
    configs.put("retention.ms", replyTimeout.toString());
    return new NewTopic(requestTopic, 2, (short) 2).configs(configs);
  }
  
  @Bean
  public NewTopic replyTopic() {
    Map<String, String> configs = new HashMap<>();
    configs.put("retention.ms", replyTimeout.toString());
    return new NewTopic(replyTopic, 2, (short) 2).configs(configs);
  }
}
