/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The ResourceEncodingFieldEditor is a field editor for editing the encoding of
 * a resource and does not use a preference store.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.1
 */
public final class ResourceEncodingFieldEditor extends
		AbstractEncodingFieldEditor {

	/**
	 * The resource being edited.
	 */
	private IResource resource;

	/**
	 * Creates a new encoding field editor for setting the encoding on the given
	 * resource.
	 * 
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 * @param charsetResource
	 *            must be an <code>IContainer</code> or an <code>IFile</code>.
	 * 
	 * @see org.eclipse.core.resources.IContainer#getDefaultCharset()
	 * @see org.eclipse.core.resources.IFile#getCharset()
	 */
	public ResourceEncodingFieldEditor(String labelText, Composite parent,
			IResource charsetResource) {
		super();
		Assert.isTrue(charsetResource instanceof IContainer
				|| charsetResource instanceof IFile);
		setLabelText(labelText);
		this.resource = charsetResource;
		createControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.dialogs.AbstractEncodingFieldEditor#getStoredValue()
	 */
	protected String getStoredValue() {
		try {
			if (resource instanceof IContainer)
				return ((IContainer) resource).getDefaultCharset(false);
			return ((IFile) resource).getCharset(false);

		} catch (CoreException e) {// If there is an error return the default
			IDEWorkbenchPlugin
					.log(
							IDEWorkbenchMessages.ResourceEncodingFieldEditor_ErrorLoadingMessage,
							e.getStatus());
			return WorkbenchEncoding.getWorkbenchDefaultEncoding();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {

		String encoding = getSelectedEncoding();

		// Clear the value if nothing is selected
		if (isDefaultSelected())
			encoding = null;
		// Don't update if the same thing is selected
		if (hasSameEncoding(encoding))
			return;

		String descriptionCharset = getCharsetFromDescription();
		if (descriptionCharset != null
				&& !(descriptionCharset.equals(encoding)) && encoding != null) {
			Shell shell = null;
			DialogPage page = getPage();
			if (page != null)
				shell = page.getShell();

			MessageDialog dialog = new MessageDialog(
					shell,
					IDEWorkbenchMessages.ResourceEncodingFieldEditor_EncodingConflictTitle,
					null,
					NLS
							.bind(
									IDEWorkbenchMessages.ResourceEncodingFieldEditor_EncodingConflictMessage,
									encoding, descriptionCharset),
					MessageDialog.WARNING, new String[] {
							IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL }, 0); // yes is the
																// default
			if (dialog.open() > 0)
				return;
		}

		IDEEncoding.addIDEEncoding(encoding);

		final String finalEncoding = encoding;

		Job charsetJob = new Job(IDEWorkbenchMessages.IDEEncoding_EncodingJob) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (resource instanceof IContainer)
						((IContainer) resource).setDefaultCharset(
								finalEncoding, monitor);
					else
						((IFile) resource).setCharset(finalEncoding, monitor);
					return Status.OK_STATUS;
				} catch (CoreException e) {// If there is an error return the
											// default
					IDEWorkbenchPlugin
							.log(
									IDEWorkbenchMessages.ResourceEncodingFieldEditor_ErrorStoringMessage,
									e.getStatus());
					return e.getStatus();
				}
			}
		};

		charsetJob.schedule();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#store()
	 */
	public void store() {// Override the store method as we are not using a
							// preference store
		doStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#load()
	 */
	public void load() {// Override the load method as we are not using a
						// preference store
		setPresentsDefaultValue(false);
		doLoad();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#loadDefault()
	 */
	public void loadDefault() {
		setPresentsDefaultValue(true);
		doLoadDefault();
		refreshValidState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#findDefaultEncoding()
	 */
	protected String findDefaultEncoding() {

		if (resource instanceof IWorkspaceRoot)
			return super.findDefaultEncoding();

		String defaultCharset = getCharsetFromDescription();
		defaultCharset = getCharsetFromDescription();

		if (defaultCharset != null && defaultCharset.length() > 0)
			return defaultCharset;
		try {
			// Query up the whole hierarchy
			defaultCharset = resource.getParent().getDefaultCharset(true);
		} catch (CoreException exception) {
			// If there is an exception try again
		}

		if (defaultCharset != null && defaultCharset.length() > 0)
			return defaultCharset;

		return super.findDefaultEncoding();
	}

	/**
	 * Returns the charset from the content description if there is one.
	 * 
	 * @return the charset from the content description, or <code>null</code>
	 */
	private String getCharsetFromDescription() {
		IContentDescription description = getContentDescription();
		if (description != null)
			return description.getCharset();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#defaultButtonText()
	 */
	protected String defaultButtonText() {

		if (resource instanceof IWorkspaceRoot)
			return super.defaultButtonText();

		if (resource instanceof IFile) {
			try {
				IContentDescription description = ((IFile) resource)
						.getContentDescription();
				// If we can find a charset from the description then derive
				// from that
				if (description == null || description.getCharset() == null)
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceInfo_fileContainerEncodingFormat,
									getDefaultEnc());

				return NLS
						.bind(
								IDEWorkbenchMessages.ResourceInfo_fileContentEncodingFormat,
								getDefaultEnc());

			} catch (CoreException exception) {
				// Do nothing here as we will just try to derive from the
				// container
			}
		}

		return NLS.bind(
				IDEWorkbenchMessages.ResourceInfo_containerEncodingFormat,
				getDefaultEnc());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#createEncodingGroup(org.eclipse.swt.widgets.Composite,
	 *      int)
	 */
	protected Composite createEncodingGroup(Composite parent, int numColumns) {
		Composite group = super.createEncodingGroup(parent, numColumns);
		String byteOrderLabel = IDEEncoding
				.getByteOrderMarkLabel(getContentDescription());
		if (byteOrderLabel != null) {
			Label label = new Label(group, SWT.NONE);
			label
					.setText(NLS
							.bind(
									IDEWorkbenchMessages.WorkbenchPreference_encoding_encodingMessage,
									byteOrderLabel));
			GridData layoutData = new GridData();
			layoutData.horizontalSpan = numColumns + 1;
			label.setLayoutData(layoutData);

		}
		return group;
	}

	/**
	 * Returns the content description of the resource if it is a file and it
	 * has a content description.
	 * 
	 * @return the content description or <code>null</code> if resource is not
	 *         an <code>IFile</code> or it does not have a description
	 */
	private IContentDescription getContentDescription() {
		try {
			if (resource instanceof IFile)
				return (((IFile) resource).getContentDescription());
		} catch (CoreException exception) {
			// If we cannot find it return null
		}
		return null;
	}

}
