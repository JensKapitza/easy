package de.back2heaven.easy.net.cert;

import java.io.IOException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

public class Certificate implements PGPCertificate  {
	private PGPCertificate cert;
	private String name;
	private byte[] oid;

	public Certificate(String name, byte[] oid, PGPCertificate cert) {
		this.name = name;
		this.oid = oid;
		this.cert = cert;
	}

	public byte[] encrypt(byte[] data) {
		return cert.encrypt(data);
	}

	public byte[] sign(byte[] data) throws IOException, PGPException {
		return cert.sign(data);
	}

	public byte[] decrypt(byte[] data) {
		return cert.decrypt(data);
	}

	public byte[] check(byte[] data) throws InvalidSignature, IOException, PGPException {
		return cert.check(data);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) throws InvalidName {
		if (null == name || name.getBytes().length > 256) {
			throw new InvalidName();
		}
		this.name = name;
	}

	public byte[] getOID() {
		return oid;
	}

	public String getOIDasString() throws InvalidOID {
		return OIDGenerator.OIDasHEX(oid);
	}

	@Override
	public PGPPrivateKey getPrivateKey() {
		return cert.getPrivateKey();
	}

	@Override
	public PGPPublicKey getPublicKey() {
		return cert.getPublicKey();
	}

}
