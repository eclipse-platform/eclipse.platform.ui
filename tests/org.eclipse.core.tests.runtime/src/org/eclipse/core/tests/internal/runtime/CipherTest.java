package org.eclipse.core.tests.internal.runtime;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.runtime.Cipher;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class CipherTest extends RuntimeTest {
public CipherTest() {
	super(null);
}
public CipherTest(String name) {
	super(name);
}
public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new CipherTest("test1"));
	return suite;
}
public void test1(){
	try {
		String password = "testing";
		byte[] data = "This is a test!".getBytes("UTF8");

		Cipher cipher = new Cipher(Cipher.ENCRYPT_MODE, "testing");
		byte[] encryptedData = cipher.cipher(data);
		assertEquals("00", data.length, encryptedData.length);
		for(int i = 0; i < data.length; ++i){
			assertTrue("01."+i, data[i] != encryptedData[i]);
		}
	
		cipher = new Cipher(cipher.DECRYPT_MODE, password);
		byte[] decryptedData = cipher.cipher(encryptedData);
		assertEquals("02", data.length, decryptedData.length);
		for(int i = 0; i < data.length; ++i){
			assertEquals("03."+i, data[i], decryptedData[i]);
		}
	} catch(Exception e){
		assertTrue("04", false);
	}
}
}
