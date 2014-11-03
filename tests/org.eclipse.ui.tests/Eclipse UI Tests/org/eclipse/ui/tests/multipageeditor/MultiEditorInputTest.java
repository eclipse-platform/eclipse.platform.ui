/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import junit.framework.TestCase;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;

/**
 * Tests the API of <code>MultiPageEditorInput</code>.
 *
 * @since 3.1
 */
public class MultiEditorInputTest extends TestCase {

    /**
     * Constructs a new instance of <code>MultiPageEditorInputTest</code>.
     *
     * @param name
     *            The name of the test to be run.
     */
    public MultiEditorInputTest(String name) {
        super(name);
    }

    /**
     * Tests the equals and hashCode methods.
     */
    public void testEqualsAndHash() {
        String ea = "dummy.editor.id.A";
        String eb = "dummy.editor.id.B";
        String ec = "dummy.editor.id.C";
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IEditorInput ia = new FileEditorInput(root.getFile(new Path("/DummyProject/FileA")));
        IEditorInput ib = new FileEditorInput(root.getFile(new Path("/DummyProject/FileB")));
        IEditorInput ic = new FileEditorInput(root.getFile(new Path("/DummyProject/FileC")));
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
