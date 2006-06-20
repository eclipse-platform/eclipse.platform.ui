/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.util.ArrayList;
import java.util.Collection;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests regression of bug 25457.  In this case, attempting to move a project
 * that is only a case change, where the move fails due to another handle being
 * open on a file in the hierarchy, would cause deletion of the source.
 */
public class Bug_029851 extends ResourceTest {

	public static Test suite() {
		return new TestSuite(Bug_029851.class);
	}

	public Bug_029851() {
		super();
	}

	public Bug_029851(String name) {
		super(name);
	}

	private Collection createChildren(int breadth, int depth, IPath prefix) {
		ArrayList result = new ArrayList();
		for (int i = 0; i < breadth; i++) {
			IPath child = prefix.append(Integer.toString(i)).addTrailingSeparator();
			result.add(child.toString());
			if (depth > 0)
				result.addAll(createChildren(breadth, depth - 1, child));
		}
		return result;
	}

	public String[] defineHierarchy() {
		int depth = 3;
		int breadth = 3;
		IPath prefix = new Path("/a/");
		Collection result = createChildren(breadth, depth, prefix);
		result.add(prefix.toString());
		return (String[]) result.toArray(new String[0]);
	}

	public void test() {
		// disable for now.
		if (true)
			return;
		createHierarchy();
		final QualifiedName key = new QualifiedName("local", getUniqueString());
		final String value = getUniqueString();
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				resource.setPersistentProperty(key, value);
				return true;
			}
		};
		try {
			getWorkspace().getRoot().accept(visitor);
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}
}
