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
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart;

/**
 * Mock view part that implements ISaveablePart.
 * Used for testing hideView and other view lifecycle on saveable views.
 * 
 * @since 3.0.1
 */
public class SaveableMockViewPart extends MockViewPart implements
		ISaveablePart {
	
	public static String ID = "org.eclipse.ui.tests.api.SaveableMockViewPart";

	private boolean isDirty = false;

    private boolean saveAsAllowed = false;

    private boolean saveNeeded = true;

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        final Button dirtyToggle = new Button(parent, SWT.CHECK);
        dirtyToggle.setText("Dirty");
        dirtyToggle.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setDirty(dirtyToggle.getSelection());
            }
        });
        dirtyToggle.setSelection(isDirty());

        final Button saveNeededToggle = new Button(parent, SWT.CHECK);
        saveNeededToggle.setText("Save on close");
        saveNeededToggle.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSaveNeeded(saveNeededToggle.getSelection());
            }
        });
        saveNeededToggle.setSelection(saveNeeded);
        
        final Button saveAsToggle = new Button(parent, SWT.CHECK);
        saveAsToggle.setText("Save as allowed");
        saveAsToggle.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setSaveAsAllowed(saveAsToggle.getSelection());
            }
        });
        saveAsToggle.setSelection(saveAsAllowed);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		callTrace.add("doSave" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		callTrace.add("doSaveAs" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		callTrace.add("isDirty" );
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		callTrace.add("isSaveAsAllowed" );
		return saveAsAllowed ;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		callTrace.add("isSaveOnCloseNeeded" );
		return saveNeeded;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
        firePropertyChange(PROP_DIRTY);
	}
    
    public void setSaveAsAllowed(boolean isSaveAsAllowed) {
        this.saveAsAllowed = isSaveAsAllowed;
    }
    
    public void setSaveNeeded(boolean isSaveOnCloseNeeded) {
        this.saveNeeded = isSaveOnCloseNeeded;
    }
}
