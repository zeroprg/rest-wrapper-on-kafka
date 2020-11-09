package com.kafkaexample.demo;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import ca.bloberry.mobile.core.kafka.restclient.KafkaRestClient;
import ca.bloberry.mobile.core.kafka.restclient.RequestObject;
import ca.bloberry.mobile.core.kafka.restclient.ResponseObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;;

@RestController
public class TestController {

	@Autowired
	KafkaRestClient kafkaRestClient; 
	
	@RequestMapping("/")
	public String index() {
		return "This is tst controller to test mbo-orderservice-utils ";
	}

	
	@RequestMapping("/firstTest")
	public  DeferredResult<ResponseEntity<ResponseObject>> firstTest() {
		
		RequestObject<String> requestObject = new RequestObject<String>(); 
		requestObject.setContent( "this is first test!!!");
		
		DeferredResult<ResponseEntity<ResponseObject>> result = kafkaRestClient.getResponseObject(requestObject);
		
		return result; // "Greetings from mbo-orderservice-utils first test working! result=" + result.getResult();
	}

}