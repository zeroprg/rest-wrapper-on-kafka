package ca.bloberry.mobile.core.kafka.restclient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.bloberry.mobile.core.kafka.request_reply_util.CompletableFutureReplyingKafkaOperations;

@Component
public class ResponseObjectFacadeImpl1 implements ResponseObjectFacade<RequestObject, ResponseObject> {

  @Autowired
  private CompletableFutureReplyingKafkaOperations<String, RequestObject, ResponseObject> requestReplyKafkaTemplate;

  @Value("${kafka.topic.request}")
  private String requestTopic;

  @Override
  public ResponseObject getObject(RequestObject requestObject) {
    try {
      return getObjectAsync(requestObject).get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Failed to get Response Object", e);
    }
  }

  @Override
  public CompletableFuture<ResponseObject> getObjectAsync(RequestObject requestObject) {
    return requestReplyKafkaTemplate.requestReply(requestTopic, requestObject);
  }

}
