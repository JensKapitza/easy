package de.back2heaven.easy.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends Thread {

	private int maxClients = 10;
	private AtomicInteger currentOpen = new AtomicInteger();
	private Exception error = new Exception();
	private int port = 3279; // easy ;)
	private ServerSocket socket;

	private ExecutorService service = Executors.newWorkStealingPool();

	@Override
	public void run() {
		try {
			socket = new ServerSocket(port, Runtime.getRuntime()
					.availableProcessors());
		} catch (IOException e) {
			error.addSuppressed(e);
		}

		serverloop: while (socket != null && !socket.isClosed()
				&& !service.isShutdown()) {
			try {
				Socket client = socket.accept();

				Connector connectionHandler = new Connector(client, currentOpen);
				connectionHandler.connect();
				AbstractLayer layer = new LayerOne(connectionHandler);
				layer.setService(service);
				// submit to pool
				service.execute(layer);

				while (maxClients == currentOpen.incrementAndGet()) {
					synchronized (currentOpen) {
						try {
							currentOpen.wait();
						} catch (InterruptedException e) {
							error.addSuppressed(e);
							if (Thread.interrupted()) {
								break serverloop; // end up we are terminated
							}
						}
					}
				}
			} catch (SocketTimeoutException ex) {
				continue; // this is not bad
			} catch (IOException e) {
				error.addSuppressed(e);
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				error.addSuppressed(e);
			}
		}
		socket = null;
	}

	public Exception getError() {
		return error;
	}

	public void shutdown() {
		try {
			service.shutdown();
			boolean status = service.awaitTermination(5, TimeUnit.SECONDS);
			if (!status) {
				System.err.println("not all tasks reached the end");
			}
		} catch (InterruptedException e) {
			error.addSuppressed(e);
		}
		synchronized (currentOpen) {
			currentOpen.notify();
		}

		// kill the rest!
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				error.addSuppressed(e);
			}
		}
	}

}
