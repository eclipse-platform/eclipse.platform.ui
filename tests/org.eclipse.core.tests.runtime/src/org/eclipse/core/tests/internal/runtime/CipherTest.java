/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
	return new TestSuite(CipherTest.class);
//	TestSuite suite = new TestSuite();
//	suite.addTest(new CipherTest("test1"));
//	return suite;
}
public void test1(){
	try {
		String[] passwords = getPasswords();
		String[] messages = getMessages();
		for (int i = 0; i < messages.length; i++) {
			byte[] data = messages[i].getBytes();
			for (int j = 0; j < passwords.length; j++) {
				doCipherTest(passwords[j], data);
			}
		}
	} catch(Exception e){
		fail("04", e);
	}
}
public void testDifferentChunkSizes() {
	//read and write different chunk sizes at once.  Should still decrypt to
	//be the same bytes
	byte[] inputBytes = "This is the message that will be encrypted.".getBytes();	
	String password = "music";
	
	//encrypt first ten bytes, then remaining bytes
	try {
		Cipher cipher = new Cipher(Cipher.ENCRYPT_MODE, password);
		byte[] encrypted1 = cipher.cipher(inputBytes, 0, 10);
		//introduce some noise by encrypting an empty array
		cipher.cipher(new byte[0]);
		byte[] encrypted2 = cipher.cipher(inputBytes, 10, inputBytes.length-10);
		byte[] fullEncrypted = new byte[encrypted1.length + encrypted2.length];
		System.arraycopy(encrypted1, 0, fullEncrypted, 0, encrypted1.length);
		System.arraycopy(encrypted2, 0, fullEncrypted, encrypted1.length, encrypted2.length);

		cipher = new Cipher(Cipher.DECRYPT_MODE, password);
		//introduce some noise by decrypting an empty array
		cipher.cipher(new byte[0]);
		//now decrypt all at once
		byte[] result = cipher.cipher(fullEncrypted);
		
		assertEquals("1.0", inputBytes.length, result.length);
		for (int i = 0; i < inputBytes.length; i++) {
			assertEquals("2." + i, inputBytes[i], result[i]);
		}
	} catch (Exception e) {
		fail("1.99", e);
	}
}
	
protected String[] getMessages() {
	return new String[] {
		"This is a test a test!",
		"",
		"a",
		getLongMessage(),
		getVeryLongMessage(),
	};
}
protected String getLongMessage() {
	return "This is a test!This is a test!This is a test!This is a test!This is a test!"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"This is a very long message that contains quite a lot of bytes and thus" +
		"may prove to make for a more interesting test case than the far simpler" +
		"(and admittedly mundane) messages that are also included in this test"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences"+
		"este e' o meu conteudo (portuguese)there is no imagination for more sentences";
}
protected String[] getPasswords() {
	return new String[] {
		"",
		"password",
		"a",
		"This is a very long password that contains quite a lot of bytes and thus" +
		"may prove to make for a more interesting test case than the far simpler" +
		"(and admittedly mundane) passwords that are also included in this array",
	};
}
protected String getVeryLongMessage() {
	StringBuffer message = new StringBuffer(1000);
	while (message.length() < 5000) {
		message.append(getLongMessage());
	}
	return message.toString();
}
protected void doCipherTest(String password, byte[] data) throws Exception {
	Cipher cipher = new Cipher(Cipher.ENCRYPT_MODE, password);
	byte[] encryptedData = cipher.cipher(data);
	assertEquals("00", data.length, encryptedData.length);
	
	cipher = new Cipher(Cipher.DECRYPT_MODE, password);
	byte[] decryptedData = cipher.cipher(encryptedData);
	assertEquals("02", data.length, decryptedData.length);
	for(int i = 0; i < data.length; ++i){
		assertEquals("03."+i, data[i], decryptedData[i]);
	}
}
}
