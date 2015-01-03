package de.back2heaven.easy.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Connector extends Thread {

	private Exception error = new Exception();
	private String ip;
	private int port = 3279; // easy ;)
	private Socket connection;
	private AtomicLong lastAction = new AtomicLong();
	private long timeShift = 2000;
	private AtomicInteger countDown = new AtomicInteger();

	private Thread runner;

	private InputStream in;
	private OutputStream out;

	public Connector(String ip) {
		this.ip = ip;
	}

	public Connector(Socket client, AtomicInteger currentOpen) {
		connection = client;
		ip = null; // ensure no auto connect will work!
		countDown = currentOpen;
	}

	@Override
	public void run() {
		runner = Thread.currentThread();
		try {
			if (connection == null && ip != null) {
				// start connection if needed
				connection = new Socket(InetAddress.getByName(ip), port);
			}
			if (connection != null) {
				in = connection.getInputStream();
				out = connection.getOutputStream();
			}
		} catch (IOException e) {
			connection = null;
			error.addSuppressed(e);
		}
		long last = System.currentTimeMillis();
		lastAction.set(last);
		updateActionIfNotNull();

		while (connection != null && (lastAction.get() + timeShift) > last) {
			last = System.currentTimeMillis();
			try {
				// wakeup waiters
				synchronized (this) {
					notifyAll();
				}

				Thread.sleep(timeShift);
			} catch (InterruptedException e) {
				if (Thread.interrupted()) {
					break;
				} else {
					error.addSuppressed(e);
				}
			}
		}
		// we are idle and will close now.

		if (connection != null) {
			try {
				if (in != null) {
					synchronized (in) { // last read
						in.close();
					}
				}
			} catch (IOException e) {

				error.addSuppressed(e);
			}
			try {
				if (out != null) {
					synchronized (out) { // write out last data
						out.close();
					}
				}
			} catch (IOException e) {

				error.addSuppressed(e);
			}
			try {
				connection.close();
			} catch (IOException e) {
				error.addSuppressed(e);
			}
		}
		connection = null; // now the connection is lost
		runner = null;
		in = null;
		out = null;
		// throw away this connection
		countDown.decrementAndGet();
		synchronized (countDown) {
			countDown.notify(); // wakeup server
		}

	}

	public Connector read(int bytes, Consumer<byte[]> consumer) {
		updateActionIfNotNull();
		byte[] data = new byte[bytes];
		if (in != null) {
			try {
				int r = -1;
				synchronized (in) {
					r = in.read(data);
				}
				if (r != -1) {
					if (r < bytes) {
						byte[] n = new byte[r];
						System.arraycopy(data, 0, n, 0, r);
						data = n;
					}
					consumer.accept(data);
				}

			} catch (IOException e) {
				error.addSuppressed(e);
				destroy();
			} finally {
				updateActionIfNotNull();
			}
		}
		return this;
	}

	private void updateActionIfNotNull() {
		if (lastAction.get() != 0) {
			lastAction.set(System.currentTimeMillis());
		}
	}

	public Connector write(byte[] data) {
		if (out != null) {
			updateActionIfNotNull();

			try {
				synchronized (out) {
					out.write(data);
				}
			} catch (IOException e) {
				error.addSuppressed(e);
				destroy();
			}
			updateActionIfNotNull();
		}
		return this;
	}

	public void destroy() {
		if (runner != null) {
			// nur wenn noch nicht tot
			lastAction.set(0);
			runner.interrupt();
		}
	}

	public void shutdown() {
		destroy();
	}

	public synchronized void connect() {
		connect(1000);
	}

	public synchronized void connect(long timeout) {
		start();
		long waiting = 0;
		long stand = 5000;
		while (in == null && timeout > waiting) {
			try {
				wait(stand);
				waiting += stand;
			} catch (InterruptedException e) {
				error.addSuppressed(e);
				if (Thread.interrupted()) {
					break;

				}
			}
		}
	}
}
