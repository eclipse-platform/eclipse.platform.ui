/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.editor;

import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.ant.tests.ui.editor.performance.EditorTestHelper;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.PartInitException;

public class AntEditorTests extends AbstractAntUITest {

    public AntEditorTests(String name) {
        super(name);
    }
    
    public void testHover() throws PartInitException {
        try {
		    IFile file= getIFile("refid.xml");
		    AntEditor editor= (AntEditor)EditorTestHelper.openInEditor(file, "org.eclipse.ant.ui.internal.editor.AntEditor", true);
		    XMLTextHover hover= new XMLTextHover(editor);
		    IRegion region= hover.getHoverRegion(editor.getViewer(), 269);
		    String hoverText= hover.getHoverInfo(editor.getViewer(), region);
		    String correctResult= "<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1><h5>Path Elements:</h5><ul><li>";
		    assertTrue("Expected the following hover text to start with: " + correctResult, hoverText.startsWith(correctResult));
		    
        } finally {
            EditorTestHelper.closeAllEditors();    
        }
    }
}
