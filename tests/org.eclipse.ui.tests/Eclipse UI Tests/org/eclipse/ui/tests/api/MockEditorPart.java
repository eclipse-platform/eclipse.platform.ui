/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;

public class MockEditorPart extends MockWorkbenchPart implements IEditorPart,
        IGotoMarker {

    private static final String BASE = "org.eclipse.ui.tests.api.MockEditorPart";

    public static final String ID1 = BASE + "1";

    public static final String ID2 = BASE + "2";

    public static final String NAME = "Mock Editor 1";

    private IEditorInput input;

    private boolean dirty = false;

    private boolean saveNeeded = false;

    public MockEditorPart() {
        super();
    }

    /**
     * @see IEditorPart#doSave(IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        setDirty(false);
        callTrace.add("doSave");
    }

    /**
     * @see IEditorPart#doSaveAs()
     */
    public void doSaveAs() {
    }

    /**
     * @see IEditorPart#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        return input;
    }

    /**
     * @see IEditorPart#getEditorSite()
     */
    public IEditorSite getEditorSite() {
        return (IEditorSite) getSite();
    }

    /**
     * @see org.eclipse.ui.ide.IGotoMarker
     */
    public void gotoMarker(IMarker marker) {
        callTrace.add("gotoMarker");
    }

    /**
     * @see IEditorPart#init(IEditorSite, IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        this.input = input;
        setSite(site);
        callTrace.add("init");
        setSiteInitialized();
    }

    /**
     * @see IEditorPart#isDirty()
     */
    public boolean isDirty() {
        callTrace.add("isDirty");
        return dirty;
    }

    public void setDirty(boolean value) {
        dirty = value;
        firePropertyChange(PROP_DIRTY);
    }

    /**
     * @see IEditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * @see IEditorPart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
        callTrace.add("isSaveOnCloseNeeded");
        return saveNeeded;
    }

    public void setSaveNeeded(boolean value) {
        saveNeeded = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.MockWorkbenchPart#getActionBars()
     */
    protected IActionBars getActionBars() {
        return getEditorSite().getActionBars();
    }
}

