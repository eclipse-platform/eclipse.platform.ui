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
package org.eclipse.core.tests.resources.regression;

import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests regression of bug 97608 - error restoring workspace
 * after writing marker with value that is too long.
 */
public class Bug_097608 extends ResourceTest {
	public static Test suite() {
		return new TestSuite(Bug_097608.class);
	}

	/**
	 * Tests that creating a marker with very long value causes failure.
	 */
	public void testBug() {
		char[] chars = new char[40000];
		Arrays.fill(chars, 'a');
		String value = new String(chars);
		IMarker marker = null;
		try {
			marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.MARKER);
			//first try a long value under the limit
			marker.setAttribute(IMarker.MESSAGE, value);
		} catch (CoreException e) {
			fail("0.99", e);
		}
		//now create a marker with illegal length attribute
		value = value + value;
		try {
			marker.setAttribute(IMarker.MESSAGE, value);
			fail("1.0");
		} catch (RuntimeException e) {
			//expected
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//try a string with less than 65536 characters whose UTF encoding exceeds the limit
		Arrays.fill(chars, (char) 0x0800);
		value = new String(chars);
		try {
			marker.setAttribute(IMarker.MESSAGE, value);
			fail("2.0");
		} catch (RuntimeException e) {
			//expected
		} catch (CoreException e) {
			fail("2.99", e);
		}
	}
}
