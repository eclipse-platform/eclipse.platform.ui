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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new CipherStreamsTest("test1"));
	return suite;
}
public void test1(){
	try {

	String password = "testing";
	byte[] data = "This is a test!".getBytes("UTF8");

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	CipherOutputStream cos = new CipherOutputStream(baos, password);
	cos.write(data);
	cos.close();

	byte[] encryptedData = baos.toByteArray();
	assertEquals("00", data.length, encryptedData.length);
	for(int i = 0; i < data.length; ++i){
		assertTrue("01."+i, data[i] != encryptedData[i]);
	}

	ByteArrayInputStream bais = new ByteArrayInputStream(encryptedData);
	CipherInputStream cis = new CipherInputStream(bais, password);
	byte[] decryptedData = new byte[data.length];
	cis.read(decryptedData);
	cis.close();

	assertEquals("02", data.length, decryptedData.length);
	for(int i = 0; i < data.length; ++i){
		assertEquals("03."+i, data[i], decryptedData[i]);
	}

	} catch(Exception e){
		assertTrue("04", false);
	}
}
}
