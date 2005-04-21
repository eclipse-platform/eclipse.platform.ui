/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import java.io.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.tests.util.UITestCase;

public class ScopedPreferenceStoreTestCase extends UITestCase {

	final String DEFAULT_DEFAULT_STRING = "";

	public ScopedPreferenceStoreTestCase(String name) {
		super(name);
	}

	/**
	 * Fails the test due to the given throwable.
	 */
	public static void fail(String message, Throwable e) {
		// If the exception is a CoreException with a multistatus
		// then print out the multistatus so we can see all the info.
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			write(status, 0);
		} else
			e.printStackTrace();
		fail(message + ": " + e);
	}

	private static void indent(OutputStream output, int indent) {
		for (int i = 0; i < indent; i++)
			try {
				output.write("\t".getBytes());
			} catch (IOException e) {
				// ignore
			}
	}

	private static void write(IStatus status, int indent) {
		PrintStream output = System.out;
		indent(output, indent);
		output.println("Severity: " + status.getSeverity());

		indent(output, indent);
		output.println("Plugin ID: " + status.getPlugin());

		indent(output, indent);
		output.println("Code: " + status.getCode());

		indent(output, indent);
		output.println("Message: " + status.getMessage());

		if (status.getException() != null) {
			indent(output, indent);
			output.print("Exception: ");
			status.getException().printStackTrace(output);
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++)
				write(children[i], indent + 1);
		}
	}

	public void testNeedsSaving() {
		IScopeContext context = new InstanceScope();
		String qualifier = "org.eclipse.ui.tests.preferences";
		ScopedPreferenceStore store = new ScopedPreferenceStore(context,
				qualifier);
		String key = "key1";
		String value = "value1";

		// nothing there
		assertFalse("0.1", store.needsSaving());
		assertFalse("0.2", store.contains(key));
		assertEquals("0.3", DEFAULT_DEFAULT_STRING, store.getString(key));

		// set the value
		store.setValue(key, value);
		assertTrue("1.0", store.needsSaving());
		assertTrue("1.1", store.contains(key));
		assertEquals("1.2", value, store.getString(key));

		// flush
		try {
			store.save();
		} catch (IOException e) {
			fail("2.99", e);
		}

		// do the test
		assertFalse("3.0", store.needsSaving());

		// change the node outside of the scoped store
		String key2 = "key2";
		String value2 = "value2";
		IEclipsePreferences node = context.getNode(qualifier);
		node.put(key2, value2);
		assertEquals("4.0", value2, node.get(key2, null));
		assertFalse("4.1", store.needsSaving());
	}

}