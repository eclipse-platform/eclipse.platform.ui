/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Test for bug 329836
 */
public class Bug_329836 extends ResourceTest {
	public Bug_329836() {
		super();
	}

	public Bug_329836(String name) {
		super(name);
	}

	public void testBug() {
		if (!Platform.getOS().equals(Platform.OS_MACOSX))
			return;

		IFileStore fileStore = getTempStore().getChild(getUniqueString());
		createFileInFileSystem(fileStore);

		// set EFS.ATTRIBUTE_READ_ONLY which also sets EFS.IMMUTABLE on Mac
		IFileInfo info = fileStore.fetchInfo();
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		try {
			fileStore.putInfo(info, EFS.SET_ATTRIBUTES, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		// read the info again
		info = fileStore.fetchInfo();

		// check that attributes are really set
		assertTrue("2.0", info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertTrue("3.0", info.getAttribute(EFS.ATTRIBUTE_IMMUTABLE));

		// unset EFS.ATTRIBUTE_READ_ONLY which also unsets EFS.IMMUTABLE on Mac

		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
		try {
			fileStore.putInfo(info, EFS.SET_ATTRIBUTES, getMonitor());
		} catch (CoreException e) {
			fail("4.0", e);
		}

		// read the info again
		info = fileStore.fetchInfo();

		// check that attributes are really unset
		assertFalse("5.0", info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertFalse("6.0", info.getAttribute(EFS.ATTRIBUTE_IMMUTABLE));

	}

	public static Test suite() {
		return new TestSuite(Bug_329836.class);
	}
}
