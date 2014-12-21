package de.back2heaven.easy.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {

	private int maxClients = 10;
	private AtomicInteger currentOpen = new AtomicInteger();
	private Exception error = new Exception();
	private int port = 3279; // easy ;)
	private ServerSocket socket;

	@Override
	public void run() {
		try {
			socket = new ServerSocket(port, Runtime.getRuntime()
					.availableProcessors());
		} catch (IOException e) {
			error.addSuppressed(e);
		}

		while (socket != null && !socket.isClosed()) {
			try {
				Socket client = socket.accept();

				Connector connectionHandler = new Connector(client,currentOpen);
				// submit to pool
				CompletableFuture.runAsync(connectionHandler);
				CompletableFuture.runAsync(new LayerOne(connectionHandler));

				while (maxClients == currentOpen.incrementAndGet()) {
					synchronized (currentOpen) {
						try {
							currentOpen.wait();
						} catch (InterruptedException e) {
							error.addSuppressed(e);
							if (Thread.interrupted()) {
								break;
							}
						}
					}
				}
			} catch (IOException e) {
				error.addSuppressed(e);
			}
		}
	}

}
