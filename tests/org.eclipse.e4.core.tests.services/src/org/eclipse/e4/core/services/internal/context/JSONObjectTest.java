/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.util.JSONObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for {@link JSONObject}.
 */
public class JSONObjectTest extends TestCase {
	public static Test suite() {
		return new TestSuite(JSONObjectTest.class);
	}

	public JSONObjectTest() {
		super("");
	}

	public JSONObjectTest(String name) {
		super(name);
	}

	public void testSimpleRoundTrip() {
		JSONObject input = new JSONObject();
		input.set("x", "value");
		String[] valueArray = new String[] { "a", "value", "array" };
		input.set("y", valueArray);
		JSONObject child = new JSONObject();
		String childValue = "\b\f\n\r\t";
		child.set("z", childValue);
		input.set("child", child);
		String result = input.serialize();

		JSONObject output = JSONObject.deserialize(result);
		assertEquals("value", output.getString("x"));
		String[] childStrings = output.getStrings("y");
		assertEquals(valueArray.length, childStrings.length);
		for (int i = 0; i < childStrings.length; i++) {
			assertEquals(valueArray[i], childStrings[i]);
		}
		JSONObject outputChild = output.getObject("child");
		assertEquals(childValue, outputChild.getString("z"));
	}
}
