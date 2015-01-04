package de.back2heaven.easy.net.cert;

import java.io.IOException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

public interface PGPCertificate {
	PGPPrivateKey getPrivateKey();
	PGPPublicKey getPublicKey();

	byte[] encrypt(byte[] data);
	byte[] sign(byte[] data) throws IOException, PGPException;
	byte[] decrypt(byte[] data);
	byte[] check(byte[] data) throws InvalidSignature, IOException, PGPException;
	
}
