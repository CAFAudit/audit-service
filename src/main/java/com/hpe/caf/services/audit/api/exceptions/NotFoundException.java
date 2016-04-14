package com.hpe.caf.services.audit.api.exceptions;

public class NotFoundException extends Exception {
	public NotFoundException (String msg) {
		super(msg);
	}

	public NotFoundException (String msg, Throwable throwable) {
		super(msg, throwable);
	}
}
