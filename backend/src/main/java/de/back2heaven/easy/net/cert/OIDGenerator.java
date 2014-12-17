package de.back2heaven.easy.net.cert;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

import org.bouncycastle.util.encoders.Hex;

public class OIDGenerator {
	private static final short OID_LENGTH = 256;

	private OIDGenerator() {
	}

	public static byte[] asByteArray(final UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		ByteBuffer buffer = ByteBuffer.allocate(2 * Long.SIZE);
		buffer.putLong(msb).putLong(lsb);
		return buffer.array();
	}

	/**
	 * this method must return a valid OID using timestamp, UUID, and random
	 * bytes to fill oid up
	 * 
	 * @return a byte[256] array fixed length
	 */
	public static byte[] generate() {
		byte[] timestamp8 = ByteBuffer.allocate(Long.SIZE)
				.putLong(System.currentTimeMillis()).array();
		byte[] uuid16 = asByteArray(UUID.randomUUID());
		SecureRandom random = new SecureRandom(uuid16);
		byte[] oid = new byte[OID_LENGTH];
		random.nextBytes(oid);
		// overwrite bytes
		System.arraycopy(timestamp8, 0, oid, 0, timestamp8.length);
		System.arraycopy(uuid16, 0, oid, timestamp8.length, uuid16.length);
		return oid;
	}

	/**
	 * convert a OID to HEX String
	 * 
	 * @param oid
	 *            the fixed OID
	 * @return a HEX for given OID
	 * @throws InvalidOID
	 *             if OID length does not match
	 */
	public static String OIDasHEX(final byte[] oid) throws InvalidOID {
		if (null == oid || oid.length != OID_LENGTH) {
			throw new InvalidOID();
		}
		return Hex.toHexString(oid);
	}
}
