/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
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
	
	cipher = new Cipher(cipher.DECRYPT_MODE, password);
	byte[] decryptedData = cipher.cipher(encryptedData);
	assertEquals("02", data.length, decryptedData.length);
	for(int i = 0; i < data.length; ++i){
		assertEquals("03."+i, data[i], decryptedData[i]);
	}
}
}
