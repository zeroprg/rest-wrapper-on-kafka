package ca.bloberry.mobile.core.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;

import ca.bloberry.mobile.core.kafka.restclient.RequestObject;
import ca.bloberry.mobile.core.kafka.restclient.ResponseObject;


public class ConsumerRestCaller {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRestCaller.class);

  @KafkaListener(topics = "${kafka.topic.request}", containerFactory = "requestReplyListenerContainerFactory")
  @SendTo()
  public ResponseObject<String> receive(RequestObject<String> requestObject) {
    LOGGER.info("received request for requestObject {} ", requestObject);
    // REST call  
    // Object response = ....
    ResponseObject<String> responseObject = new ResponseObject<String>();
    responseObject.setContent("This is response of first test  as String object...");
    LOGGER.info("sending reply {} ", responseObject);
    return responseObject;
  }
}
