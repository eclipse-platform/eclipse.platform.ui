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
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.swt.widgets.Composite;

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
			WorkbenchPlugin.log(IDEWorkbenchMessages.getString("ResourceEncodingFieldEditor.ErrorLoadingMessage"), e.getStatus()); //$NON-NLS-1$
			return WorkbenchEncoding.getWorkbenchDefaultEncoding();
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {

		final String encoding = getSelectedEncoding();

		if (hasSameEncoding(encoding))
			return;

		IDEEncoding.addIDEEncoding(encoding);

		Job charsetJob = new Job(IDEWorkbenchMessages.getString("IDEEncoding.EncodingJob")) {//$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (resource instanceof IContainer)
						((IContainer) resource).setDefaultCharset(encoding, monitor);
					else
						((IFile) resource).setCharset(encoding, monitor);
					return Status.OK_STATUS;
				} catch (CoreException e) {//If there is an error return the default
					WorkbenchPlugin.log(IDEWorkbenchMessages.getString("ResourceEncodingFieldEditor.ErrorStoringMessage"), e.getStatus()); //$NON-NLS-1$
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

}
