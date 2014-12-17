package de.back2heaven.test.easy.net;

import org.junit.Test;

import de.back2heaven.easy.net.cert.InvalidOID;
import de.back2heaven.easy.net.cert.OIDGenerator;

public class OIDGeneratorTest {

	@Test(expected = InvalidOID.class)
	public void invalidOIDNULL() throws InvalidOID {
		OIDGenerator.OIDasHEX(null);
	}

	@Test(expected = InvalidOID.class)
	public void invalidOIDSize() throws InvalidOID {
		OIDGenerator.OIDasHEX(new byte[128]);
	}
	@Test()
	public void generateAndTestOID() throws InvalidOID {
		OIDGenerator.OIDasHEX(OIDGenerator.generate());
	}

}
