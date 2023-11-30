/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Marc R. Hoffmann <hoffmann@mountainminds.com> - Bug 284265 [JFace]
 *                  DialogSettings.save() silently ignores IOException
 *******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.junit.Test;

public class DialogSettingsTest {

	private static final float DELTA = 0.0000001f;

	private static final String[] TEST_STRINGS = { "value", " value with spaces ", "value.with.many.dots",
			"value_with_underscores", "value<with<lessthan", "value>with>greaterthan", "value&with&ampersand",
			"value\"with\"quote", "value#with#hash", "", "\nvalue\nwith\nnewlines\n", "\tvalue\twith\ttab\t",
			"\rvalue\rwith\rreturn\r", };

	@Test
	public void testDialogSettings() throws IOException {
		for (String testString : TEST_STRINGS) {
			final String name = testString;
			testPutAndGetWithTitle(new DialogSettingsChecker() {
				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					// nothing
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					assertEquals(name, deserializedDialogSettings.getName());
				}
			}, name);
		}
	}

	@Test
	public void testAddNewSection() throws IOException {
		for (String testString : TEST_STRINGS) {
			final String name = testString;
			testPutAndGet(new DialogSettingsChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					assertEquals(0, dialogSettingsToSerialize.getSections().length);
					assertEquals(null, dialogSettingsToSerialize.getSection(name));
					dialogSettingsToSerialize.addNewSection(name);
					assertEquals(1, dialogSettingsToSerialize.getSections().length);
					assertNotNull(dialogSettingsToSerialize.getSection(name));
					assertEquals(name, dialogSettingsToSerialize.getSection(name).getName());
					assertEquals(name, dialogSettingsToSerialize.getSections()[0].getName());
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					assertEquals(1, deserializedDialogSettings.getSections().length);
					assertNotNull(deserializedDialogSettings.getSection(name));
					assertEquals(name, deserializedDialogSettings.getSection(name).getName());
					assertEquals(name, deserializedDialogSettings.getSections()[0].getName());
				}
			});
		}
	}

	/**
	 * Helper method to fill a DialogSettings object to be checked later by check.
	 */
	private static void fill(IDialogSettings dialogSettings) {
		dialogSettings.put("booleanKey", true);
		dialogSettings.put("floatKey", 0.4f);
		dialogSettings.put("doubleKey", 0.5);
		dialogSettings.put("integerKey", 324765);
		dialogSettings.put("longKey", 1324765L);
		dialogSettings.put("stringKey", "a string");
		dialogSettings.put("stringArrayKey", new String[] { "some text data1", "some text data2" });
		final IDialogSettings section = dialogSettings.addNewSection("child1");
		section.addNewSection("child2");
		section.addNewSection("child3");
	}

	/**
	 * Helper method to check if the values set by fill are still there.
	 */
	protected void check(IDialogSettings dialogSettings) {
		assertTrue(dialogSettings.getBoolean("booleanKey"));
		assertEquals(0.4f, dialogSettings.getFloat("floatKey"), 0.4f);
		assertEquals(0.4f, dialogSettings.getDouble("doubleKey"), 0.5);
		assertEquals(324765, dialogSettings.getInt("integerKey"));
		assertEquals(1324765L, dialogSettings.getLong("longKey"));
		assertEquals("a string", dialogSettings.get("stringKey"));
		String[] stringArray = dialogSettings.getArray("stringArrayKey");
		assertEquals(2, stringArray.length);
		assertEquals("some text data1", stringArray[0]);
		assertEquals("some text data2", stringArray[1]);
		IDialogSettings section = dialogSettings.getSection("child1");
		assertNotNull(section);
		assertNotNull(section.getSection("child2"));
		assertNotNull(section.getSection("child3"));
	}

	@Test
	public void testAddSection() throws IOException {
		testPutAndGet(new DialogSettingsChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
				IDialogSettings section = new DialogSettings("some section");
				fill(section);
				check(section);
				assertEquals("some section", section.getName());
				dialogSettingsToSerialize.addSection(section);
			}

			@Override
			public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
				final IDialogSettings section = deserializedDialogSettings.getSection("some section");
				assertNotNull(section);
				assertEquals("some section", section.getName());
				check(section);
			}
		});
	}

	@Test
	public void testRemoveSection() {
		DialogSettings dialogSettings = new DialogSettings(null);
		IDialogSettings section = dialogSettings.addNewSection("new-section");
		assertEquals(1, dialogSettings.getSections().length);

		dialogSettings.removeSection(section);

		assertEquals(0, dialogSettings.getSections().length);
	}

	@Test
	public void testRemoveSectionByName() {
		DialogSettings dialogSettings = new DialogSettings(null);
		IDialogSettings section = dialogSettings.addNewSection("new-section");
		assertEquals(1, dialogSettings.getSections().length);

		final IDialogSettings removedSection = dialogSettings.removeSection("new-section");

		assertEquals(0, dialogSettings.getSections().length);
		assertEquals(section, removedSection);
	}

	@Test
	public void testRemoveNonExistingSection() {
		DialogSettings dialogSettings = new DialogSettings(null);
		dialogSettings.addNewSection("new-section");
		assertEquals(1, dialogSettings.getSections().length);
		IDialogSettings otherSection = new DialogSettings(null);

		dialogSettings.removeSection(otherSection);

		assertEquals(1, dialogSettings.getSections().length);
	}

	@Test
	public void testRemoveOtherSection() {
		DialogSettings dialogSettings = new DialogSettings(null);
		dialogSettings.addNewSection("new-section");
		assertEquals(1, dialogSettings.getSections().length);
		IDialogSettings otherSection = new DialogSettings("new-section");

		dialogSettings.removeSection(otherSection);

		assertEquals(1, dialogSettings.getSections().length);
	}

	@Test
	public void testRemoveSectionWithNullArgument() {
		DialogSettings dialogSettings = new DialogSettings(null);

		try {
			dialogSettings.removeSection((IDialogSettings) null);
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testKeys() throws IOException {
		for (String testString : TEST_STRINGS) {
			final String key = testString;
			testPutAndGet(new DialogSettingsChecker() {
				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					assertNull(dialogSettingsToSerialize.get(key));
					dialogSettingsToSerialize.put(key, "some string");
					assertEquals("some string", dialogSettingsToSerialize.get(key));
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					assertEquals("some string", deserializedDialogSettings.get(key));
				}
			});
		}
	}

	@Test
	public void testGet() throws IOException {
		for (String testString : TEST_STRINGS) {
			final String value = testString;
			testPutAndGet(new DialogSettingsChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					assertEquals(value, dialogSettingsToSerialize.get("someKey"));
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					assertEquals(value, deserializedDialogSettings.get("someKey"));
				}
			});
		}
	}

	@Test
	public void testGetArray() throws IOException {
		for (String testString : TEST_STRINGS) {
			final String value1 = testString;
			for (String otherTestString : TEST_STRINGS) {
				final String value2 = otherTestString;
				final String[] value = new String[] { value1, value2 };
				testPutAndGet(new DialogSettingsChecker() {

					@Override
					public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
						dialogSettingsToSerialize.put("someKey", value);
						assertEquals(2, dialogSettingsToSerialize.getArray("someKey").length);
						assertEquals(value1, dialogSettingsToSerialize.getArray("someKey")[0]);
						assertEquals(value2, dialogSettingsToSerialize.getArray("someKey")[1]);
						dialogSettingsToSerialize.put("anotherKey1", new String[] {});
						// TODO see bug 98332, we should handle null cases too
						// dialogSettingsToSerialize.put("anotherKey2",
						// new String[] { null });
						// dialogSettingsToSerialize.put("anotherKey3",
						// new String[] { "string", null });
						// dialogSettingsToSerialize.put("anotherKey4",
						// new String[] { null, "string", null });
					}

					@Override
					public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
						assertEquals(2, deserializedDialogSettings.getArray("someKey").length);
						assertEquals(value1, deserializedDialogSettings.getArray("someKey")[0]);
						assertEquals(value2, deserializedDialogSettings.getArray("someKey")[1]);
						assertEquals(0, deserializedDialogSettings.getArray("anotherKey1").length);
						// TODO see bug 98332, we should handle null cases too
						// assertEquals(1, deserializedDialogSettings
						// .getArray("anotherKey2").length);
						// assertEquals(null, deserializedDialogSettings
						// .getArray("anotherKey2")[0]);
						// assertEquals(2, deserializedDialogSettings
						// .getArray("anotherKey3").length);
						// assertEquals("string", deserializedDialogSettings
						// .getArray("anotherKey3")[0]);
						// assertEquals(null, deserializedDialogSettings
						// .getArray("anotherKey3")[1]);
						// assertEquals(3, deserializedDialogSettings
						// .getArray("anotherKey4").length);
						// assertEquals(null, deserializedDialogSettings
						// .getArray("anotherKey4")[0]);
						// assertEquals("string", deserializedDialogSettings
						// .getArray("anotherKey4")[1]);
						// assertEquals(null, deserializedDialogSettings
						// .getArray("anotherKey4")[2]);
					}
				});
			}
		}
	}

	@Test
	public void testGetBoolean() throws IOException {
		testPutAndGet(new DialogSettingsChecker() {

			@Override
			public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
				dialogSettingsToSerialize.put("true", true);
				dialogSettingsToSerialize.put("false", false);
				assertTrue(dialogSettingsToSerialize.getBoolean("true"));
				assertFalse(dialogSettingsToSerialize.getBoolean("false"));
			}

			@Override
			public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
				assertTrue(deserializedDialogSettings.getBoolean("true"));
				assertFalse(deserializedDialogSettings.getBoolean("false"));
			}
		});
	}

	@Test
	public void testGetDouble() throws IOException {
		final double[] values = new double[] { -3.1415, 1, 0, 4554.45235, Double.MAX_VALUE, Double.MIN_VALUE,
				Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };

		for (double testValue : values) {
			final double value = testValue;
			testPutAndGet(new DialogSettingsChecker() {
				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					final double d = dialogSettingsToSerialize.getDouble("someKey");
					if (Double.isNaN(value)) {
						assertTrue(Double.isNaN(d));
					} else {
						assertEquals(value, d, DELTA);
					}
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					final double d = deserializedDialogSettings.getDouble("someKey");
					if (Double.isNaN(value)) {
						assertTrue(Double.isNaN(d));
					} else {
						assertEquals(value, d, DELTA);
					}
				}
			});
		}
	}

	@Test
	public void testGetFloat() throws IOException {
		final float[] values = new float[] { -3.1415f, 1, 0, 4554.45235f, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN,
				Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY };

		for (float testValue : values) {
			final float value = testValue;
			testPutAndGet(new DialogSettingsChecker() {
				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					final float f = dialogSettingsToSerialize.getFloat("someKey");
					if (Float.isNaN(value)) {
						assertTrue(Float.isNaN(f));
					} else {
						assertEquals(value, f, DELTA);
					}
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					final float f = deserializedDialogSettings.getFloat("someKey");
					if (Float.isNaN(value)) {
						assertTrue(Float.isNaN(f));
					} else {
						assertEquals(value, f, DELTA);
					}
				}
			});
		}
	}

	@Test
	public void testGetInt() throws IOException {
		int[] values = new int[] { 36254, 0, 1, -36254, Integer.MAX_VALUE, Integer.MIN_VALUE };

		for (int testValue : values) {
			final int value = testValue;
			testPutAndGet(new DialogSettingsChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					assertEquals(value, dialogSettingsToSerialize.getInt("someKey"));
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					assertEquals(value, deserializedDialogSettings.getInt("someKey"));
				}
			});
		}
	}

	@Test
	public void testGetLong() throws IOException {
		long[] values = new long[] { 36254L, 0L, 1L, -36254L, Long.MAX_VALUE, Long.MIN_VALUE };

		for (long testValue : values) {
			final long value = testValue;
			testPutAndGet(new DialogSettingsChecker() {

				@Override
				public void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					assertEquals(value, dialogSettingsToSerialize.getLong("someKey"));
				}

				@Override
				public void checkAfterDeserialization(IDialogSettings deserializedDialogSettings) {
					assertEquals(value, deserializedDialogSettings.getLong("someKey"));
				}
			});
		}

	}

	private interface DialogSettingsChecker {
		void prepareAndCheckBeforeSerialization(IDialogSettings dialogSettingsToSerialize);

		void checkAfterDeserialization(IDialogSettings deserializedDialogSettings);
	}

	private static void testPutAndGet(DialogSettingsChecker dialogSettingsChecker) throws IOException {
		testPutAndGetWithTitle(dialogSettingsChecker, "DialogSettingsTest");
	}

	private static void testPutAndGetWithTitle(DialogSettingsChecker dialogSettingsChecker, String sectionName)
			throws IOException {
		IDialogSettings dialogSettingsToSerialize = new DialogSettings(sectionName);

		dialogSettingsChecker.prepareAndCheckBeforeSerialization(dialogSettingsToSerialize);

		try (StringWriter writer = new StringWriter()) {
			dialogSettingsToSerialize.save(writer);
			StringReader reader = new StringReader(writer.getBuffer().toString());
			DialogSettings deserializedDialogSettings = new DialogSettings("");
			deserializedDialogSettings.load(reader);

			dialogSettingsChecker.checkAfterDeserialization(deserializedDialogSettings);
		}

	}

	@Test
	@SuppressWarnings("resource")
	public void testSaveWithIOException() {
		final DialogSettings settings = new DialogSettings("test");
		try {
			settings.save(new BrokenWriter());
			fail("IOException expected");
		} catch (IOException e) {
		}
	}

	private static class BrokenWriter extends Writer {

		@Override
		public void write(final char[] cbuf, final int off, final int len) throws IOException {
			throw new IOException("Bang!");
		}

		@Override
		public void close() throws IOException {
			throw new IOException("Bang!");
		}

		@Override
		public void flush() throws IOException {
			throw new IOException("Bang!");
		}

	}

}
