/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
 * Tests regression of bug 288315 - MarkerInfo.setAttributes(Map map) calls
 * checkValidAttribute which throws IllegalArgumentException.
 */
public class Bug_288315 extends ResourceTest {

	public static Test suite() {
		return new TestSuite(Bug_288315.class);
	}

	/**
	 * Tests that creating a marker with very long value causes failure.
	 */
	public void testBug() throws CoreException {
		char[] chars = new char[65537];
		Arrays.fill(chars, 'a');
		String value1 = new String(chars);
		Float value2 = new Float(1.1);
		
		IMarker nonPersistentMarker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.MARKER);
		try {
			nonPersistentMarker.setAttribute(IMarker.MESSAGE, value1);
			nonPersistentMarker.setAttribute(IMarker.MESSAGE, value2);
		} catch (RuntimeException e) {
			fail("1.0", e);
		} catch (CoreException e) {
			fail("1.1", e);
		}
		nonPersistentMarker.delete();

		IMarker persistentMarker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IMarker.PROBLEM);
		try {
			persistentMarker.setAttribute(IMarker.MESSAGE, value1);
			fail("1.2");
		} catch (RuntimeException e) {
			// expected
		} catch (CoreException e) {
			fail("1.3", e);
		}
		try {
			persistentMarker.setAttribute(IMarker.MESSAGE, value2);
			fail("1.4");
		} catch (RuntimeException e) {
			// expected
		} catch (CoreException e) {
			fail("1.5", e);
		}
		persistentMarker.delete();
	}

}