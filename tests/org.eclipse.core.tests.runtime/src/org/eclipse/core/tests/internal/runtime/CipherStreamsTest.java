/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.runtime;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.runtime.CipherInputStream;
import org.eclipse.core.internal.runtime.CipherOutputStream;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class CipherStreamsTest extends RuntimeTest {
	public CipherStreamsTest() {
		super(null);
	}

	public CipherStreamsTest(String name) {
		super(name);
	}

	protected void doCipherTest(String password, byte[] data) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CipherOutputStream cos = new CipherOutputStream(baos, password);
			cos.write(data);
			cos.close();

			byte[] encryptedData = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(encryptedData);
			CipherInputStream cis = new CipherInputStream(bais, password);
			byte[] decryptedData = new byte[data.length];
			cis.read(decryptedData);
			assertTrue("01", cis.read() == -1);
			cis.close();

			assertEquals("02", data.length, decryptedData.length);
			for (int i = 0; i < data.length; ++i) {
				assertEquals("03." + i, data[i], decryptedData[i]);
			}
		} catch (IOException e) {
			fail("99", e);
		}
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CipherStreamsTest.class.getName());
		suite.addTest(new CipherStreamsTest("test1"));
		return suite;
	}

	protected String getLongMessage() {
		return "This is a test!This is a test!This is a test!This is a test!This is a test!" + "este e' o meu conteudo (portuguese)there is no imagination for more sentences" + "este e' o meu conteudo (portuguese)there is no imagination for more sentences" + "este e' o meu conteudo (portuguese)there is no imagination for more sentences" + "este e' o meu conteudo (portuguese)there is no imagination for more sentences" + "This is a very long message that contains quite a lot of bytes and thus" + "may prove to make for a more interesting test case than the far simpler" + "(and admittedly mundane) messages that are also included in this test" + "este e' o meu conteudo (portuguese)there is no imagination for more sentences"
				+ "este e' o meu conteudo (portuguese)there is no imagination for more sentences" + "este e' o meu conteudo (portuguese)there is no imagination for more sentences";
	}

	protected String[] getMessages() {
		return new String[] {"This is a test!", "", "a", getLongMessage(), getVeryLongMessage(),};
	}

	protected String[] getPasswords() {
		return new String[] {"", "pasord", " ", "This is a very long password that contains quite a lot of bytes and thus" + "may prove to make for a more interesting test case than the far simpler" + "(and admittedly mundane) passwords that are also included in this array",};
	}

	protected String getVeryLongMessage() {
		StringBuffer message = new StringBuffer(1000);
		while (message.length() < 5300) {
			message.append(getLongMessage());
		}
		return message.toString();
	}

	public void test1() {
		String[] passwords = getPasswords();
		String[] messages = getMessages();
		for (int i = 0; i < messages.length; i++) {
			byte[] data = messages[i].getBytes();
			for (int j = 0; j < passwords.length; j++) {
				doCipherTest(passwords[j], data);
			}
		}
	}

}