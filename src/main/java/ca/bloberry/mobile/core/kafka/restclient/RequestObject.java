package ca.bloberry.mobile.core.kafka.restclient;

import java.io.Serializable;

/**
 *  This is simple response wrapper over response which will generialized in future
 */
public class RequestObject<K> implements Serializable {

	private static final long serialVersionUID = 1L;
	K content;
	public K getContent() {
		return content;
	}

	public void setContent(K content) {
		this.content = content;
	}
	
}
