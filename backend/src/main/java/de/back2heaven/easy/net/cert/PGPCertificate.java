package de.back2heaven.easy.net.cert;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

public interface PGPCertificate {
	PGPPrivateKey getPrivateKey();
	PGPPublicKey getPublicKey();

	byte[] crypt(byte[] data);
	byte[] sign(byte[] data);
	byte[] decrypt(byte[] data);
	byte[] check(byte[] data) throws InvalidSignature;
	
}
