/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.multipageeditor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;
import org.junit.Test;

/**
 * Tests the API of <code>MultiPageEditorInput</code>.
 *
 * @since 3.1
 */
public class MultiEditorInputTest {

	/**
	 * Tests the equals and hashCode methods.
	 */
	@Test
	public void testEqualsAndHash() {
		String ea = "dummy.editor.id.A";
		String eb = "dummy.editor.id.B";
		String ec = "dummy.editor.id.C";
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IEditorInput ia = new FileEditorInput(root.getFile(IPath.fromOSString("/DummyProject/FileA")));
		IEditorInput ib = new FileEditorInput(root.getFile(IPath.fromOSString("/DummyProject/FileB")));
		IEditorInput ic = new FileEditorInput(root.getFile(IPath.fromOSString("/DummyProject/FileC")));
		MultiEditorInput a = new MultiEditorInput(new String[] { ea }, new IEditorInput[] { ia });
		MultiEditorInput a2 = new MultiEditorInput(new String[] { ea }, new IEditorInput[] { ia });
		MultiEditorInput b = new MultiEditorInput(new String[] { eb }, new IEditorInput[] { ib });
		MultiEditorInput abc = new MultiEditorInput(new String[] { ea, eb, ec }, new IEditorInput[] { ia, ib, ic });
		MultiEditorInput abc2 = new MultiEditorInput(new String[] { ea, eb, ec }, new IEditorInput[] { ia, ib, ic });

		assertTrue(a.equals(a));
		assertTrue(abc.equals(abc));

		assertTrue(a.equals(a2));
		assertTrue(a.hashCode() == a2.hashCode());

		assertFalse(a.equals(b));

		assertTrue(abc.equals(abc2));
		assertTrue(abc.hashCode() == abc2.hashCode());

		// check that differing array lengths are handled properly
		assertFalse(a.equals(abc));
		assertFalse(abc.equals(a));
	}
}
