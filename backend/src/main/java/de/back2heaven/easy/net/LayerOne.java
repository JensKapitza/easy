package de.back2heaven.easy.net;

public class LayerOne extends AbstractLayer {

	private Connector connectionHandler;

	public LayerOne(Connector connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	@Override
	public void run() {
		// we need to update lastAction and read/write date
		// first we step of protocol

		connectionHandler.read(256, oid -> {
			System.out.println("oid");
			if (!checkOID(oid)) {
				connectionHandler.destroy();
			}

		}).read(1, mode -> {
			System.out.println("opt:");
			if (!checkMode(mode[0])) {
				connectionHandler.destroy();
			}
		});
		
		
		// now we can jump up the layers.

	}

	private boolean checkMode(byte mode) {
		if (mode != 0) {
			// check MODE TABLE
			// avoid communication with wrong modes
		}
		return false;
	}

	private boolean checkOID(byte[] oid) {
		// check if each byte is the same
		if (oid != null && oid.length == 256) {
			// check TABLE SQL etc?
			// all IDs should be there if they are permitted
			// if not then destroy the connection
			return true;
		}

		return false;
	}

}
