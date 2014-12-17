package de.back2heaven.easy.net.cert;

public class Certificate {
	private PGPCertificate cert;
	private String name;
	private byte[] oid;

	public Certificate(String name, byte[] oid, PGPCertificate cert) {
		this.name = name;
		this.oid = oid;
		this.cert = cert;
	}

	public byte[] crypt(byte[] data) {
		return cert.crypt(data);
	}

	public byte[] sign(byte[] data) {
		return cert.sign(data);
	}

	public byte[] decrypt(byte[] data) {
		return cert.decrypt(data);
	}

	public byte[] check(byte[] data) throws InvalidSignature {
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

}
