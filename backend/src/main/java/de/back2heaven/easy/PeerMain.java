package de.back2heaven.easy;

import de.back2heaven.easy.net.Server;

public class PeerMain {
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
}
