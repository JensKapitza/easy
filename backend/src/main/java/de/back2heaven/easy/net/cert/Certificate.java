package de.back2heaven.easy.net.cert;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.util.encoders.Hex;

public class Certificate implements PGPCertificate {
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

	public byte[] check(byte[] data) throws InvalidSignature, IOException,
			PGPException {
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

	public void save(Writer out) throws IOException, InvalidOID {
		// save cert to file or whatever

		out.write("Certificate: " + getName() + "\n");
		out.write("OID: " + OIDGenerator.OIDasHEX(getOID()) + "\n");

		out.write(Hex.toHexString(getBytes(true)) +"\n");
		out.write(Hex.toHexString(getBytes(false)));
		// store the cert
		out.flush();
	}

	public static Certificate load(String[] lines, char[] pass)
			throws IOException, InvalidCertificate {

		try {
			String name = lines[0].split(":")[1].trim();
			String oid = lines[1].split(":")[1].trim();

			// der rest ist das cert!
			String cert = lines[2];

			return new Certificate(name, OIDGenerator.HEXasOID(oid), new PGP(
					Hex.decode(cert), pass));
		} catch (Exception e) {
			throw new InvalidCertificate();
		}

	}

	public static void main(String[] args) throws Exception {
		Certificate cert = Certificate.load(Files.readAllLines(Paths.get("simpleCert.certx2")), "test".toCharArray());
				
				//new Certificate("Jens", OIDGenerator.generate(),
				//new PGP("simple", "test".toCharArray()));
		System.out.println(cert.getPrivateKey().getKeyID() + " + pID");
		cert.save(new FileWriter("simpleCert.certx"));
	}

	public static Certificate load(List<String> readAllLines, char[] charArray) throws IOException, InvalidCertificate {
		return load(readAllLines.toArray(new String[0]), charArray);
	}

	@Override
	public byte[] getBytes(boolean withPrivateKey) throws IOException {
		return cert.getBytes(withPrivateKey);
	}
}
