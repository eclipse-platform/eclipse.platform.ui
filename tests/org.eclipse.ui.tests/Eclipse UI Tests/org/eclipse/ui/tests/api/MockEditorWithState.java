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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

public class MockEditorWithState extends MockWorkbenchPart implements
		IEditorPart, IPersistableEditor {

	public static final String ID = "org.eclipse.ui.tests.api.MockEditorStatePart";

	public static final String NAME = "Mock Editor State 1";

	private IEditorInput input;

	private boolean dirty = false;

	private boolean saveNeeded = true;

	private boolean saveAsAllowed = false;

	public MockEditorWithState() {
		super();
	}

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
		callTrace.add("isSaveAsAllowed");
		return saveAsAllowed;
	}

	/**
	 * @see IEditorPart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		callTrace.add("isSaveOnCloseNeeded");
		return saveNeeded;
	}

	public void setSaveAsAllowed(boolean isSaveAsAllowed) {
		this.saveAsAllowed = isSaveAsAllowed;
	}

	public void setSaveNeeded(boolean value) {
		saveNeeded = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.api.MockWorkbenchPart#getActionBars()
	 */
	protected IActionBars getActionBars() {
		return getEditorSite().getActionBars();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPersistableEditor#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		callTrace.add("restoreState");
		String val = memento.getString("saveNeeded");
		if (val != null) {
			saveNeeded = Boolean.valueOf(val).booleanValue();
		}
		val = memento.getString("saveAsAllowed");
		if (val != null) {
			saveAsAllowed = Boolean.valueOf(val).booleanValue();
		}
		val = memento.getString("dirty");
		if (val != null) {
			dirty = Boolean.valueOf(val).booleanValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		callTrace.add("saveState");
		memento.putString("saveNeeded", String.valueOf(saveNeeded));
		memento.putString("saveAsAllowed", String.valueOf(saveAsAllowed));
		memento.putString("dirty", String.valueOf(dirty));
	}
}
