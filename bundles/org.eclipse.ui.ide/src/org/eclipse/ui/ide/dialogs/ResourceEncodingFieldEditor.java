/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.misc.Assert;

/**
 * The ResourceEncodingFieldEditor is a field editor for
 * editing the encoding of a resource and does not use a 
 * preference store.
 */
public class ResourceEncodingFieldEditor extends AbstractEncodingFieldEditor {

	/**
	 * The resource being edited.
	 */
	private IResource resource;

	/**
	 * Create a new instance of the receiver for setting the
	 * encoding on charsetResource.
	 * @param labelText
	 * @param parent
	 * @param charsetResource Must be an IContainer or an IFile.
	 * @see org.eclipse.core.resources.IContainer#getDefaultCharset()
	 * @see org.eclipse.core.resources.IFile#getCharset()
	 */
	public ResourceEncodingFieldEditor(String labelText, Composite parent, IResource charsetResource) {
		super();
		setLabelText(labelText);
		Assert.isTrue(charsetResource instanceof IContainer || charsetResource instanceof IFile);
		this.resource = charsetResource;
		createControl(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.AbstractEncodingFieldEditor#getStoredValue()
	 */
	protected String getStoredValue() {
		try {
			if (resource instanceof IContainer)
				return ((IContainer) resource).getDefaultCharset();
			return ((IFile) resource).getCharset();

		} catch (CoreException e) {//If there is an error return the default
			WorkbenchPlugin.log(IDEWorkbenchMessages
					.getString("ResourceEncodingFieldEditor.ErrorLoadingMessage"), e.getStatus()); //$NON-NLS-1$
			return WorkbenchEncoding.getWorkbenchDefaultEncoding();
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {

		String encoding = getSelectedEncoding();

		if (hasSameEncoding(encoding))
			return;

		String descriptionCharset = getCharsetFromDescription();
		if (descriptionCharset != null && !(descriptionCharset.equals(encoding))) {

			MessageDialog dialog = new MessageDialog(
					getPreferencePage().getShell(),
					IDEWorkbenchMessages
							.getString("ResourceEncodingFieldEditor.EncodingConflictTitle"), //$NON-NLS-1$
					null,
					IDEWorkbenchMessages
							.format(
									"ResourceEncodingFieldEditor.EncodingConflictMessage", new String[] { encoding, descriptionCharset }), //$NON-NLS-1$
					MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL }, 0); // yes is the default
			if (dialog.open() > 0)
				return;
		}

		if (encoding.equals(getDefaultEnc()))
			encoding = null;
		else
			IDEEncoding.addIDEEncoding(encoding);

		final String finalEncoding = encoding;

		Job charsetJob = new Job(IDEWorkbenchMessages.getString("IDEEncoding.EncodingJob")) {//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (resource instanceof IContainer)
						((IContainer) resource).setDefaultCharset(finalEncoding, monitor);
					else
						((IFile) resource).setCharset(finalEncoding, monitor);
					return Status.OK_STATUS;
				} catch (CoreException e) {//If there is an error return the default
					WorkbenchPlugin
							.log(
									IDEWorkbenchMessages
											.getString("ResourceEncodingFieldEditor.ErrorStoringMessage"), e.getStatus()); //$NON-NLS-1$
					return e.getStatus();
				}
			}
		};

		charsetJob.schedule();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#store()
	 */
	public void store() {//Override the store method as we are not using a preference store
		doStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#load()
	 */
	public void load() {//Override the load method as we are not using a preference store
		setPresentsDefaultValue(false);
		doLoad();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#loadDefault()
	 */
	public void loadDefault() {
		setPresentsDefaultValue(true);
		doLoadDefault();
		refreshValidState();
	}

	/* (non-Javadoc)
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
			//Query up the whole hierarchy
			defaultCharset = resource.getParent().getDefaultCharset(true);
		} catch (CoreException exception) {
			//If there is an exception try again
		}

		if (defaultCharset != null && defaultCharset.length() > 0)
			return defaultCharset;

		return super.findDefaultEncoding();
	}

	/**
	 * Get the charset from the content description if there is one.
	 * @return String or <code>null</code>.
	 */
	private String getCharsetFromDescription() {
		IContentDescription description = getContentDescription();
		if (description != null)
			return description.getCharset();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#defaultButtonText()
	 */
	protected String defaultButtonText() {

		if (resource instanceof IWorkspaceRoot)
			return super.defaultButtonText();

		if (resource instanceof IFile) {

			try {
				//If we can find a charset then derive from that
				if (((IFile) resource).getContentDescription() == null)
					return IDEWorkbenchMessages.format("ResourceInfo.fileContainerEncodingFormat", //$NON-NLS-1$
							new String[] { getDefaultEnc() });

				return IDEWorkbenchMessages.format("ResourceInfo.fileContentEncodingFormat", //$NON-NLS-1$
						new String[] { getDefaultEnc() });

			} catch (CoreException exception) {
				//Do nothing here as we will just try to derive from the container
			}
		}

		return IDEWorkbenchMessages.format("ResourceInfo.containerEncodingFormat", //$NON-NLS-1$
				new String[] { getDefaultEnc() });

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#createEncodingGroup(org.eclipse.swt.widgets.Composite, int)
	 */
	protected Group createEncodingGroup(Composite parent, int numColumns) {
		Group group = super.createEncodingGroup(parent, numColumns);
		String byteOrderLabel = IDEEncoding.getByteOrderMarkLabel(getContentDescription());
		if (byteOrderLabel != null) {
			Label label = new Label(group, SWT.NONE);
			label.setText(IDEWorkbenchMessages.format(
					"WorkbenchPreference.encoding.encodingMessage", //$NON-NLS-1$
					new String[] { byteOrderLabel }));
			GridData layoutData = new GridData();
			layoutData.horizontalSpan = numColumns + 1;
			label.setLayoutData(layoutData);

		}
		return group;
	}

	/**
	 * Get the content description of the resource if it is 
	 * a file and it has a content description.
	 * @return IContentDescription or <code>null</code> if resource is
	 * not an IFile or it does not have a descrption.
	 */
	private IContentDescription getContentDescription() {
		try {
			if (resource instanceof IFile)
				return (((IFile) resource).getContentDescription());
		} catch (CoreException exception) {
			//If we cannot find it return null
		}
		return null;
	}

}
