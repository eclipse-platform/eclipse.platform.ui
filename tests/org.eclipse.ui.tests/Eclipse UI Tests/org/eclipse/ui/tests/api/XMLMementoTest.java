/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.junit.Test;

/**
 * Testing XMLMemento (see bug 93262). Emphasis is on ensuring that the 3.1
 * version behaves just like the 3.0.1 version.
 *
 * @since 3.1
 */
public class XMLMementoTest {

	private static final String[] TEST_STRINGS = { "value",
			" value with spaces ", "value.with.many.dots",
			"value_with_underscores", "value<with<lessthan",
			"value>with>greaterthan", "value&with&ampersand",
			"value\"with\"quote", "value#with#hash", "",
			/*
			 * the following cases are for bug 93720
			 */
			"\nvalue\nwith\nnewlines\n", "\tvalue\twith\ttab\t",
			"\rvalue\rwith\rreturn\r", };

	/*
	 * Class under test for XMLMemento createReadRoot(Reader)
	 */
	@Test
	public void testCreateReadRootReaderExceptionCases() {
		assertThrows(WorkbenchException.class, () -> XMLMemento.createReadRoot(new StringReader("Invalid format")));
		assertThrows("no exception even though there is noe element", WorkbenchException.class,
				() -> XMLMemento.createReadRoot(new StringReader("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>")));
		assertThrows(WorkbenchException.class, () ->
			XMLMemento.createReadRoot(new Reader() {

				@Override
				public void close() throws IOException {
					throw new IOException();
				}

				@Override
				public int read(char[] arg0, int arg1, int arg2)
						throws IOException {
					throw new IOException();
				}
		}));
	}

	@Test
	public void testCreateReadRootReader() throws WorkbenchException {
		XMLMemento memento = XMLMemento
				.createReadRoot(new StringReader(
						"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><simple>some text data</simple>"));
		assertEquals("some text data", memento.getTextData());
	}

	/*
	 * Class under test for XMLMemento createReadRoot(Reader, String)
	 */
	@Test
	public void testCreateReadRootReaderString() {
		// TODO - I don't know how to test this. The method is not called by
		// anyone as of 2005/04/05.
	}

	@Test
	public void testCreateWriteRoot() {
		String[] rootTypes = { "type", "type.with.dots",
				"type_with_underscores" };
		for (String type : rootTypes) {
			XMLMemento memento = XMLMemento.createWriteRoot(type);
			assertNotNull(memento);
		}
	}

	@Test
	public void testSpacesInRootAreIllegal() {
		assertThrows(Exception.class, () -> XMLMemento.createWriteRoot("with space"));
	}

	@Test
	public void testSpacesInKeysAreIllegal() {
		XMLMemento memento = XMLMemento.createWriteRoot("foo");
		assertThrows(Exception.class, () -> memento.createChild("with space", "bar"));
		assertThrows(Exception.class, () -> memento.putString("with space", "bar"));
	}

	@Test
	public void testCopyChild() throws WorkbenchException, IOException {

		testPutAndGet(new MementoChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(
					XMLMemento mementoToSerialize) {
				IMemento child = mementoToSerialize.createChild("c", "i");
				fillMemento(child);
				IMemento copiedChild = mementoToSerialize.copyChild(child);
				assertEquals("i", copiedChild.getID());
				checkMemento(copiedChild, true);
			}

			@Override
			public void checkAfterDeserialization(XMLMemento deserializedMemento) {
				IMemento child = deserializedMemento.getChild("c");
				checkMemento(child, true);
				IMemento[] children = deserializedMemento.getChildren("c");
				assertEquals(2, children.length);
				assertEquals("i", children[0].getID());
				checkMemento(children[0], true);
				assertEquals("i", children[1].getID());
				checkMemento(children[1], true);
			}
		});
	}

	/**
	 * Helper method to fill a memento to be checked later by checkMemento.
	 */
	private void fillMemento(IMemento memento) {
		memento.putFloat("floatKey", 0.4f);
		memento.putInteger("integerKey", 324765);
		memento.putString("stringKey", "a string");
		memento.putTextData("some text data");
		memento.createChild("child1");
		memento.createChild("child2", "child2id1");
		memento.createChild("child2", "child2id2");
	}

	/**
	 * Helper method to check if the values set by fillMemento are still there.
	 * The boolean parameter is needed because in some cases
	 * (IMememento#putMemento), the text data gets lost.
	 */
	protected void checkMemento(IMemento memento, boolean checkForTextData) {
		assertEquals(0.4f, memento.getFloat("floatKey").floatValue(), 0.0f);
		assertEquals(324765, memento.getInteger("integerKey").intValue());
		assertEquals("a string", memento.getString("stringKey"));
		if (checkForTextData) {
			assertEquals("some text data", memento.getTextData());
		}
		IMemento child1 = memento.getChild("child1");
		assertNotNull(child1);
		IMemento child2 = memento.getChild("child2");
		assertNotNull(child2);
		assertEquals("child2id1", child2.getID());
		IMemento[] children = memento.getChildren("child2");
		assertNotNull(children);
		assertEquals(2, children.length);
		assertEquals("child2id1", children[0].getID());
		assertEquals("child2id2", children[1].getID());
	}

	@Test
	public void testCreateAndGetChild() throws WorkbenchException, IOException {
		final String type1 = "type1";
		final String type2 = "type2";
		final String id = "id";

		testPutAndGet(new MementoChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(
					XMLMemento mementoToSerialize) {
				// check that nothing is there yet
				assertEquals(null, mementoToSerialize.getChild(type1));
				assertEquals(null, mementoToSerialize.getChild(type2));

				// creation without ID
				IMemento child1 = mementoToSerialize.createChild(type1);
				assertNotNull(child1);
				assertNotNull(mementoToSerialize.getChild(type1));

				// creation with ID
				IMemento child2 = mementoToSerialize.createChild(type2, id);
				assertNotNull(child2);
				assertNotNull(mementoToSerialize.getChild(type2));
				assertEquals(id, child2.getID());
			}

			@Override
			public void checkAfterDeserialization(XMLMemento deserializedMemento) {
				IMemento child1 = deserializedMemento.getChild(type1);
				assertNotNull(child1);
				IMemento child2 = deserializedMemento.getChild(type2);
				assertNotNull(child2);
				assertEquals(id, child2.getID());
			}
		});
	}

	@Test
	public void testGetChildren() throws WorkbenchException, IOException {
		final String type = "type";
		final String id1 = "id";
		final String id2 = "id2";

		testPutAndGet(new MementoChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(
					XMLMemento mementoToSerialize) {
				// check that nothing is there yet
				assertEquals(null, mementoToSerialize.getChild(type));

				IMemento child1 = mementoToSerialize.createChild(type, id1);
				assertNotNull(child1);
				assertNotNull(mementoToSerialize.getChild(type));
				assertEquals(id1, child1.getID());

				// second child with the same type
				IMemento child2 = mementoToSerialize.createChild(type, id2);
				assertNotNull(child2);
				assertEquals(2, mementoToSerialize.getChildren(type).length);
				assertEquals(id2, child2.getID());
			}

			@Override
			public void checkAfterDeserialization(XMLMemento deserializedMemento) {
				IMemento[] children = deserializedMemento.getChildren(type);
				assertNotNull(children);
				assertEquals(2, children.length);

				// this checks that the order is maintained.
				// the spec does not promise this, but clients
				// may rely on the current implementation behaviour.
				assertEquals(id1, children[0].getID());
				assertEquals(id2, children[1].getID());
			}
		});
	}

	@Test
	public void testGetID() throws WorkbenchException, IOException {
		final String type = "type";

		String[] ids = { "id", "", "id.with.many.dots", "id_with_underscores",
				"id<with<lessthan", "id>with>greaterthan", "id&with&ampersand",
				"id\"with\"quote", "id#with#hash" };

		for (final String id : ids) {
			testPutAndGet(new MementoChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(
						XMLMemento mementoToSerialize) {
					assertEquals(null, mementoToSerialize.getChild(type));
					IMemento child = mementoToSerialize.createChild(type, id);
					assertEquals(id, child.getID());
				}

				@Override
				public void checkAfterDeserialization(
						XMLMemento deserializedMemento) {
					IMemento child = deserializedMemento.getChild(type);
					assertNotNull(child);
					assertEquals(id, child.getID());
				}
			});
		}
	}

	@Test
	public void testPutAndGetFloat() throws WorkbenchException, IOException {
		final String key = "key";

		final Float[] values = new Float[] { Float.valueOf((float) -3.1415), Float.valueOf(1),
				Float.valueOf(0), Float.valueOf((float) 4554.45235),
				Float.valueOf(Float.MAX_VALUE), Float.valueOf(Float.MIN_VALUE),
				Float.valueOf(Float.NaN), Float.valueOf(Float.POSITIVE_INFINITY),
				Float.valueOf(Float.NEGATIVE_INFINITY) };

		for (final Float value : values) {
			testPutAndGet(new MementoChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(
						XMLMemento mementoToSerialize) {
					assertEquals(null, mementoToSerialize.getFloat(key));
					mementoToSerialize.putFloat(key, value.floatValue());
					assertEquals(value, mementoToSerialize.getFloat(key));
				}

				@Override
				public void checkAfterDeserialization(
						XMLMemento deserializedMemento) {
					assertEquals(value, deserializedMemento.getFloat(key));
				}
			});
		}
	}

	@Test
	public void testPutAndGetInteger() throws WorkbenchException, IOException {
		final String key = "key";

		Integer[] values = new Integer[] { Integer.valueOf(36254), Integer.valueOf(0),
				Integer.valueOf(1), Integer.valueOf(-36254),
				Integer.valueOf(Integer.MAX_VALUE), Integer.valueOf(Integer.MIN_VALUE) };

		for (final Integer value : values) {
			testPutAndGet(new MementoChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(
						XMLMemento mementoToSerialize) {
					assertEquals(null, mementoToSerialize.getInteger(key));
					mementoToSerialize.putInteger(key, value.intValue());
					assertEquals(value, mementoToSerialize.getInteger(key));
				}

				@Override
				public void checkAfterDeserialization(
						XMLMemento deserializedMemento) {
					assertEquals(value, deserializedMemento.getInteger(key));
				}
			});
		}

	}

	@Test
	public void testPutMemento() throws WorkbenchException, IOException {
		testPutAndGet(new MementoChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(
					XMLMemento mementoToSerialize) {
				mementoToSerialize.putTextData("unchanged text data");
				mementoToSerialize.putString("neverlost", "retained value");

				IMemento aMemento = XMLMemento.createWriteRoot("foo");
				fillMemento(aMemento);

				// note that this does not copy the text data:
				mementoToSerialize.putMemento(aMemento);

				// do not check for text data:
				checkMemento(mementoToSerialize, false);

				assertEquals("unchanged text data", mementoToSerialize
						.getTextData());
				assertEquals("retained value", mementoToSerialize
						.getString("neverlost"));
			}

			@Override
			public void checkAfterDeserialization(XMLMemento deserializedMemento) {
				// do not check for text data:
				checkMemento(deserializedMemento, false);

				assertEquals("unchanged text data", deserializedMemento
						.getTextData());
				assertEquals("retained value", deserializedMemento
						.getString("neverlost"));
			}
		});
	}

	@Test
	public void testPutAndGetString() throws IOException, WorkbenchException {
		final String key = "key";

		// values with newline, tab, or return characters lead to bug 93720.
		String[] values = TEST_STRINGS;

		for (final String value : values) {
			testPutAndGet(new MementoChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(
						XMLMemento mementoToSerialize) {
					assertEquals(null, mementoToSerialize.getString(key));
					mementoToSerialize.putString(key, value);
					assertEquals(value, mementoToSerialize.getString(key));
				}

				@Override
				public void checkAfterDeserialization(
						XMLMemento deserializedMemento) {
					assertEquals(value, deserializedMemento.getString(key));
				}
			});
		}
	}

	@Test
	public void testPutAndGetTextData() throws WorkbenchException, IOException {
		String[] values = TEST_STRINGS;

		for (final String data : values) {
			testPutAndGet(new MementoChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(
						XMLMemento mementoToSerialize) {
					assertEquals(null, mementoToSerialize.getTextData());
					mementoToSerialize.putTextData(data);
					assertEquals(data, mementoToSerialize.getTextData());
				}

				@Override
				public void checkAfterDeserialization(
						XMLMemento deserializedMemento) {
					if (data.isEmpty()) {
						// this comes back as null...
						assertEquals(null, deserializedMemento.getTextData());
					} else {
						assertEquals(data, deserializedMemento.getTextData());
					}
				}
			});
		}
	}

	@Test
	public void testLegalKeys() throws WorkbenchException, IOException {
		String[] legalKeys = { "value", "value.with.many.dots",
				"value_with_underscores" };

		for (final String key : legalKeys) {
			testPutAndGet(new MementoChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(
						XMLMemento mementoToSerialize) {
					assertEquals(null, mementoToSerialize.getString(key));
					try {
						mementoToSerialize.putString(key, "some string");
					} catch (RuntimeException ex) {
						System.out.println("offending key: '" + key + "'");
						throw ex;
					}
					assertEquals("some string", mementoToSerialize
							.getString(key));
				}

				@Override
				public void checkAfterDeserialization(
						XMLMemento deserializedMemento) {
					assertEquals("some string", deserializedMemento
							.getString(key));
				}
			});
		}

	}

	@Test
	public void testIllegalKeys() {
		String[] illegalKeys = { "", " ", " key", "key ", "key key", "\t",
				"\tkey", "key\t", "key\tkey", "\n", "\nkey", "key\n",
				"key\nkey", "key<with<lessthan", "key>with>greaterthan",
				"key&with&ampersand", "key#with#hash", "key\"with\"quote", "\"" };

		for (final String key : illegalKeys) {
			XMLMemento memento = XMLMemento.createWriteRoot("foo");
			assertThrows("should fail with illegal key", Exception.class, () -> memento.putString(key, "some string"));
		}
	}

	@Test
	public void testPutTextDataWithChildrenBug93718()
			throws WorkbenchException, IOException {
		final String textData = "\n\tThis is\ntext data\n\t\twith newlines and \ttabs\t\n\t ";
		testPutAndGet(new MementoChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(
					XMLMemento mementoToSerialize) {
				mementoToSerialize.createChild("type", "id");
				mementoToSerialize.putTextData(textData);
				mementoToSerialize.createChild("type", "id");
				mementoToSerialize.createChild("type", "id");
				assertEquals(textData, mementoToSerialize.getTextData());
			}

			@Override
			public void checkAfterDeserialization(XMLMemento deserializedMemento) {
				assertEquals(textData, deserializedMemento.getTextData());
			}
		});
	}

	private static interface MementoChecker {
		void prepareAndCheckBeforeSerialization(XMLMemento mementoToSerialize);

		void checkAfterDeserialization(XMLMemento deserializedMemento);
	}

	private void testPutAndGet(MementoChecker mementoChecker)
			throws IOException, WorkbenchException {
		XMLMemento mementoToSerialize = XMLMemento
				.createWriteRoot("XMLMementoTest");

		mementoChecker.prepareAndCheckBeforeSerialization(mementoToSerialize);

		try (StringWriter writer = new StringWriter()) {
			mementoToSerialize.save(writer);
			StringReader reader = new StringReader(writer.getBuffer().toString());
			XMLMemento deserializedMemento = XMLMemento.createReadRoot(reader);
			mementoChecker.checkAfterDeserialization(deserializedMemento);
		}


	}

		@Test
		public void testMementoWithTextContent113659() throws Exception {
			IMemento memento = XMLMemento.createWriteRoot("root");
			IMemento mementoWithChild = XMLMemento.createWriteRoot("root");
			IMemento child = mementoWithChild.createChild("child");
			child.putTextData("text");
			memento.putMemento(mementoWithChild);
			IMemento copiedChild = memento.getChild("child");
			assertEquals("text", copiedChild.getTextData());
		}



}
