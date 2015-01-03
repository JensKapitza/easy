package de.back2heaven.easy.net;

import java.util.concurrent.ExecutorService;

public abstract class AbstractLayer implements Runnable {

	ExecutorService service;

	public ExecutorService getService() {
		return service;
	}

	public void setService(ExecutorService service) {
		this.service = service;
	}
}
