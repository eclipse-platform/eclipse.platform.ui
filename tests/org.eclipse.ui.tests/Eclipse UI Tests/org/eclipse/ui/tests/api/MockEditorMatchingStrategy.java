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

package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * An editor matching strategy to support the IEditorMatchingStrategyTest tests.
 * This models the scenario for PDE manifest editors described in bug 53700.
 *
 * @since 3.1
 */
public class MockEditorMatchingStrategy implements IEditorMatchingStrategy {

    @Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
        IFile inputFile = ResourceUtil.getFile(input);
        if (inputFile != null && inputFile.getParent() instanceof IProject) {
            String name = inputFile.getName();
            if (name.equals("plugin.xml") || name.equals("MANIFEST.MF") || name.equals("build.properties")) {
                try {
                    IFile editorFile = ResourceUtil.getFile(editorRef.getEditorInput());
                    return editorFile != null && inputFile.getProject().equals(editorFile.getProject());
                } catch (PartInitException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

}
