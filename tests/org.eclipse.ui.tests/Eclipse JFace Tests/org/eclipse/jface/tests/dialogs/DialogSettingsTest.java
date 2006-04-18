/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.dialogs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;

public class DialogSettingsTest extends TestCase {

	private static final float DELTA = 0.0000001f;

	private static final String[] TEST_STRINGS = { "value",
			" value with spaces ", "value.with.many.dots",
			"value_with_underscores", "value<with<lessthan",
			"value>with>greaterthan", "value&with&ampersand",
			"value\"with\"quote", "value#with#hash", "",
			"\nvalue\nwith\nnewlines\n", "\tvalue\twith\ttab\t",
			"\rvalue\rwith\rreturn\r", };

	public void testDialogSettings() throws IOException {
		for (int i = 0; i < TEST_STRINGS.length; i++) {
			final String name = TEST_STRINGS[i];
			testPutAndGetWithTitle(new DialogSettingsChecker() {
				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					// nothing
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					assertEquals(name, deserializedDialogSettings.getName());
				}
			}, name);
		}
	}

	public void testAddNewSection() throws IOException {
		for (int i = 0; i < TEST_STRINGS.length; i++) {
			final String name = TEST_STRINGS[i];
			testPutAndGet(new DialogSettingsChecker() {

				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					assertEquals(0,
							dialogSettingsToSerialize.getSections().length);
					assertEquals(null, dialogSettingsToSerialize
							.getSection(name));
					dialogSettingsToSerialize.addNewSection(name);
					assertEquals(1,
							dialogSettingsToSerialize.getSections().length);
					assertNotNull(dialogSettingsToSerialize.getSection(name));
					assertEquals(name, dialogSettingsToSerialize.getSection(
							name).getName());
					assertEquals(name,
							dialogSettingsToSerialize.getSections()[0]
									.getName());
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					assertEquals(1,
							deserializedDialogSettings.getSections().length);
					assertNotNull(deserializedDialogSettings.getSection(name));
					assertEquals(name, deserializedDialogSettings.getSection(
							name).getName());
					assertEquals(name,
							deserializedDialogSettings.getSections()[0]
									.getName());
				}
			});
		}
	}

	/**
	 * Helper method to fill a DialogSettings object to be checked later by
	 * check.
	 * 
	 * @param memento
	 */
	private void fill(IDialogSettings dialogSettings) {
		dialogSettings.put("booleanKey", true);
		dialogSettings.put("floatKey", 0.4f);
		dialogSettings.put("doubleKey", 0.5);
		dialogSettings.put("integerKey", 324765);
		dialogSettings.put("longKey", 1324765L);
		dialogSettings.put("stringKey", "a string");
		dialogSettings.put("stringArrayKey", new String[] { "some text data1",
				"some text data2" });
		final IDialogSettings section = dialogSettings.addNewSection("child1");
		section.addNewSection("child2");
		section.addNewSection("child3");
	}

	/**
	 * Helper method to check if the values set by fill are still there.
	 * 
	 */
	protected void check(IDialogSettings dialogSettings) {
		assertEquals(true, dialogSettings.getBoolean("booleanKey"));
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

	public void testAddSection() throws IOException {
		testPutAndGet(new DialogSettingsChecker() {

			public void prepareAndCheckBeforeSerialization(
					IDialogSettings dialogSettingsToSerialize) {
				IDialogSettings section = new DialogSettings("some section");
				fill(section);
				check(section);
				assertEquals("some section", section.getName());
				dialogSettingsToSerialize.addSection(section);
			}

			public void checkAfterDeserialization(
					IDialogSettings deserializedDialogSettings) {
				final IDialogSettings section = deserializedDialogSettings
						.getSection("some section");
				assertNotNull(section);
				assertEquals("some section", section.getName());
				check(section);
			}
		});
	}

	public void testKeys() throws IOException {
		for (int i = 0; i < TEST_STRINGS.length; i++) {
			final String key = TEST_STRINGS[i];
			testPutAndGet(new DialogSettingsChecker() {
				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					assertNull(dialogSettingsToSerialize.get(key));
					dialogSettingsToSerialize.put(key, "some string");
					assertEquals("some string", dialogSettingsToSerialize
							.get(key));
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					assertEquals("some string", deserializedDialogSettings
							.get(key));
				}
			});
		}
	}

	public void testGet() throws IOException {
		for (int i = 0; i < TEST_STRINGS.length; i++) {
			final String value = TEST_STRINGS[i];
			testPutAndGet(new DialogSettingsChecker() {

				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					assertEquals(value, dialogSettingsToSerialize
							.get("someKey"));
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					assertEquals(value, deserializedDialogSettings
							.get("someKey"));
				}
			});
		}
	}

	public void testGetArray() throws IOException {
		for (int i = 0; i < TEST_STRINGS.length; i++) {
			final String value1 = TEST_STRINGS[i];
			for (int j = 0; j < TEST_STRINGS.length; j++) {
				final String value2 = TEST_STRINGS[j];
				final String[] value = new String[] { value1, value2 };
				testPutAndGet(new DialogSettingsChecker() {

					public void prepareAndCheckBeforeSerialization(
							IDialogSettings dialogSettingsToSerialize) {
						dialogSettingsToSerialize.put("someKey", value);
						assertEquals(2, dialogSettingsToSerialize
								.getArray("someKey").length);
						assertEquals(value1, dialogSettingsToSerialize
								.getArray("someKey")[0]);
						assertEquals(value2, dialogSettingsToSerialize
								.getArray("someKey")[1]);
						dialogSettingsToSerialize.put("anotherKey1",
								new String[] {});
						// TODO see bug 98332, we should handle null cases too
						// dialogSettingsToSerialize.put("anotherKey2",
						// new String[] { null });
						// dialogSettingsToSerialize.put("anotherKey3",
						// new String[] { "string", null });
						// dialogSettingsToSerialize.put("anotherKey4",
						// new String[] { null, "string", null });
					}

					public void checkAfterDeserialization(
							IDialogSettings deserializedDialogSettings) {
						assertEquals(2, deserializedDialogSettings
								.getArray("someKey").length);
						assertEquals(value1, deserializedDialogSettings
								.getArray("someKey")[0]);
						assertEquals(value2, deserializedDialogSettings
								.getArray("someKey")[1]);
						assertEquals(0, deserializedDialogSettings
								.getArray("anotherKey1").length);
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

	public void testGetBoolean() throws IOException {
		testPutAndGet(new DialogSettingsChecker() {

			public void prepareAndCheckBeforeSerialization(
					IDialogSettings dialogSettingsToSerialize) {
				dialogSettingsToSerialize.put("true", true);
				dialogSettingsToSerialize.put("false", false);
				assertEquals(true, dialogSettingsToSerialize.getBoolean("true"));
				assertEquals(false, dialogSettingsToSerialize
						.getBoolean("false"));
			}

			public void checkAfterDeserialization(
					IDialogSettings deserializedDialogSettings) {
				assertEquals(true, deserializedDialogSettings
						.getBoolean("true"));
				assertEquals(false, deserializedDialogSettings
						.getBoolean("false"));
			}
		});
	}

	public void testGetDouble() throws IOException {
		final double[] values = new double[] { -3.1415, 1, 0, 4554.45235,
				Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN,
				Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };

		for (int i = 0; i < values.length; i++) {
			final double value = values[i];
			testPutAndGet(new DialogSettingsChecker() {
				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					final double d = dialogSettingsToSerialize
							.getDouble("someKey");
					if (Double.isNaN(value)) {
						assertTrue(Double.isNaN(d));
					} else {
						assertEquals(value, d, DELTA);
					}
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					final double d = deserializedDialogSettings
							.getDouble("someKey");
					if (Double.isNaN(value)) {
						assertTrue(Double.isNaN(d));
					} else {
						assertEquals(value, d, DELTA);
					}
				}
			});
		}
	}

	public void testGetFloat() throws IOException {
		final float[] values = new float[] { -3.1415f, 1, 0, 4554.45235f,
				Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN,
				Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY };

		for (int i = 0; i < values.length; i++) {
			final float value = values[i];
			testPutAndGet(new DialogSettingsChecker() {
				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					final float f = dialogSettingsToSerialize
							.getFloat("someKey");
					if (Float.isNaN(value)) {
						assertTrue(Float.isNaN(f));
					} else {
						assertEquals(value, f, DELTA);
					}
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					final float f = deserializedDialogSettings
							.getFloat("someKey");
					if (Float.isNaN(value)) {
						assertTrue(Float.isNaN(f));
					} else {
						assertEquals(value, f, DELTA);
					}
				}
			});
		}
	}

	public void testGetInt() throws IOException {
		int[] values = new int[] { 36254, 0, 1, -36254, Integer.MAX_VALUE,
				Integer.MIN_VALUE };

		for (int i = 0; i < values.length; i++) {
			final int value = values[i];
			testPutAndGet(new DialogSettingsChecker() {

				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					assertEquals(value, dialogSettingsToSerialize
							.getInt("someKey"));
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					assertEquals(value, deserializedDialogSettings
							.getInt("someKey"));
				}
			});
		}
	}

	public void testGetLong() throws IOException {
		long[] values = new long[] { 36254L, 0L, 1L, -36254L, Long.MAX_VALUE,
				Long.MIN_VALUE };

		for (int i = 0; i < values.length; i++) {
			final long value = values[i];
			testPutAndGet(new DialogSettingsChecker() {

				public void prepareAndCheckBeforeSerialization(
						IDialogSettings dialogSettingsToSerialize) {
					dialogSettingsToSerialize.put("someKey", value);
					assertEquals(value, dialogSettingsToSerialize
							.getLong("someKey"));
				}

				public void checkAfterDeserialization(
						IDialogSettings deserializedDialogSettings) {
					assertEquals(value, deserializedDialogSettings
							.getLong("someKey"));
				}
			});
		}

	}

	public void testGetSection() {
	}

	public void testGetSections() {
	}

	private static interface DialogSettingsChecker {
		void prepareAndCheckBeforeSerialization(
				IDialogSettings dialogSettingsToSerialize);

		void checkAfterDeserialization(
				IDialogSettings deserializedDialogSettings);
	}

	private void testPutAndGet(DialogSettingsChecker dialogSettingsChecker)
			throws IOException {
		testPutAndGetWithTitle(dialogSettingsChecker, "DialogSettingsTest");
	}

	private void testPutAndGetWithTitle(
			DialogSettingsChecker dialogSettingsChecker, String sectionName)
			throws IOException {
		IDialogSettings dialogSettingsToSerialize = new DialogSettings(
				sectionName);

		dialogSettingsChecker
				.prepareAndCheckBeforeSerialization(dialogSettingsToSerialize);

		StringWriter writer = new StringWriter();
		dialogSettingsToSerialize.save(writer);
		writer.close();

		StringReader reader = new StringReader(writer.getBuffer().toString());
		DialogSettings deserializedDialogSettings = new DialogSettings("");
		deserializedDialogSettings.load(reader);

		dialogSettingsChecker
				.checkAfterDeserialization(deserializedDialogSettings);
	}

}
