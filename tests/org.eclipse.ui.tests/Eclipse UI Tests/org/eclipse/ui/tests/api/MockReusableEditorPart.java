/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.ide.IGotoMarker;

public class MockReusableEditorPart extends MockWorkbenchPart
		implements IGotoMarker, IShowEditorInput, IReusableEditor {

	private static final String BASE = "org.eclipse.ui.tests.api.MockReusableEditorPart";

	public static final String ID1 = BASE + "1";

	public static final String ID2 = BASE + "2";

	public static final String NAME = "Mock Reusable Editor 1";

	private IEditorInput input;

	private boolean dirty = false;

	private boolean saveNeeded = true;

	private boolean saveAsAllowed = false;

	public MockReusableEditorPart() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		final Button dirtyToggle = new Button(parent, SWT.CHECK);
		dirtyToggle.setText("Dirty");
		dirtyToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(dirtyToggle.getSelection());
			}
		});
		dirtyToggle.setSelection(isDirty());

		final Button saveNeededToggle = new Button(parent, SWT.CHECK);
		saveNeededToggle.setText("Save on close");
		saveNeededToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setSaveNeeded(saveNeededToggle.getSelection());
			}
		});
		saveNeededToggle.setSelection(saveNeeded);

		final Button saveAsToggle = new Button(parent, SWT.CHECK);
		saveAsToggle.setText("Save as allowed");
		saveAsToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setSaveAsAllowed(saveAsToggle.getSelection());
			}
		});
		saveAsToggle.setSelection(saveAsAllowed);
	}
	@Override
	public void doSave(IProgressMonitor monitor) {
		setDirty(false);
		callTrace.add("doSave");
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public IEditorInput getEditorInput() {
		return input;
	}

	@Override
	public IEditorSite getEditorSite() {
		return (IEditorSite) getSite();
	}

	@Override
	public void gotoMarker(IMarker marker) {
		callTrace.add("gotoMarker");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		this.input = input;
		setSite(site);
		callTrace.add("init");
		setSiteInitialized();
	}

	@Override
	public boolean isDirty() {
		callTrace.add("isDirty");
		return dirty;
	}

	public void setDirty(boolean value) {
		dirty = value;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isSaveAsAllowed() {
		callTrace.add("isSaveAsAllowed");
		return saveAsAllowed;
	}

	@Override
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

	@Override
	protected IActionBars getActionBars() {
		return getEditorSite().getActionBars();
	}

	@Override
	public void showEditorInput(IEditorInput editorInput) {
		callTrace.add("showEditorInput");
	}

	@Override
	public void setInput(IEditorInput input) {
		this.input = input;
		firePropertyChange(PROP_INPUT);
	}
}

