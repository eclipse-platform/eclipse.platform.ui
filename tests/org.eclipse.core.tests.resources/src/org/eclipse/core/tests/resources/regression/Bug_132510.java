/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.LinkDescription;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests concurrent modification of the project description link table.
 */
public class Bug_132510 extends ResourceTest {
	public static Test suite() {
		return new TestSuite(Bug_132510.class);
	}

	public Bug_132510(String name) {
		super(name);
	}

	public void testBug() {
		ProjectDescription desc = new ProjectDescription();
		IPath path1 = new Path("/a/b/");
		IPath path2 = new Path("/a/c/");
		LinkDescription link = new LinkDescription();
		desc.setLinkLocation(path1, link);
		HashMap linkMap = desc.getLinks();
		Iterator it = linkMap.values().iterator();
		desc.setLinkLocation(path2, link);
		try {
			it.next();
		} catch (ConcurrentModificationException e) {
			fail("4.99", e);
		}
	}

}
