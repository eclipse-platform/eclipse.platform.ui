/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests which verify the operation of the virtual viewer in the lazy mode.
 * Note: the virtual viewer doesn't support lazy mode yet, so this class
 * is really just a big place holder.
 * 
 *  @since 3.6
 */
public class VirtualViewerLazyModeTests extends TestCase {
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public VirtualViewerLazyModeTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay/*, SWT.ON_TOP | SWT.SHELL_TRIM*/);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(false, false);
        fViewer.addViewerUpdateListener(fListener);
        fViewer.addLabelUpdateListener(fListener);
        fViewer.addModelChangedListener(fListener);

        fShell.open ();
    }

    protected ITreeModelViewer createViewer(Display display, Shell shell) {
        return new VirtualTreeModelViewer(fDisplay, SWT.VIRTUAL, new PresentationContext("TestViewer"));
    }
    
    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        fViewer.removeLabelUpdateListener(fListener);
        fViewer.removeViewerUpdateListener(fListener);
        fViewer.removeModelChangedListener(fListener);
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
    }

    public void test() {
        // TODO
    }
}
