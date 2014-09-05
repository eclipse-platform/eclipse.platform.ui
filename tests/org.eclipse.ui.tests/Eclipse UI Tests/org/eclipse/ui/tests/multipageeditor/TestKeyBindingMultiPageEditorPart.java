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
package org.eclipse.ui.tests.multipageeditor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * A multi-page editor for testing key bindings while switching pages. This
 * is one of two pages -- each with a different context. The first context binds
 * "Ctrl+Shift+4" -- the second binds "Ctrl+Shift+5" -- to the command
 * "org.eclipse.ui.tests.TestCommand". Which context to use is determined by a
 * page number, which is passed as a parameter.
 * 
 * @since 3.0
 */
public final class TestKeyBindingMultiPageEditorPart extends EditorPart {

    /**
     * The page number for this part.
     *  
     */
    private final int number;

    /**
     * Constructs a new instance of <code>TestKeyBindingMultiPageEditorPart</code>
     * with the page number of this page.
     * 
     * @param number
     *            The page number for this part.
     */
    public TestKeyBindingMultiPageEditorPart(int number) {
        super();
        this.number = number;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());
        Text text1 = new Text(composite, SWT.NONE);
        text1.setText("Blue"); //$NON-NLS-1$
        Text text2 = new Text(composite, SWT.NONE);
        text2.setText("Red"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
	public void doSave(IProgressMonitor monitor) {
        // Do nothing.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
	public void doSaveAs() {
        throw new UnsupportedOperationException("Not implemented in this test."); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#gotoMarker(org.eclipse.core.resources.IMarker)
     */
    public void gotoMarker(IMarker marker) {
        // Do nothing.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
	public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        setInput(input);
        setSite(site);
        setPartName("Editor"); //$NON-NLS-1$
        setTitleImage(input.getImageDescriptor().createImage());
        setTitleToolTip("Moooooo"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
	public boolean isDirty() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
	public boolean isSaveAsAllowed() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
	public void setFocus() {
        final boolean odds = (number % 2) > 0;
        final String scope1 = "org.eclipse.ui.tests.scope1"; //$NON-NLS-1$
        final String scope2 = "org.eclipse.ui.tests.scope2"; //$NON-NLS-1$
        IKeyBindingService keyBindingService = getEditorSite()
                .getKeyBindingService();
        keyBindingService.setScopes(new String[] { (odds) ? scope1 : scope2 });
    }
}
