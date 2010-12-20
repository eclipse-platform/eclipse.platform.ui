/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

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

	private Composite group;

	private Button separateDerivedEncodingsButton = null;

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
		setLabelAndResource(labelText, charsetResource);
		createControl(parent);
	}
	
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
	 *  @param groupTitle
	 *  		  the title for the field editor's control. If groupTitle is
	 *            <code>null</code> the control will be unlabelled
	 *            (by default a {@link Composite} instead of a {@link Group}.
	 * 
	 * @see org.eclipse.core.resources.IContainer#getDefaultCharset()
	 * @see org.eclipse.core.resources.IFile#getCharset()
	 * @see AbstractEncodingFieldEditor#setGroupTitle(String)
	 * @since 3.3
	 */
	public ResourceEncodingFieldEditor(String labelText, Composite parent,
			IResource charsetResource,String groupTitle) {
		super();
		setLabelAndResource(labelText, charsetResource);
		setGroupTitle(groupTitle);
		createControl(parent);
	}

	/**
     * Set the label text and the resource we are editing.
	 * @param labelText
	 * @param charsetResource
     * @since 3.3
	 */
	private void setLabelAndResource(String labelText, IResource charsetResource) {
		Assert.isTrue(charsetResource instanceof IContainer
				|| charsetResource instanceof IFile);
		setLabelText(labelText);
		this.resource = charsetResource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.dialogs.AbstractEncodingFieldEditor#getStoredValue()
	 */
	protected String getStoredValue() {
		try {
			if (resource instanceof IContainer) {
				return ((IContainer) resource).getDefaultCharset(false);
			}
			return ((IFile) resource).getCharset(false);

		} catch (CoreException e) {// If there is an error return the default
			IDEWorkbenchPlugin
					.log(
							IDEWorkbenchMessages.ResourceEncodingFieldEditor_ErrorLoadingMessage,
							e.getStatus());
			return WorkbenchEncoding.getWorkbenchDefaultEncoding();
		}

	}

	private boolean getStoredSeparateDerivedEncodingsValue() {
		// be careful looking up for our node so not to create any nodes as side effect
		Preferences node = Platform.getPreferencesService().getRootNode()
				.node(ProjectScope.SCOPE);
		String projectName = ((IProject) resource).getName();
		try {
			//TODO once bug 90500 is fixed, should be as simple as this:
			//			String path = projectName + IPath.SEPARATOR + ResourcesPlugin.PI_RESOURCES;
			//			return node.nodeExists(path) ? node.node(path).getBoolean(ResourcesPlugin.PREF_SEPARATE_DERIVED_ENCODINGS, false) : false;
			// for now, take the long way
			if (!node.nodeExists(projectName))
				return false;
			node = node.node(projectName);
			if (!node.nodeExists(ResourcesPlugin.PI_RESOURCES))
				return false;
			node = node.node(ResourcesPlugin.PI_RESOURCES);
			return node.getBoolean(
					ResourcesPlugin.PREF_SEPARATE_DERIVED_ENCODINGS, false);
		} catch (BackingStoreException e) {
			// default value
			return false;
		}
	}

	private boolean hasSameSeparateDerivedEncodings() {
		return (separateDerivedEncodingsButton == null)
				|| ((separateDerivedEncodingsButton != null) && (separateDerivedEncodingsButton
						.getSelection() == getStoredSeparateDerivedEncodingsValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {

		String encoding = getSelectedEncoding();

		// Clear the value if nothing is selected
		if (isDefaultSelected()) {
			encoding = null;
		}
		// Don't update if the same thing is selected
		final boolean hasSameEncoding = hasSameEncoding(encoding);
		final boolean hasSameSeparateDerivedEncodings = hasSameSeparateDerivedEncodings();
		if (hasSameEncoding && hasSameSeparateDerivedEncodings) {
			return;
		}

		String descriptionCharset = getCharsetFromDescription();
		if (descriptionCharset != null
				&& !(descriptionCharset.equals(encoding)) && encoding != null) {
			Shell shell = null;
			DialogPage page = getPage();
			if (page != null) {
				shell = page.getShell();
			}

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
							IDialogConstants.NO_LABEL }, 0) {
				protected int getShellStyle() {
					return super.getShellStyle() | SWT.SHEET;
				}
			}; // yes is the
			// default
			if (dialog.open() > 0) {
				return;
			}
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
					if (!hasSameEncoding) {
						if (resource instanceof IContainer) {
							((IContainer) resource).setDefaultCharset(
									finalEncoding, monitor);
						} else {
							((IFile) resource).setCharset(finalEncoding,
									monitor);
						}
					}
					if (!hasSameSeparateDerivedEncodings) {
						Preferences prefs = new ProjectScope((IProject) resource).getNode(ResourcesPlugin.PI_RESOURCES);
						if (getStoredSeparateDerivedEncodingsValue())
							prefs.remove(ResourcesPlugin.PREF_SEPARATE_DERIVED_ENCODINGS);
						else
							prefs.putBoolean(ResourcesPlugin.PREF_SEPARATE_DERIVED_ENCODINGS, true);
						prefs.flush();
					}
					return Status.OK_STATUS;
				} catch (CoreException e) {// If there is an error return the
					// default
					IDEWorkbenchPlugin
							.log(
									IDEWorkbenchMessages.ResourceEncodingFieldEditor_ErrorStoringMessage,
									e.getStatus());
					return e.getStatus();
				} catch (BackingStoreException e) {
					IDEWorkbenchPlugin.log(IDEWorkbenchMessages.ResourceEncodingFieldEditor_ErrorStoringMessage, e);
					return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, e.getMessage(), e);
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
	public void store() {
		// Override the store method as we are not using a preference store
		doStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#load()
	 */
	public void load() {
		// Override the load method as we are not using a preference store
		setPresentsDefaultValue(false);
		doLoad();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#loadDefault()
	 */
	public void loadDefault() {
		// Override the loadDefault method as we are not using a preference store
		setPresentsDefaultValue(true);
		doLoadDefault();
		refreshValidState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		super.doLoadDefault();
		if (separateDerivedEncodingsButton != null)
			separateDerivedEncodingsButton
					.setSelection(getStoredSeparateDerivedEncodingsValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#findDefaultEncoding()
	 */
	protected String findDefaultEncoding() {

		if (resource instanceof IWorkspaceRoot) {
			return super.findDefaultEncoding();
		}

		String defaultCharset = getCharsetFromDescription();

		if (defaultCharset != null && defaultCharset.length() > 0) {
			return defaultCharset;
		}
		try {
			// Query up the whole hierarchy
			defaultCharset = resource.getParent().getDefaultCharset(true);
		} catch (CoreException exception) {
			// If there is an exception try again
		}

		if (defaultCharset != null && defaultCharset.length() > 0) {
			return defaultCharset;
		}

		return super.findDefaultEncoding();
	}

	/**
	 * Returns the charset from the content description if there is one.
	 * 
	 * @return the charset from the content description, or <code>null</code>
	 */
	private String getCharsetFromDescription() {
		IContentDescription description = getContentDescription();
		if (description != null) {
			return description.getCharset();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor#defaultButtonText()
	 */
	protected String defaultButtonText() {

		if (resource instanceof IWorkspaceRoot) {
			return super.defaultButtonText();
		}

		if (resource instanceof IFile) {
			try {
				IContentDescription description = ((IFile) resource)
						.getContentDescription();
				// If we can find a charset from the description then derive
				// from that
				if (description == null || description.getCharset() == null) {
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceInfo_fileContainerEncodingFormat,
									getDefaultEnc());
				}

				IContentType contentType = description.getContentType();
				if (contentType != null && contentType.getDefaultCharset() == description.getCharset()) {
					return NLS
							.bind(
									IDEWorkbenchMessages.ResourceInfo_fileContentTypeEncodingFormat,
									getDefaultEnc());
				}

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
		group = super.createEncodingGroup(parent, numColumns);
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

		if (resource.getType() == IResource.PROJECT) {
			separateDerivedEncodingsButton = new Button(group, SWT.CHECK);
			GridData data = new GridData();
			data.horizontalSpan = 2;
			separateDerivedEncodingsButton.setLayoutData(data);
			separateDerivedEncodingsButton
					.setText(IDEWorkbenchMessages.ResourceEncodingFieldEditor_SeparateDerivedEncodingsLabel);
			separateDerivedEncodingsButton
					.setSelection(getStoredSeparateDerivedEncodingsValue());
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
			if (resource instanceof IFile) {
				return (((IFile) resource).getContentDescription());
			}
		} catch (CoreException exception) {
			// If we cannot find it return null
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean, org.eclipse.swt.widgets.Composite)
	 */
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		group.setEnabled(enabled);
		Control[] children = group.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setEnabled(enabled);

		}
	}

}
