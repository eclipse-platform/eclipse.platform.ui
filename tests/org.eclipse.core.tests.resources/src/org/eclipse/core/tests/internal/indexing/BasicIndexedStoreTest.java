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
import java.util.*;
import junit.framework.*;
import org.eclipse.core.internal.indexing.*;

public class BasicIndexedStoreTest extends TestCase {

	TestEnvironment env;

	public BasicIndexedStoreTest(String name, TestEnvironment env) {
		super(name);
		this.env = env;
	}

	public static Test suite(TestEnvironment env) {
		TestSuite suite = new TestSuite(BasicIndexedStoreTest.class.getName());
		suite.addTest(new BasicIndexedStoreTest("testSanity", env));
		suite.addTest(new BasicIndexedStoreTest("testRecovery", env));
		suite.addTest(new BasicIndexedStoreTest("testTransactions", env));
		suite.addTest(new BasicIndexedStoreTest("testIndexInsert", env));
		suite.addTest(new BasicIndexedStoreTest("testIndexCursorOperations", env));
		suite.addTest(new BasicIndexedStoreTest("testIndexRemove", env));
		suite.addTest(new BasicIndexedStoreTest("testIndexOrdering", env));
		suite.addTest(new BasicIndexedStoreTest("testIndexReplace", env));
		suite.addTest(new BasicIndexedStoreTest("testObjectUpdate", env));
		suite.addTest(new BasicIndexedStoreTest("testObjectPerformance", env));
		suite.addTest(new BasicIndexedStoreTest("testMultiCursorSearch", env));
		suite.addTest(new BasicIndexedStoreTest("testMultiCursorUpdate", env));
		suite.addTest(new BasicIndexedStoreTest("testMultiCursorRemove1", env));
		suite.addTest(new BasicIndexedStoreTest("testMultiCursorRemove2", env));
		suite.addTest(new BasicIndexedStoreTest("testIndexSplit", env));
		suite.addTest(new BasicIndexedStoreTest("test1GCE9JD", env));
		suite.addTest(new BasicIndexedStoreTest("testObjectLife", env));
		return suite;
	}

	private int random(int lo, int hi) {
		double t0 = Math.random();
		double t1 = (hi + 1 - lo) * t0 + lo;
		double t2 = Math.floor(t1);
		return (int) t2;
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

	/**
	 * Insert entries with the specified key length into the index. Compare the ordering after insertion.
	 * Key values are 0 to numberOfEntries-1.
	 * Keys are inserted in order of their value modulo the skip value.
	 * That is, all values equivalent to 0 are inserted first, then all values equivalent to 1, ...
	 */
	private void insertAndCompare(int keySize, int numberOfEntries, int skipValue) throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		store.createIndex("Index");
		Index index = store.getIndex("Index");
		String key;
		for (int i = 0; i < skipValue; i++) {
			int j = i;
			while (j < numberOfEntries) {
				key = generateString(keySize, j);
				index.insert(key, "");
				j += skipValue;
				if (j % 100 == 0)
					store.commit();
			}
			store.commit();
		}
		store.close();

		store.open(env.getFileName());
		index = store.getIndex("Index");
		IndexCursor c = index.open();
		c.findFirstEntry();
		int j = 0;
		while (!c.isAtEnd()) {
			assertEquals(c.getKeyAsString(), generateString(keySize, j));
			c.next();
			j++;
		}
		store.close();
	}

	/**
	 * Test for index store mess up recorded in PR 1GCE9JD
	 */
	public void test1GCE9JD() throws Exception {
		IndexedStore store = null;
		IndexedStore.delete(env.getFileName());
		store = new IndexedStore();
		store.open(env.getFileName());
		try {
			String a = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>.<classpath>.    <classpathentry kind=\"src\" path=\"\"/>.    <classpathentry kind=\"output\" path=\"\"/>.</classpath>";
			store.createIndex("index");
			ObjectID id = store.createObject(a);
			String b = store.getObjectAsString(id);
			assertEquals(a, b);
		} finally {
			store.close();
		}
	}

	/**
	 * This tests basic cursor insertion, location, and removal operations.
	 */
	public void testIndexCursorOperations() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		try {

			/* Create an index */
			store.createIndex("Index1");
			Index index = store.getIndex("Index1");
			String key = null;
			ObjectID id = null;

			/* populate the index using unique values and duplicate keys */
			for (int i = 0; i < 10; i++) {
				index.insert("abaa", new ObjectID(i));
			}
			for (int i = 10; i < 20; i++) {
				index.insert("acaa", new ObjectID(i));
			}
			store.commit();

			/* run the index from beginning to end, testing the values */
			IndexCursor c = index.open();
			int i = 0;
			c.findFirstEntry();
			while (!c.isAtEnd()) {
				id = c.getValueAsObjectID();
				assertTrue("assertion 1 value " + i, id.equals(new ObjectID(i)));
				c.next();
				i++;
			}

			/* run the index from the end to the beginning, testing the values */
			i = 19;
			c.previous();
			while (!c.isAtBeginning()) {
				id = c.getValueAsObjectID();
				assertTrue("assertion 2 value " + i, id.equals(new ObjectID(i)));
				c.previous();
				i--;
			}
			c.close();

			/* find all entries matching a given prefix */
			c = index.open();
			key = "ab";
			c.find(key);
			i = 0;
			while (c.keyMatches(key)) {
				id = c.getValueAsObjectID();
				assertTrue("assertion 3 value " + i, id.equals(new ObjectID(i)));
				c.next();
				i++;
			}
			assertTrue("assertion 3.1", i == 10);
			c.close();

			/* find all entries matching a different prefix */
			key = "ac";
			c = index.open();
			c.find(key);
			while (c.keyMatches(key)) {
				id = c.getValueAsObjectID();
				assertTrue("assertion 4 value " + i, id.equals(new ObjectID(i)));
				c.next();
				i++;
			}
			assertTrue("assertion 4.1", i == 20);
			c.close();

			/* find all entries matching a third prefix */
			key = "a";
			c = index.open();
			i = 0;
			c.find(key);
			while (c.keyMatches(key)) {
				id = c.getValueAsObjectID();
				assertTrue("assertion 5 value " + i, id.equals(new ObjectID(i)));
				c.next();
				i++;
			}
			assertTrue("assertion 5.1", i == 20);
			c.close();

			/* remove some entries that match a given prefix */
			key = "ab";
			c = index.open();
			c.find(key);
			while (c.keyMatches(key)) {
				c.remove();
			}
			c.close();
			store.commit();
			i = index.getNumberOfEntries();
			assertTrue("assertion 5.9 value " + i, i == 10);

			/* rescan the index to find all entries that match the short prefix again */
			key = "a";
			c = index.open();
			c.find(key);
			i = 10;
			while (c.keyMatches(key)) {
				id = c.getValueAsObjectID();
				assertTrue("assertion 6 value " + i, id.equals(new ObjectID(i)));
				c.next();
				i++;
			}
			assertTrue("assertion 6.1", i == 20);
			c.close();

			/* get the list of all identifiers in the index */
			Vector v = index.getObjectIdentifiersMatching("a");
			Iterator idStream = v.iterator();
			i = 10;
			while (idStream.hasNext()) {
				id = (ObjectID) idStream.next();
				assertTrue("assertion 7 value " + i, id.equals(new ObjectID(i)));
				i++;
			}
			assertTrue(i == 20);
			store.removeIndex("Index1");
		} finally {
			store.close();
		}
	}

	/**
	 * Insert 1000 entries with a key length 64 and value length 0 into an index.
	 */
	public void testIndexInsert() throws Exception {
		insertAndCompare(64, 1000, 1);
	}

	/**
	 * Tests the insertion of strings -- makes sure conversion to UTF8 is done correctly and that
	 * the index leaf nodes are ordered properly.
	 * Also tests many small insertions.
	 */
	public void testIndexOrdering() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		try {
			store.createIndex("Index");
			Index index = store.getIndex("Index");
			String key, value;

			/* populate the index */
			for (int i = 0; i < 10000; i++) {
				key = (new Integer(i)).toString();
				value = "";
				index.insert(key, value);
				if (i % 500 == 0)
					store.commit();
			}
		} finally {
			store.close();
		}

		/* run the index, testing the key order */
		store.open(env.getFileName());
		try {
			Index index = store.getIndex("Index");
			IndexCursor c = index.open();
			c.findFirstEntry();
			String lastKey = "";
			while (!c.isAtEnd()) {
				String key = c.getKeyAsString();
				assertTrue(lastKey.compareTo(key) < 0);
				lastKey = key;
				c.next();
			}
			c.close();
		} finally {
			store.close();
		}
	}

	/**
	 * Tests removal of items.
	 * Create an index.  Add items and remove items from it.
	 * Alternately expanding the index and contracting it.
	 */
	public void testIndexRemove() throws Exception {
		int limit = 200;
		int keySize = 500;
		int valueSize = 100;
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		try {
			store.createIndex("Index");
			Index index = store.getIndex("Index");
			for (int i = 0; i < limit; i += 20) {
				for (int j = 0; j < i; j++) {
					String key = generateString(keySize, j);
					String value = generateString(valueSize, 0);
					index.insert(key, value);
				}
				store.commit();
				assertEquals("test after insert", i, index.getNumberOfEntries());
				index.getNumberOfNodes();
				IndexCursor c = index.open();
				for (int j = 0; j < i; j++) {
					String key = generateString(keySize, j);
					c.find(key).remove();
				}
				c.close();
				store.commit();
				assertEquals("test after removal", 0, index.getNumberOfEntries());
				assertEquals("test nodes after removal", 0, index.getNumberOfNodes());
			}
		} finally {
			store.close();
		}
	}

	/** 
	 * Tests the replacement of values of items in an index.
	 */
	public void testIndexReplace() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		int n = 5000;
		int i;
		store.open(env.getFileName());
		try {
			Index index = store.createIndex("Index");
			String key;
			String value = "---*---*---*---*";

			for (i = 0; i < n; i++) {
				key = generateString(16, i);
				index.insert(key, "");
				if (i % 100 == 99)
					store.commit();
			}

			IndexCursor c = index.open();
			c.findFirstEntry();
			i = 0;
			while (!c.isAtEnd()) {
				c.updateValue(value);
				c.next();
				if (i % 100 == 99)
					store.commit();
				i++;
			}
			assertTrue(i == n);

			c = index.open();
			c.findFirstEntry();
			i = 0;
			while (!c.isAtEnd()) {
				assertEquals(value, c.getValueAsString());
				c.next();
				i++;
			}
		} finally {
			store.close();
		}
	}

	/**
	 * Store sequential keys in an index, retrieve them by cursor and by individual key.
	 * Make sure there are enough to force some page splitting behavior.  There are runs of 
	 * keys in this test.  Retrieval by key points at first one.
	 */
	public void testIndexSplit() throws Exception {
		int n = 5; /* the number of key runs to generate */
		int l = 500; /* the length of each key */
		int r = 200; /* number of entries in a run of keys */
		int i;
		Vector key = new Vector(n);

		env.println("Generating...");
		for (i = 0; i < n; i++) {
			key.addElement(generateString(l, i));
		}
		env.println("...Done");

		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();

		env.println("Inserting...");
		store.open(env.getFileName());
		try {
			Index index = store.createIndex("Index");
			for (i = 0; i < n; i++) {
				for (int j = 0; j < r; j++) {
					index.insert((String) key.elementAt(i), "");
				}
				store.commit();
			}
		} finally {
			store.close();
		}
		env.println("...Done");

		env.println("Retrieving by cursor...");
		store.open(env.getFileName());
		try {
			Index index = store.getIndex("Index");
			IndexCursor c = index.open();
			c.findFirstEntry();
			i = 0;
			while (!c.isAtEnd()) {
				for (int j = 0; j < r; j++) {
					assertEquals(c.getKeyAsString(), (String) key.elementAt(i));
					c.next();
				}
				i++;
			}
		} finally {
			store.close();
		}
		env.println("...Done");

		env.println("Retrieving by key...");
		store.open(env.getFileName());
		try {
			Index index = store.getIndex("Index");
			IndexCursor c = index.open();
			for (i = 0; i < n; i++) {
				c.find((String) key.elementAt(i));
				for (int j = 0; j < r; j++) {
					assertTrue(c.keyEquals((String) key.elementAt(i)));
					c.next();
				}
			}
			c.close();
		} finally {
			store.close();
		}
		env.println("...Done");
	}

	/**
	 * Tests usage of multiple cursors in a single thread.
	 * This is a removal test to ensure there are no interactions between cursors.
	 * A value will be removed that will cause cursors to be adjusted.
	 * Make sure that all the cursors are adjusted correctly.
	 */
	public void testMultiCursorRemove1() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		int keySize = 10;
		String key;
		int n = 10; /* number of entries & cursors */
		store.open(env.getFileName());
		try {
			Index index = store.createIndex("Index");

			/* create and populate an index */
			for (int i = 0; i < n; i++) {
				key = generateString(keySize, i);
				index.insert(key, "");
			}
			store.commit();

			/* get and place n cursors at the n entries */
			IndexCursor[] c = new IndexCursor[n];
			for (int i = 0; i < n; i++) {
				key = generateString(keySize, i);
				c[i] = index.open();
				c[i].find(key);
			}

			/* remove the entries at the cursors, this will cause cursor adjustment. */
			for (int i = 0; i < n; i++) {
				c[i].remove();
				for (int j = i + 1; j < n; j++) {
					String key0 = generateString(keySize, j);
					String keyj = c[j].getKeyAsString();
					if (j == i + 1) {
						String keyi = c[i].getKeyAsString();
						assertEquals(keyi, keyj);
					}
					assertEquals(key0, keyj);
				}
				c[i].close();
			}
		} finally {
			store.close();
		}
	}

	/**
	 * Test usage of multiple cursors in a single thread.
	 * This is a deletion test to ensure that if two cursors point to the
	 * same entry you can delete the entry, but the other cursors become
	 * invalid until repositioned.
	 */
	public void testMultiCursorRemove2() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		int keySize = 10;
		String key;
		int n = 10; // number of entries

		store.open(env.getFileName());

		try {
			/* create and populate an index */
			Index index = store.createIndex("Index");
			for (int i = 0; i < n; i++) {
				key = generateString(keySize, i);
				index.insert(key, "");
			}
			store.commit();

			/* Place two cursors at the same entry */
			IndexCursor c1 = index.open().findFirstEntry();
			IndexCursor c2 = index.open().findFirstEntry();

			/* Remove the entry at the first cursor and test the second cursor for results */
			c1.remove();

			int id = 0;
			try {
				c2.remove();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("remove test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.getKey();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("get key test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.getValue();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("get value test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.updateValue("123");
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("update value test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.isAtEnd();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("isAtEnd test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.isAtBeginning();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("isAtBeginning test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.isSet();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("isSet test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.next();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("move next test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.previous();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("move previous test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.keyEquals("");
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("key equals test", IndexedStoreException.EntryRemoved, id);

			id = 0;
			try {
				c2.keyMatches("");
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("key matches test", IndexedStoreException.EntryRemoved, id);

			c2.reset();
			id = 0;
			try {
				c2.next();
			} catch (IndexedStoreException e) {
				id = e.id;
			}
			assertEquals("reset test", 0, id);

			assertEquals("positioning test", c1.getValueAsString(), c2.getValueAsString());

		} finally {
			store.close();
		}
	}

	/**
	 * Test usage of multiple cursors in a single thread.
	 * This is a search test to ensure there are no interactions between cursors.
	 */
	public void testMultiCursorSearch() throws Exception {
		int keySize = 5;
		int n = 10; /* number of cursors */

		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());

		try {

			/* create and populate an index, all keys and values are unique */
			Index index = store.createIndex("Index");
			for (int i = 0; i < 1000; i++) {
				String key = generateString(keySize, i);
				index.insert(key, "");
			}
			store.commit();

			/* get n cursors and position them at the first entry */
			IndexCursor[] c = new IndexCursor[n];
			for (int i = 0; i < n; i++) {
				c[i] = index.open();
				c[i].findFirstEntry();
			}

			/* search using the cursors, cursors should never be left in an unset state. */
			for (int i = 0; i < 100; i++) {

				/* save the keys at each cursor -- gives a snapshot of the state */
				String[] savedKeys = new String[n];
				for (int j = 0; j < n; j++) {
					savedKeys[j] = c[j].getKeyAsString();
				}

				/* select a random cursor, position in the index, direction, and length to scan */
				int pos = random(0, 999);
				int length = random(10, 40);
				int direction = ((random(0, 1) == 0) ? -1 : 1);
				int selection = random(0, n - 1);
				IndexCursor cx = c[selection];

				/* scan starting at the cursor, skipping the begin/end state */
				String key = generateString(keySize, pos);
				cx.find(key);
				for (int j = 0; j < length; j++) {
					String k = cx.getKeyAsString();
					assertTrue(pos == Integer.parseInt(k));
					if (direction < 0) {
						cx.previous();
						if (cx.isAtBeginning())
							cx.previous();
						pos--;
						if (pos < 0)
							pos = 999;
					} else {
						cx.next();
						if (cx.isAtEnd())
							cx.next();
						pos++;
						if (pos > 999)
							pos = 0;
					}
				}

				/* check the other cursors for any side effects */
				for (int j = 0; j < n; j++) {
					if (j == selection)
						continue;
					assertEquals(savedKeys[j], c[j].getKeyAsString());
				}
			}
			for (int i = 0; i < n; i++)
				c[i].close();
		} finally {
			store.close();
		}
	}

	/**
	 * Test usage of multiple cursors in a single thread.
	 * This is an update test to ensure there are no interactions between cursors.
	 * This tests adjustments due to node splitting.
	 */
	public void testMultiCursorUpdate() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		int keySize = 1000;
		int valueSize1 = 0;
		int valueSize2 = 2000;
		String key, value;
		int n = 10; /* number of cursors */

		/* create and populate an index, all keys and values are unique */
		store.open(env.getFileName());
		try {
			Index index = store.createIndex("Index");
			for (int i = 0; i < n; i++) {
				key = generateString(keySize, i);
				value = generateString(valueSize1, i);
				index.insert(key, value);
			}
			store.commit();

			/* get n cursors and position them at the n entries */
			IndexCursor[] c = new IndexCursor[n];
			for (int i = 0; i < n; i++) {
				c[i] = index.open();
				key = generateString(keySize, i);
				c[i].find(key);
			}

			/* 
			 Update the values at the cursors. 
			 This will cause major splitting. 
			 Test cursor values after each update. 
			 */
			for (int i = 0; i < n; i++) {
				value = generateString(valueSize2, i);
				c[i].updateValue(value);
				for (int j = 0; j < n; j++) {
					int size = (j <= i) ? valueSize2 : valueSize1;
					String v1 = generateString(size, j);
					String v2 = c[j].getValueAsString();
					assertEquals(v1, v2);
				}
			}

			for (int i = 0; i < n; i++)
				c[i].close();
		} finally {
			store.close();
		}
	}

	/** 
	 * Timed test for creation and retrieval of 100000 simple objects.
	 */
	public void testObjectPerformance() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		HashSet ids = new HashSet();

		store.open(env.getFileName());
		try {
			int n = 100000;
			byte[] a = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef".getBytes();
			long t0 = System.currentTimeMillis();
			for (int i = 0; i < n; i++) {
				ids.add(store.createObject(a));
				if (i % 1000 == 999)
					store.commit();
			}
			long t1 = System.currentTimeMillis();
			env.println("Time to create = " + ((t1 - t0) / 1000));
		} finally {
			store.close();
		}

		store.open(env.getFileName());
		try {
			Iterator idStream = ids.iterator();
			long t0 = System.currentTimeMillis();
			while (idStream.hasNext()) {
				store.getObject((ObjectID) idStream.next());
			}
			long t1 = System.currentTimeMillis();
			env.println("Time to retrieve = " + ((t1 - t0) / 1000));
		} finally {
			store.close();
		}

	}

	/**
	 * Tests the creation and updating of objects in the store.
	 * This test stores a single small object and then grows it over time until is is 6000 bytes.
	 */
	public void testObjectUpdate() throws Exception {
		int n = 1000;
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		try {
			String s = "";
			ObjectID id = store.createObject(s);
			for (int i = 0; i < n; i++) {
				s = s + "123456";
				store.updateObject(id, s);
			}
			s = store.getObjectAsString(id);
			for (int i = 0; i < (6 * n); i += 6) {
				String s2 = s.substring(i, i + 6);
				assertEquals(s2, "123456");
			}
		} finally {
			store.close();
		}
	}

	/**
	 * Tests the creating, updating, and deleting of objects in the store.
	 * This test generates object names and objects, stores the names in an 
	 * index and the objects as blobs.  Deletions and updates are done as well.
	 */
	public void testObjectLife() throws Exception {
		int n = 50000;
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		try {
			Index index = store.createIndex("index");
			IndexCursor cursor = index.open();
			ObjectID id = null;
			String name = null;
			String value = null;
			Random r = new Random(100); // same seed should generate the same test on the same VM
			for (int i = 0; i < n; i++) {
				env.print(i, 8);
				int k = Math.abs(r.nextInt());
				int k1 = k % 100; // used to gen name
				int k2 = k % 2; // used to gen operation
				name = "Object" + generateString(20, k1);
				value = "Value" + k1;
				cursor.find(name);
				if (cursor.keyEquals(name)) {
					id = cursor.getValueAsObjectID();
					String foundValue = store.getObjectAsString(id);
					assertEquals(value, foundValue);
					switch (k2) {
						case 0 :
							// delete object named x if it exists
							env.println(" Deleting  " + name);
							store.removeObject(id);
							cursor.remove();
							break;
						case 1 :
							env.println(" Updating  " + name);
							store.updateObject(id, value);
							break;
						default :
							// no operation
							env.println(" Nothing");
					}
				} else {
					env.println(" Inserting " + name);
					id = store.createObject(value);
					index.insert(name, id);
				}
				if (i % 20 == 19) {
					env.println(" Commit");
					store.commit();
				}
			}
		} finally {
			store.close();
		}
	}

	/**
	 * Tests simple recovery APIs.
	 */
	public void testRecovery() throws Exception {
		IndexedStore store = null;
		IndexedStore.delete(env.getFileName());
		store = new IndexedStore();
		store.open(env.getFileName());
		try {
			try {
				store.open(env.getFileName());
			} catch (IndexedStoreException e) {
				if (e.id != IndexedStoreException.StoreIsOpen)
					fail("expected exception did not occur");
			}
			IndexedStore store2 = IndexedStore.find(env.getFileName());
			if (store2 == null)
				fail("store looks like its not open");
			assertSame(store, store2);
		} finally {
			store.close();
		}
	}

	/**
	 * Tests simple creation and deletion of an IndexedStore.
	 */
	public void testSanity() throws Exception {
		IndexedStore.delete(env.getFileName());
		IndexedStore store = new IndexedStore();
		store.open(env.getFileName());
		store.close();
	}

	/**
	 * Tests simple transaction APIs.
	 */
	public void testTransactions() throws Exception {
		IndexedStore store = null;
		IndexedStore.delete(env.getFileName());
		store = new IndexedStore();
		store.open(env.getFileName());
		try {
			store.createIndex("Index");
			store.getIndex("Index");
			store.rollback();
			try {
				store.getIndex("Index");
			} catch (IndexedStoreException e) {
				if (e.id != IndexedStoreException.IndexNotFound)
					fail("expected exception was not thrown");
			}
			store.createIndex("Index");
			store.getIndex("Index");
			store.commit();
			store.getIndex("Index");
		} finally {
			store.close();
		}
	}
}