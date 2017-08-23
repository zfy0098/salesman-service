package com.rhjf.salesman.service.util.auth;

import java.io.Serializable;

public class HttpSendResult implements Serializable {
	private static final long serialVersionUID = 3612208038316088287L;

	/**
	 * 
	 */
	private int status = -1;

	/**
	 * 
	 */
	private String responseBody;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
}
