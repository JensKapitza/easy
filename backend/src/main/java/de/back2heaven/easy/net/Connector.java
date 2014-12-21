package de.back2heaven.easy.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Connector implements Runnable {

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

	public Connector(Socket client, AtomicInteger currentOpen) {
		connection = client;
		ip = null; // ensure no auto connect will work!
		countDown = currentOpen;
	}

	@Override
	public void run() {
		runner = Thread.currentThread();
		if (connection != null && ip == null) {
			// start connection
			try {
				connection = new Socket(InetAddress.getByName(ip), port);
				in = connection.getInputStream();
				out = connection.getOutputStream();
			} catch (IOException e) {
				connection = null;
				error.addSuppressed(e);
			}
		}

		long last = System.currentTimeMillis();
		updateActionIfNotNull();

		while (connection != null && (lastAction.get() + timeShift) > last) {
			last = System.currentTimeMillis();
			try {
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
					in.close();
				}
			} catch (IOException e) {

				error.addSuppressed(e);
			}
			try {
				if (out != null) {
					out.close();
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

		in = null;
		out = null;
		// throw away this connection
		countDown.decrementAndGet();
		synchronized (countDown) {
			countDown.notify(); // wakeup server
		}

	}

	public byte[] read(int bytes) {
		updateActionIfNotNull();
		byte[] data = new byte[bytes];
		if (in != null) {
			try {
				int r = -1;
				synchronized (in) {
					r = in.read(data);
				}
				if (r == -1) {
					return new byte[0]; // indicate no data
				}
				if (r == bytes) {
					return data;
				}
				if (r > bytes) {
					throw new IOException("bad error occurs");
				}
				if (r < bytes) {
					byte[] n = new byte[r];
					System.arraycopy(data, 0, n, 0, r);
					return n;
				}

			} catch (IOException e) {
				error.addSuppressed(e);
				destroy();
			} finally {
				updateActionIfNotNull();
			}
		}

		return null;
	}

	private void updateActionIfNotNull() {
		if (lastAction.get() != 0) {
			lastAction.set(System.currentTimeMillis());
		}
	}

	public void write(byte data) {
		write(new byte[] { data });
	}

	public void write(byte[] data) {
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
	}

	public byte read() {
		byte[] d = read(1);
		if (d != null && d.length == 1) {
			return d[0];
		}
		return 0;
	}

	public void destroy() {
		lastAction.set(0);
		runner.interrupt();
	}

}
