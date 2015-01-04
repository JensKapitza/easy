package de.back2heaven.easy.net.cert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

public class PGP implements PGPCertificate {
	private static final int KEY_SIZE = 4096;
	private static final int KEY_ITERATION = 0xff;

	PGPPrivateKey privk;
	PGPPublicKey pubk;
	PGPSecretKey seck;

	public PGP(String idMail, char[] pass) throws PGPException {
		// http://bouncycastle-pgp-cookbook.blogspot.de/

		// This object generates individual key-pairs.
		RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

		// Boilerplate RSA parameters, no need to change anything
		// except for the RSA key-size (2048). You can use whatever
		// key-size makes sense for you -- 4096, etc.
		kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001),
				new SecureRandom(), KEY_SIZE, 12));

		// First create the master (signing) key with the generator.
		PGPKeyPair rsakp_sign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN,
				kpg.generateKeyPair(), new Date());

		// Add a self-signature on the id
		PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();

		// Add signed metadata on the signature.
		// 1) Declare its purpose
		signhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA
				| KeyFlags.CERTIFY_OTHER);
		// 2) Set preferences for secondary crypto algorithms to use
		// when sending messages to this key.
		signhashgen.setPreferredSymmetricAlgorithms(false,
				new int[] { SymmetricKeyAlgorithmTags.AES_256 });
		signhashgen.setPreferredHashAlgorithms(false,
				new int[] { HashAlgorithmTags.SHA512 });
		// 3) Request senders add additional checksums to the
		// message (useful when verifying unsigned messages.)
		signhashgen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);

		// Create a signature on the encryption subkey.
		PGPSignatureSubpacketGenerator enchashgen = new PGPSignatureSubpacketGenerator();
		// Add metadata to declare its purpose
		enchashgen.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS
				| KeyFlags.ENCRYPT_STORAGE);

		// Objects used to encrypt the secret key.
		PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider()
				.get(HashAlgorithmTags.SHA1);
		PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider()
				.get(HashAlgorithmTags.SHA512);

		// bcpg 1.48 exposes this API that includes s2kcount. Earlier
		// versions use a default of 0x60.
		PBESecretKeyEncryptor pske = (new BcPBESecretKeyEncryptorBuilder(
				PGPEncryptedData.AES_256, sha256Calc, KEY_ITERATION))
				.build(pass);

		// Finally, create the keyring itself. The constructor
		// takes parameters that allow it to generate the self
		// signature.
		final PGPKeyRingGenerator krgen = new PGPKeyRingGenerator(
				PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign, idMail,
				sha1Calc, signhashgen.generate(), null,
				new BcPGPContentSignerBuilder(rsakp_sign.getPublicKey()
						.getAlgorithm(), HashAlgorithmTags.SHA512), pske);

		// Then an encryption subkey.
		PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT,
				kpg.generateKeyPair(), new Date());

		// Add our encryption subkey, together with its signature.
		krgen.addSubKey(rsakp_enc, enchashgen.generate(), null);

		// Generate public key ring, dump to file.
		// PGPPublicKeyRing pkr = krgen.generatePublicKeyRing();

		// Generate private key, dump to file.
		PGPSecretKeyRing skr = krgen.generateSecretKeyRing();
		PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(
				new BcPGPDigestCalculatorProvider()).build(pass);

		seck = skr.getSecretKey();
		pubk = seck.getPublicKey();
		privk = seck.extractPrivateKey(decryptor);

		System.out.println(privk.getPrivateKeyDataPacket().getFormat());
		System.out.println(privk.getPublicKeyPacket().getAlgorithm());
	}

	public PGP(byte[] decode, char[] pass) throws PGPException, IOException {
		// know we need to decode the cert!

		PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(
				new BcPGPDigestCalculatorProvider()).build(pass);
		PGPSecretKeyRing secretKeyRing = new PGPSecretKeyRing(decode,
				new JcaKeyFingerprintCalculator());

		seck = secretKeyRing.getSecretKey();
		pubk = seck.getPublicKey();
		privk = seck.extractPrivateKey(decryptor);
	}

	@Override
	public byte[] getBytes(boolean withPrivateKey) throws IOException {
		// store to file!
		if (withPrivateKey) {
			return seck.getEncoded();
		}
		return pubk.getEncoded();

	}

	public static void main(String[] args) throws Exception {
		PGP p = new PGP("jensX", "fdsgfd".toCharArray());
		System.out.println(new String(p.check(p.sign("das ist ein Test"
				.getBytes()))));
	}

	@Override
	public PGPPrivateKey getPrivateKey() {
		return privk;
	}

	@Override
	public PGPPublicKey getPublicKey() {
		return pubk;
	}

	@Override
	public byte[] encrypt(byte[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] sign(byte[] data) throws IOException, PGPException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

		PGPSignatureGenerator sGen = new PGPSignatureGenerator(
				new BcPGPContentSignerBuilder(pubk.getAlgorithm(),
						HashAlgorithmTags.SHA512));
		sGen.init(PGPSignature.BINARY_DOCUMENT, privk);
		Iterator<?> it = pubk.getUserIDs();
		if (it.hasNext()) {
			PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
			spGen.setSignerUserID(false, (String) it.next());
			sGen.setHashedSubpackets(spGen.generate());
		}
		PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(
				PGPCompressedData.ZLIB);
		BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(bos));
		sGen.generateOnePassVersion(false).encode(bOut);

		PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
		OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY,
				"SIGNED-DATA", data.length, new Date());

		for (byte ch : data) {
			lOut.write(ch);
			sGen.update((byte) ch);
		}

		lGen.close();
		sGen.generate().encode(bOut);
		cGen.close();

		return bos.toByteArray();
	}

	@Override
	public byte[] decrypt(byte[] data) {
		return null;

	}

	@Override
	public byte[] check(byte[] data) throws InvalidSignature, IOException,
			PGPException {

		JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(data);
		PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
		pgpFact = new JcaPGPObjectFactory(c1.getDataStream());
		PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) pgpFact
				.nextObject();
		PGPOnePassSignature ops = p1.get(0);
		PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();
		InputStream dIn = p2.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ops.init(new BcPGPContentVerifierBuilderProvider(), pubk);
		int ch;
		while ((ch = dIn.read()) >= 0) {
			ops.update((byte) ch);
			out.write(ch);
		}
		out.close();
		PGPSignatureList p3 = (PGPSignatureList) pgpFact.nextObject();
		if (ops.verify(p3.get(0))) {
			// wenn alles ok ist kommen hier nur die richtigen DATEN raus
			return out.toByteArray();
		} else {
			throw new InvalidSignature();
		}

	}

}
