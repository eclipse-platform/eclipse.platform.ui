/*******************************************************************************
 * Copyright (c) 2011, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - bug fixing
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.8
 */
public class JFaceViewerFilterTests extends FilterTests {
    
    public JFaceViewerFilterTests(String name) {
        super(name);
    }

    @Override
	protected IInternalTreeModelViewer createViewer(Display display, Shell shell) {
		return new TreeModelViewer(fShell, SWT.VIRTUAL | SWT.MULTI, new PresentationContext("TestViewer")); //$NON-NLS-1$
    }
}
