package de.back2heaven.test.easy.net;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.back2heaven.easy.net.Connector;
import de.back2heaven.easy.net.Server;
import de.back2heaven.easy.net.cert.OIDGenerator;

public class ConnectTest {
	Server server;

	@Before
	public void serverUp() {
		server = new Server();
		server.start();
	}

	@Test
	public void tryConnect() throws IOException {
		Connector con = new Connector("localhost");
		con.connect();
		con.write(OIDGenerator.generate());
		con.write(new byte[] { 1 });
	}

	@After
	public void down() {
		server.shutdown();
	}
}
