/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;


/**
 * Capable of handling input elements that have an associated status with them.
 * @since 2.0
 */
public class StatusTextEditor extends AbstractTextEditor {

	/** The root composite of this editor */
	private Composite fParent;
	/** The layout used to manage the regular and the status page */
	private StackLayout fStackLayout;
	/** The root composite for the regular page */
	private Composite fDefaultComposite;
	/** The status page */
	private Control fStatusControl;
	/** {@link #setFocus()} is still running */
	private boolean setFocusIsRunning;

	// No .options for plugin yet
	private static final boolean DEBUG = false;

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor.createPartControl(Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		fParent= new Composite(parent, SWT.NONE);
		fStackLayout= new StackLayout();
		fParent.setLayout(fStackLayout);

		fDefaultComposite= new Composite(fParent, SWT.NONE);
		fDefaultComposite.setLayout(new FillLayout());
		super.createPartControl(fDefaultComposite);

		updatePartControl(getEditorInput());
	}

	/**
	 * Checks if the status of the given input is OK. If not the
	 * status control is shown rather than the default control.
	 *
	 * @param input the input whose status is checked
	 */
	public void updatePartControl(IEditorInput input) {
		final String where = "updatePartControl"; //$NON-NLS-1$
		if (setFocusIsRunning) {
			trace(where, "ERROR: trying to call update while processing focus", fStatusControl); //$NON-NLS-1$
		} else {
			trace(where, "START", fStatusControl); //$NON-NLS-1$
		}

		boolean restoreFocus= false;

		if (fStatusControl != null) {
			if (!fStatusControl.isDisposed()) {
				restoreFocus= containsFocus(fStatusControl);
			}
			fStatusControl.dispose();
			trace(where, "status control disposed", fStatusControl); //$NON-NLS-1$
			fStatusControl= null;
		}

		Control front= null;
		if (fParent != null && input != null) {
			if (getDocumentProvider() instanceof IDocumentProviderExtension) {
				IDocumentProviderExtension extension= (IDocumentProviderExtension) getDocumentProvider();
				IStatus status= extension.getStatus(input);
				if (!isErrorStatus(status)) {
					front= fDefaultComposite;
				} else {
					fStatusControl= createStatusControl(fParent, status);
					trace(where, "status control created", fStatusControl); //$NON-NLS-1$
					front= fStatusControl;
				}
			}
		}

		if (fStackLayout.topControl != front) {
			fStackLayout.topControl= front;
			fParent.layout();
			updateStatusFields();
		}

		if (restoreFocus && fStatusControl != null && !containsFocus(fStatusControl)) {
			fParent.setFocus();
		}
		trace(where, "END", fStatusControl); //$NON-NLS-1$
	}

	private boolean containsFocus(Control control) {
		Control focusControl= control.getDisplay().getFocusControl();
		if (focusControl != null) {
			focusControl= focusControl.getParent();
			while (focusControl != fParent && focusControl != null && !(focusControl instanceof Shell)) {
				focusControl= focusControl.getParent();
			}
		}
		return focusControl == fParent;
	}

	@Override
	public void setFocus() {
		final String where = "setFocus"; //$NON-NLS-1$
		if (setFocusIsRunning) {
			trace(where, "ERROR: trying to call setFocus while processing focus", fStatusControl); //$NON-NLS-1$
		} else {
			trace(where, "START", fStatusControl); //$NON-NLS-1$
		}
		setFocusIsRunning = true;

		if (fStatusControl != null && !fStatusControl.isDisposed()) {
			/* even if the control does not really take focus, we still have to set it
			 * to fulfill the contract and to make e.g. Ctrl+PageUp/Down work. */
			fStatusControl.setFocus();
		} else {
			super.setFocus();
		}

		setFocusIsRunning = false;

		trace(where, "END", fStatusControl); //$NON-NLS-1$
	}

	@Override
	public boolean validateEditorInputState() {
		if (!super.validateEditorInputState())
			return false;

		if (getDocumentProvider() instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension)getDocumentProvider();
			IStatus status= extension.getStatus(getEditorInput());
			return !isErrorStatus(status) && status.getSeverity() != IStatus.CANCEL;
		}

		return true;
	}

	/**
	 * Returns whether the given status indicates an error. Subclasses may override.
	 *
	 * @param status the status to be checked
	 * @return <code>true</code> if the status indicates an error, <code>false</code> otherwise\
	 * @since 3.0
	 */
	protected boolean isErrorStatus(IStatus status) {
		return status != null && status.getSeverity() == IStatus.ERROR;
	}

	/**
	 * Creates the status control for the given status.
	 * May be overridden by subclasses.
	 *
	 * @param parent the parent control
	 * @param status the status
	 * @return the new status control
	 */
	protected Control createStatusControl(Composite parent, IStatus status) {
		return createInfoForm(parent, status);
	}

	/**
	 * Helper to get rid of deprecation warnings.
	 *
	 * @param parent the parent
	 * @param status the status
	 * @return the control
	 * @since 3.5
	 * @deprecated As of 3.5
	 */
	@Deprecated
	private Control createInfoForm(Composite parent, IStatus status) {
		InfoForm infoForm= new InfoForm(parent);
		infoForm.setHeaderText(getStatusHeader(status));
		infoForm.setBannerText(getStatusBanner(status));
		infoForm.setInfo(getStatusMessage(status));
		return infoForm.getControl();
	}

	/**
	 * Returns a header for the given status
	 *
	 * @param status the status whose message is returned
	 * @return a header for the given status
	 */
	protected String getStatusHeader(IStatus status) {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns a banner for the given status.
	 *
	 * @param status the status whose message is returned
	 * @return a banner for the given status
	 */
	protected String getStatusBanner(IStatus status) {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns a message for the given status.
	 *
	 * @param status the status whose message is returned
	 * @return a message for the given status
	 */
	protected String getStatusMessage(IStatus status) {
		return status.getMessage();
	}

	@Override
	protected void updateStatusField(String category) {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension= (IDocumentProviderExtension) provider;
			IStatus status= extension.getStatus(getEditorInput());
			if (isErrorStatus(status)) {
				IStatusField field= getStatusField(category);
				if (field != null) {
					field.setText(fErrorLabel);
					return;
				}
			}
		}

		super.updateStatusField(category);
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		updatePartControl();
	}

	@Override
	public void doRevertToSaved() {
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=19014
		super.doRevertToSaved();
		updatePartControl();
	}

	@Override
	protected void sanityCheckState(IEditorInput input) {
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=19014
		super.sanityCheckState(input);
		if (!setFocusIsRunning) {
			updatePartControl();
		} else {
			trace("sanityCheck", "delaying update", fStatusControl); //$NON-NLS-1$ //$NON-NLS-2$
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				trace("sanityCheck", "incoming update", fStatusControl); //$NON-NLS-1$ //$NON-NLS-2$
				updatePartControl();
			});
		}
	}

	@Override
	protected void handleEditorInputChanged() {
		super.handleEditorInputChanged();
		updatePartControl();
	}

	@Override
	protected void handleElementContentReplaced() {
		super.handleElementContentReplaced();
		updatePartControl();
	}

	private void updatePartControl() {
		if (fParent != null && !fParent.isDisposed()) {
			updatePartControl(getEditorInput());
		}
	}

	private static void trace(String where, String what, Control o) {
		if (!DEBUG) {
			return;
		}
		String id;
		if (o == null) {
			id = "null"; //$NON-NLS-1$
		} else {
			id = System.identityHashCode(o) + (o.isDisposed() ? "<disposed!>" : ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println(where + " |" + id + "| " + what); //$NON-NLS-1$//$NON-NLS-2$
	}
}
