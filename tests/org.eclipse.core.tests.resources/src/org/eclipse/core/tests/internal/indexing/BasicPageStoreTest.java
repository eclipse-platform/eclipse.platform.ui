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

import java.util.Random;
import junit.framework.*;
import org.eclipse.core.internal.indexing.PageStore;

public class BasicPageStoreTest extends TestCase {

	protected TestEnvironment env;

	public BasicPageStoreTest(String name, TestEnvironment env) {
		super(name);
		this.env = env;
	}

	public static Test suite(TestEnvironment env) {
		TestSuite suite = new TestSuite(BasicPageStoreTest.class.getName());
		suite.addTest(new BasicPageStoreTest("testCreate", env));
		suite.addTest(new BasicPageStoreTest("testDelete", env));
		suite.addTest(new BasicPageStoreTest("testWrite", env));
		suite.addTest(new BasicPageStoreTest("testLogging1", env));
		suite.addTest(new BasicPageStoreTest("testLogging2", env));
		suite.addTest(new BasicPageStoreTest("testLogging3", env));
		suite.addTest(new BasicPageStoreTest("testWriteLarge", env));
		suite.addTest(new BasicPageStoreTest("testWriteHuge", env));
		suite.addTest(new BasicPageStoreTest("testReadOnly", env));
		suite.addTest(new BasicPageStoreTest("testCacheHitsSequential", env));
		suite.addTest(new BasicPageStoreTest("testCacheHitsCircular", env));
		suite.addTest(new BasicPageStoreTest("testCacheHitsRandom", env));
		suite.addTest(new BasicPageStoreTest("testRandomReadWrite", env));
		return suite;
	}

	// check a byte array against a value
	boolean check(byte[] b, byte i) {
		for (int j = 0; j < b.length; j++) {
			if (b[j] != i)
				return false;
		}
		return true;
	}

	// fill a byte array with a value
	void fill(byte[] b, byte i) {
		for (int j = 0; j < b.length; j++)
			b[j] = i;
	}

	/**
	 * Creates an initialized 128 page store.
	 */
	public int initializeStore() throws Exception {
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		int n = 128;
		for (int i = 0; i < n; i++) {
			TestPage p = (TestPage) store.acquire(i);
			p.fill((byte) i);
			p.release();
		}
		store.close();
		return n;
	}

	void printStats(PageStore store) throws Exception {
		env.println("Number of pages       = " + store.numberOfPages());
		env.println("Number of writes      = " + store.numberOfFileWrites());
		env.println("Number of file reads  = " + store.numberOfFileReads());
		env.println("Number of cache reads = " + store.numberOfCacheHits());
		env.println("Number of reads       = " + store.numberOfReads());
		env.println("Cache hit ratio       = " + (float) store.numberOfCacheHits() / (float) store.numberOfReads());
	}

	/**
	 * Test cache performance using a circular reference pattern.
	 */
	public void testCacheHitsCircular() throws Exception {
		env.printHeading("testCacheHitsCircular");
		initializeStore();
		PageStore store = new PageStore(new TestPagePolicy());
		env.println("Testing 41 of 40");
		store.open(env.getFileName());
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 41; i++) {
				TestPage p = (TestPage) store.acquire(i);
				assertTrue(p.check((byte) i));
				p.release();
			}
		}
		printStats(store);
		store.close();
		env.println("Testing 40 of 40");
		store.open(env.getFileName());
		for (int j = 0; j < 100; j++) {
			for (int i = 0; i < 40; i++) {
				TestPage p = (TestPage) store.acquire(i);
				assertTrue(p.check((byte) i));
				p.release();
			}
		}
		printStats(store);
		store.close();
	}

	/**
	 * Test the effect of increasing cache sizes
	 */
	public void testCacheHitsRandom() throws Exception {
		env.printHeading("testCacheHitsRandom");
		PageStore.delete(env.getFileName());
		int n = initializeStore();
		for (int m = 0; m <= n; m += 16) {
			PageStore store = new PageStore(new TestPagePolicy());
			store.open(env.getFileName());
			Random r = new Random(100);
			for (int i = 0; i < 1000; i++) {
				TestPage p = (TestPage) store.acquire(Math.abs(r.nextInt() % n));
				p.release();
			}
			printStats(store);
			store.close();
		}
	}

	/**
	 * Checks the performance of sequential access.
	 */
	public void testCacheHitsSequential() throws Exception {
		env.printHeading("testCacheHitsSequential");
		int n = initializeStore();
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		for (int i = 0; i < n; i++) {
			TestPage p = (TestPage) store.acquire(i);
			assertTrue(p.check((byte) i));
			p.release();
		}
		printStats(store);
		store.close();
	}

	/**
	 */
	public void testCreate() throws Exception {
		env.printHeading("testCreate");
		PageStore.create(env.getFileName());
		assertTrue(PageStore.exists(env.getFileName()));
	}

	/**
	 */
	public void testDelete() throws Exception {
		env.printHeading("testDelete");
		PageStore.delete(env.getFileName());
		assertTrue(!PageStore.exists(env.getFileName()));
	}

	/**
	 * Tests the log.
	 */
	public void testLogging1() throws Exception {
		env.printHeading("testLogging1");
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		testLogPopulate(store);
		store.testLogging1();
		testLogValidate(store);
		store.close();
	}

	/**
	 * Tests the log.
	 */
	public void testLogging2() throws Exception {
		env.printHeading("testLogging2");
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		testLogPopulate(store);
		store.testLogging2();
		testLogValidate(store);
		store.close();
	}

	/**
	 * Tests the log.
	 */
	public void testLogging3() throws Exception {
		env.printHeading("testLogging3");
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		testLogPopulate(store);
		store.testLogging3();
		testLogValidate(store);
		store.close();
	}

	/**
	 * Populate the store for the logging tests.
	 */
	public void testLogPopulate(PageStore store) throws Exception {
		for (int i = 0; i < 128; i++) {
			TestPage p = (TestPage) store.acquire(i);
			p.fill((byte) i);
			p.release();
		}
	}

	/**
	 * Tests the contents of the store for the logging tests.
	 */
	public void testLogValidate(PageStore store) throws Exception {
		for (int i = 0; i < 128; i++) {
			TestPage p = (TestPage) store.acquire(i);
			assertTrue("Failed checking page " + i, p.check((byte) i));
			p.release();
		}
	}

	/**
	 * Tests random reading & writing.
	 */
	public void testRandomReadWrite() throws Exception {
		env.printHeading("testRandomReadWrite");
		PageStore.delete(env.getFileName());
		int n = 128;
		byte[] value = new byte[n];
		for (int i = 0; i < n; i++)
			value[i] = 0;
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		Random r = new Random(100);
		for (int i = 0; i < 2000; i++) {
			int k = Math.abs(r.nextInt() % n);
			TestPage p = (TestPage) store.acquire(k);
			assertTrue(p.check(value[k]));
			value[k] = (byte) r.nextInt();
			p.fill(value[k]);
			p.release();
		}
		printStats(store);
		store.close();
	}

	/**
	 * Tests read-only access on the store.
	 */
	public void testReadOnly() throws Exception {
		env.printHeading("testReadOnly");
		PageStore.delete(env.getFileName());
		int n = initializeStore();
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		assertTrue(store.numberOfPages() == n);
		for (int i = 0; i < n; i++) {
			TestPage p = (TestPage) store.acquire(i);
			assertTrue(p.check((byte) i));
			p.release();
		}
		printStats(store);
		store.close();
	}

	/**
	 * Adds & checks 128 8K pages (1 meg) to the page file.
	 */
	public void testWrite() throws Exception {
		env.printHeading("testWrite");
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		writeBlock(store);
		printStats(store);
		store.close();
	}

	/**
	 * Adds a 64 meg chunk to the page file.
	 */
	public void testWriteHuge() throws Exception {
		env.printHeading("testWriteHuge");
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		for (int i = 0; i < 64; i++)
			writeBlock(store);
		printStats(store);
		store.close();
	}

	/**
	 * Adds a 16 meg chunk to the page file.
	 */
	public void testWriteLarge() throws Exception {
		env.printHeading("testWriteLarge");
		PageStore.delete(env.getFileName());
		PageStore store = new PageStore(new TestPagePolicy());
		store.open(env.getFileName());
		for (int i = 0; i < 16; i++)
			writeBlock(store);
		printStats(store);
		store.close();
	}

	/**
	 * Adds & checks 128 8K pages (1 meg) to the page file.
	 */
	public int writeBlock(PageStore store) throws Exception {
		TestPage p = null;
		int m = 128;
		int n1 = store.numberOfPages();
		int n2 = n1 + m;
		for (int i = n1; i < n2; i++) {
			p = (TestPage) store.acquire(i);
			p.fill((byte) i);
			p.release();
		}
		store.commit();
		assertEquals(store.numberOfPages(), n2);
		for (int i = n1; i < n2; i++) {
			p = (TestPage) store.acquire(i);
			assertTrue("Page " + i + " " + p.value(), p.check((byte) i));
			p.release();
		}
		return m;
	}
}