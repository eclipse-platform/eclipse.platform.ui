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
package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.ArrayUtil;

public class IFileEditorMappingTest extends TestCase {
    private IFileEditorMapping[] fMappings;

    public IFileEditorMappingTest(String testName) {
        super(testName);
    }

    public void setUp() {
        fMappings = PlatformUI.getWorkbench().getEditorRegistry()
                .getFileEditorMappings();
    }

    public void testGetName() throws Throwable {
        for (int i = 0; i < fMappings.length; i++)
            assertNotNull(fMappings[i].getName());
    }

    public void testGetLabel() throws Throwable {
        String label;
        for (int i = 0; i < fMappings.length; i++) {
            label = fMappings[i].getLabel();
            assertNotNull(label);
            assertTrue(label.equals(fMappings[i].getName() + "."
                    + fMappings[i].getExtension()));
        }
    }

    public void testGetExtension() throws Throwable {
        for (int i = 0; i < fMappings.length; i++)
            assertNotNull(fMappings[i].getExtension());
    }

    public void testGetEditors() throws Throwable {
        IEditorDescriptor[] editors;

        for (int i = 0; i < fMappings.length; i++) {
            editors = fMappings[i].getEditors();
            assertTrue(ArrayUtil.checkNotNull(editors) == true);
        }
    }

    public void testGetImageDescriptor() throws Throwable {
        for (int i = 0; i < fMappings.length; i++)
            assertNotNull(fMappings[i].getImageDescriptor());
    }

    //how do i set the default editor?
    public void testGetDefaultEditor() throws Throwable {
        /*		for( int i = 0; i < fMappings.length; i ++ )
         assertNotNull( fMappings[ i ].getDefaultEditor() );*/
    }
}
