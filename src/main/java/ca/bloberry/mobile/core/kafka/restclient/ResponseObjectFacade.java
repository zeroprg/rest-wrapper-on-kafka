package ca.bloberry.mobile.core.kafka.restclient;

import java.util.concurrent.CompletableFuture;

public interface ResponseObjectFacade<V,R> {

  R getObject(V request);

  CompletableFuture<R> getObjectAsync(V request);

}