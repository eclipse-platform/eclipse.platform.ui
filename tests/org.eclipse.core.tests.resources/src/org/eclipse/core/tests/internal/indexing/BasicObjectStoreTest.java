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
package org.eclipse.core.tests.internal.indexing;

import java.io.StringWriter;
import java.util.Vector;
import junit.framework.*;
import org.eclipse.core.internal.indexing.ObjectAddress;
import org.eclipse.core.internal.indexing.ObjectStore;

public class BasicObjectStoreTest extends TestCase {

	protected Vector addresses;
	protected TestEnvironment env;

	public BasicObjectStoreTest(String name, TestEnvironment env) {
		super(name);
		this.env = env;
	}

	public static Test suite(TestEnvironment env) {
		TestSuite suite = new TestSuite(BasicObjectStoreTest.class.getName());
		suite.addTest(new BasicObjectStoreTest("testSanity", env));
		suite.addTest(new BasicObjectStoreTest("testInsertRemove", env));
		suite.addTest(new BasicObjectStoreTest("testPopulate", env));
		suite.addTest(new BasicObjectStoreTest("testUpdate", env));
		suite.addTest(new BasicObjectStoreTest("testIdentity", env));
		suite.addTest(new BasicObjectStoreTest("testRemove", env));
		suite.addTest(new BasicObjectStoreTest("testLarge", env));
		return suite;
	}

	/**
	 * Generates a string of length n from a number i.
	 */
	private String generateString(int n, int i) {
		StringWriter a = new StringWriter();
		String suffix = Integer.toString(i);
		for (int k = 0; k < (n - suffix.length()); k++)
			a.write("0");
		a.write(suffix);
		return a.toString();
	}

	// populate an object store with 256 copies of a particular string
	public void populate(String string) throws Exception {
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		try {
			addresses = new Vector();
			for (int i = 0; i < 255; i++) {
				TestObject object = new TestObject(string.getBytes());
				ObjectAddress address = store.insertObject(object);
				addresses.addElement(address);
				if (i % 16 == 15)
					store.commit();
			}
		} finally {
			store.close();
		}
	}

	// test object identity
	public void testIdentity() throws Exception {
		ObjectStore.delete(env.getFileName());
		String s = "000011112222";
		populate(s);
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		try {
			s = "aaaabbbbcccc";
			int n = addresses.size();
			for (int i = 0; i < n; i++) {
				ObjectAddress address = (ObjectAddress) addresses.elementAt(i);
				TestObject object1 = (TestObject) store.acquireObject(address);
				TestObject object2 = (TestObject) store.acquireObject(address);
				object1.updateValue(s.getBytes());
				String t = new String(object2.getValue());
				assertEquals("Testing", s, t);
				assertSame(object1, object2);
				store.releaseObject(object1);
				store.releaseObject(object2);
				store.commit();
			}
		} finally {
			store.close();
		}
	}

	/** 
	 * Tests inserts and removes of objects.  This should force the compression of
	 * object pages.
	 */
	public void testInsertRemove() throws Exception {
		String string = "---*---*---*---*---*---*---*---*---*---*---*---*---*---*---*---*";
		ObjectStore.delete(env.getFileName());
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		try {
			for (int i = 0; i < 1000; i++) {
				TestObject object = new TestObject(string.getBytes());
				ObjectAddress address = store.insertObject(object);
				store.removeObject(address);
				if (i % 100 == 99)
					store.commit();
			}
		} finally {
			store.close();
		}
	}

	// put some big objects in
	public void testLarge() throws Exception {
		ObjectStore.delete(env.getFileName());
		StringBuffer buf = new StringBuffer(3500);
		while (buf.length() < 3500)
			buf.append("---*---*---*---*");
		String s = buf.toString();
		byte[] b1 = s.getBytes();
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		try {
			addresses = new Vector();
			for (int i = 0; i < (16 * 1024); i++) {
				TestObject object = new TestObject(b1);
				addresses.addElement(store.insertObject(object));
				if (i % 16 == 15)
					store.commit();
			}
			int n = addresses.size();
			for (int i = 0; i < n; i++) {
				ObjectAddress address = (ObjectAddress) addresses.elementAt(i);
				TestObject o = (TestObject) store.acquireObject(address);
				byte[] b2 = o.getValue();
				String t = new String(b2);
				store.releaseObject(o);
				assertEquals(s, t);
			}
		} finally {
			store.close();
		}
	}

	// populate and check the contents of the database
	public void testPopulate() throws Exception {
		ObjectStore.delete(env.getFileName());
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		StringBuffer buffer = new StringBuffer(4096);
		String fragment = "---*---*---*---*";
		for (int j = 0; j < 64; j++) {
			String s = buffer.toString();
			populate(s);
			store.open(env.getFileName());
			try {
				int n = addresses.size();
				for (int i = 0; i < n; i++) {
					ObjectAddress address = (ObjectAddress) addresses.elementAt(i);
					TestObject o = (TestObject) store.acquireObject(address);
					String t = new String(o.getValue());
					store.releaseObject(o);
					assertEquals(s, t);
				}
			} finally {
				store.close();
			}
			buffer.append(fragment);
		}
	}

	// remove all the objects
	public void testRemove() throws Exception {
		ObjectStore.delete(env.getFileName());
		String s = "000011112222";
		populate(s);
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		try {
			int n = addresses.size();
			for (int i = 0; i < n; i++) {
				ObjectAddress address = (ObjectAddress) addresses.elementAt(i);
				store.removeObject(address);
				store.commit();
			}
		} finally {
			store.close();
		}
	}

	// open and close
	public void testSanity() throws Exception {
		ObjectStore.delete(env.getFileName());
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		store.close();
		return;
	}

	// update the objects and check the contents again, object size does not change
	public void testUpdate() throws Exception {
		ObjectStore.delete(env.getFileName());
		String s = "000011112222";
		populate(s);
		ObjectStore store = new ObjectStore(new TestObjectPolicy());
		store.open(env.getFileName());
		try {
			s = "aaaabbbbcccc";
			int n = addresses.size();
			for (int i = 0; i < n; i++) {
				ObjectAddress address = (ObjectAddress) addresses.elementAt(i);
				TestObject object = (TestObject) store.acquireObject(address);
				object.updateValue(s.getBytes());
				store.releaseObject(object);
				store.commit();
			}
			for (int i = 0; i < n; i++) {
				ObjectAddress address = (ObjectAddress) addresses.elementAt(i);
				TestObject object = (TestObject) store.acquireObject(address);
				String t = new String(object.getValue());
				store.releaseObject(object);
				assertEquals(s, t);
			}
		} finally {
			store.close();
		}
	}

}