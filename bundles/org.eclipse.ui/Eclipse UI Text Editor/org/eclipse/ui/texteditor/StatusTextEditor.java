package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IEditorInput;


/**
 * Capable of handling input elements that have an associated status with them.
 */
public class StatusTextEditor extends AbstractTextEditor {
	
	private Composite fParent;
	private StackLayout fStackLayout;
	private Composite fDefaultComposite;
	private Control fStatusControl;

	/*
	 * @see IWorkbenchPart#createPartControl(Composite)
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
		
		if (fStatusControl != null) {
			fStatusControl.dispose();
			fStatusControl= null;
		}
		
		Control front= null;
		if (fParent != null && input != null) {
			if (getDocumentProvider() instanceof IDocumentProviderExtension) {
				IDocumentProviderExtension extension= (IDocumentProviderExtension) getDocumentProvider();
				IStatus status= extension.getStatus(input);
				if (status.isOK()) {
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
	}
	
	/**
	 * Creates the status control for the given status. May be overridden by subclasses.
	 * 
	 * @param parent the parent control
	 * @param status the status
	 */
	protected Control createStatusControl(Composite parent, IStatus status) {
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
			if (status != null && !status.isOK()) {
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
}