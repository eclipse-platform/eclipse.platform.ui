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

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor.createPartControl(Composite)
	 */
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
		boolean restoreFocus= false;
		
		if (fStatusControl != null) {
			if (!fStatusControl.isDisposed()) {
				restoreFocus= containsFocus(fStatusControl);
			}
			fStatusControl.dispose();
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
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setFocus()
	 * @since 3.6
	 */
	public void setFocus() {
		if (fStatusControl != null && !fStatusControl.isDisposed()) {
			/* even if the control does not really take focus, we still have to set it
			 * to fulfill the contract and to make e.g. Ctrl+PageUp/Down work. */
			fStatusControl.setFocus();
		} else {
			super.setFocus();
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#validateEditorInputState()
	 * @since 3.3
	 */
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

	/*
	 * @see AbstractTextEditor#updateStatusField(String)
	 */
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

	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fParent != null && !fParent.isDisposed())
			updatePartControl(getEditorInput());
	}

	/*
	 * @see ITextEditor#doRevertToSaved()
	 */
	public void doRevertToSaved() {
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=19014
		super.doRevertToSaved();
		if (fParent != null && !fParent.isDisposed())
			updatePartControl(getEditorInput());
	}

	/*
	 * @see AbstractTextEditor#sanityCheckState(IEditorInput)
	 */
	protected void sanityCheckState(IEditorInput input) {
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=19014
		super.sanityCheckState(input);
		if (fParent != null && !fParent.isDisposed())
			updatePartControl(getEditorInput());
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handleEditorInputChanged()
	 * @since 3.1
	 */
	protected void handleEditorInputChanged() {
		super.handleEditorInputChanged();
		if (fParent != null && !fParent.isDisposed())
			updatePartControl(getEditorInput());
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handleElementContentReplaced()
	 * @since 3.1
	 */
	protected void handleElementContentReplaced() {
		super.handleElementContentReplaced();
		if (fParent != null && !fParent.isDisposed())
			updatePartControl(getEditorInput());
	}
}
