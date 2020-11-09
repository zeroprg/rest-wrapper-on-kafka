package ca.bloberry.mobile.core.kafka.restclient;

import java.util.concurrent.CompletableFuture;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;


@Component
public class KafkaRestClient {

  @Autowired
  private ResponseObjectFacade responseObjectFacade;

  public DeferredResult<ResponseEntity<ResponseObject>> getResponseObject(RequestObject requestObject) {
    DeferredResult<ResponseEntity<ResponseObject>> result = new DeferredResult<>();
    // get request object from queue
    CompletableFuture<ResponseObject> reply = responseObjectFacade.getObjectAsync(requestObject);
    
    reply.thenAccept(responseObject ->
      result.setResult(new ResponseEntity(responseObject, HttpStatus.OK))
    ).exceptionally(ex -> {    	
     // retry will be implement here	
      result.setErrorResult(new ApiException(HttpStatus.NOT_FOUND, ex.getCause().getMessage()));
      return null;
    });
    return result;
  }

/*
  @ExceptionHandler(ApiException.class)
  public final ResponseEntity<ErrorMessage> handleApiException(ApiException ex,
      WebRequest request) {
    HttpStatus status = ex.getStatus();
    ErrorMessage errorDetails = new ErrorMessage().timestamp(ex.getTimestamp())
        .status(status.value()).error(status.getReasonPhrase()).message(ex.getMessage());
    if (request instanceof ServletWebRequest) {
      ServletWebRequest servletWebRequest = (ServletWebRequest) request;
      HttpServletRequest servletRequest =
          servletWebRequest.getNativeRequest(HttpServletRequest.class);
      if (servletRequest != null) {
        errorDetails = errorDetails.path(servletRequest.getRequestURI());
      }
    }
    return new ResponseEntity<>(errorDetails, status);
  }
*/
}
