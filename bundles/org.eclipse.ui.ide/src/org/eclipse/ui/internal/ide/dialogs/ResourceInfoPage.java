/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.LineDelimiterEditor;

/**
 * The ResourceInfoPage is the page that shows the basic
 * info about the resource.
 */
public class ResourceInfoPage extends PropertyPage {

	private Button editableBox;

	private Button executableBox;

	private Button archiveBox;

	private Button derivedBox;

	private boolean previousReadOnlyValue;

	private boolean previousExecutableValue;

	private boolean previousArchiveValue;

	private boolean previousDerivedValue;

	private IContentDescription cachedContentDescription;

	private ResourceEncodingFieldEditor encodingEditor;
	
	private LineDelimiterEditor lineDelimiterEditor;

	private static String READ_ONLY = IDEWorkbenchMessages.ResourceInfo_readOnly;

	private static String EXECUTABLE = IDEWorkbenchMessages.ResourceInfo_executable;
	
	private static String ARCHIVE = IDEWorkbenchMessages.ResourceInfo_archive;

	private static String DERIVED = IDEWorkbenchMessages.ResourceInfo_derived;

	private static String TYPE_TITLE = IDEWorkbenchMessages.ResourceInfo_type;

	private static String LOCATION_TITLE = IDEWorkbenchMessages.ResourceInfo_location;

	private static String RESOLVED_LOCATION_TITLE = IDEWorkbenchMessages.ResourceInfo_resolvedLocation;

	private static String SIZE_TITLE = IDEWorkbenchMessages.ResourceInfo_size;

	private static String BYTES_LABEL = IDEWorkbenchMessages.ResourceInfo_bytes;

	private static String FILE_LABEL = IDEWorkbenchMessages.ResourceInfo_file;

	private static String FILE_TYPE_FORMAT = IDEWorkbenchMessages.ResourceInfo_fileTypeFormat;

	private static String FOLDER_LABEL = IDEWorkbenchMessages.ResourceInfo_folder;

	private static String PROJECT_LABEL = IDEWorkbenchMessages.ResourceInfo_project;

	private static String LINKED_FILE_LABEL = IDEWorkbenchMessages.ResourceInfo_linkedFile;

	private static String LINKED_FOLDER_LABEL = IDEWorkbenchMessages.ResourceInfo_linkedFolder;

	private static String UNKNOWN_LABEL = IDEWorkbenchMessages.ResourceInfo_unknown;

	private static String NOT_LOCAL_TEXT = IDEWorkbenchMessages.ResourceInfo_notLocal;

	private static String MISSING_PATH_VARIABLE_TEXT = IDEWorkbenchMessages.ResourceInfo_undefinedPathVariable;

	private static String NOT_EXIST_TEXT = IDEWorkbenchMessages.ResourceInfo_notExist;

	private static String PATH_TITLE = IDEWorkbenchMessages.ResourceInfo_path;

	private static String TIMESTAMP_TITLE = IDEWorkbenchMessages.ResourceInfo_lastModified;

	private static String FILE_NOT_EXIST_TEXT = IDEWorkbenchMessages.ResourceInfo_fileNotExist;

	private static String FILE_ENCODING_TITLE = IDEWorkbenchMessages.WorkbenchPreference_encoding;

	private static String CONTAINER_ENCODING_TITLE = IDEWorkbenchMessages.ResourceInfo_fileEncodingTitle;

	//Max value width in characters before wrapping
	private static final int MAX_VALUE_WIDTH = 80;

	/**
	 * Create the group that shows the name, location, size and type.
	 * 
	 * @param parent
	 *            the composite the group will be created in
	 * @param resource
	 *            the resource the information is being taken from.
	 * @return the composite for the group
	 */
	private Composite createBasicInfoGroup(Composite parent, IResource resource) {

		Font font = parent.getFont();

		Composite basicInfoComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		basicInfoComposite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		basicInfoComposite.setLayoutData(data);
		basicInfoComposite.setFont(font);

		//The group for path
		Label pathLabel = new Label(basicInfoComposite, SWT.NONE);
		pathLabel.setText(PATH_TITLE);
		GridData gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		pathLabel.setLayoutData(gd);
		pathLabel.setFont(font);

		// path value label
		Text pathValueText = new Text(basicInfoComposite, SWT.WRAP
				| SWT.READ_ONLY);
		pathValueText.setText(resource.getFullPath().toString());
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		pathValueText.setLayoutData(gd);
		pathValueText.setFont(font);
		pathValueText.setBackground(pathValueText.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));

		//The group for types
		Label typeTitle = new Label(basicInfoComposite, SWT.LEFT);
		typeTitle.setText(TYPE_TITLE);
		typeTitle.setFont(font);

		Text typeValue = new Text(basicInfoComposite, SWT.LEFT | SWT.READ_ONLY);
		typeValue.setText(getTypeString(resource));
		typeValue.setBackground(typeValue.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		typeValue.setFont(font);

		//The group for location
		Label locationTitle = new Label(basicInfoComposite, SWT.LEFT);
		locationTitle.setText(LOCATION_TITLE);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		locationTitle.setLayoutData(gd);
		locationTitle.setFont(font);

		Text locationValue = new Text(basicInfoComposite, SWT.WRAP
				| SWT.READ_ONLY);
		locationValue.setText(getLocationText(resource));
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		locationValue.setLayoutData(gd);
		locationValue.setFont(font);
		locationValue.setBackground(locationValue.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));

		if (isPathVariable(resource)) {
			Label resolvedLocationTitle = new Label(basicInfoComposite,
					SWT.LEFT);
			resolvedLocationTitle.setText(RESOLVED_LOCATION_TITLE);
			gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			resolvedLocationTitle.setLayoutData(gd);
			resolvedLocationTitle.setFont(font);

			Text resolvedLocationValue = new Text(basicInfoComposite, SWT.WRAP
					| SWT.READ_ONLY);
			resolvedLocationValue.setText(getResolvedLocationText(resource));
			gd = new GridData();
			gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			resolvedLocationValue.setLayoutData(gd);
			resolvedLocationValue.setFont(font);
			resolvedLocationValue.setBackground(resolvedLocationValue
					.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		}
		if (resource.getType() == IResource.FILE) {
			//The group for size
			Label sizeTitle = new Label(basicInfoComposite, SWT.LEFT);
			sizeTitle.setText(SIZE_TITLE);
			sizeTitle.setFont(font);

			Text sizeValue = new Text(basicInfoComposite, SWT.LEFT
					| SWT.READ_ONLY);
			sizeValue.setText(getSizeString((IFile) resource));
			gd = new GridData();
			gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			sizeValue.setLayoutData(gd);
			sizeValue.setFont(font);
			sizeValue.setBackground(sizeValue.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));
		}

		return basicInfoComposite;
	}

	protected Control createContents(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IIDEHelpContextIds.RESOURCE_INFO_PROPERTY_PAGE);

		// layout the page
		IResource resource = (IResource) getElement();
		if (resource.getType() != IResource.PROJECT) {
			ResourceAttributes attrs = resource.getResourceAttributes();
			if (attrs != null) {
				previousReadOnlyValue = attrs.isReadOnly();
				previousExecutableValue = attrs.isExecutable();
				previousArchiveValue = attrs.isArchive();
			} else {
				previousReadOnlyValue = previousExecutableValue = previousArchiveValue = false;
			}
			previousDerivedValue = resource.isDerived();
		}

		// top level group
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		createBasicInfoGroup(composite, resource);
		createSeparator(composite);
		createStateGroup(composite, resource);
		new Label(composite, SWT.NONE); // a vertical spacer
		encodingEditor = new ResourceEncodingFieldEditor(
				getFieldEditorLabel(resource), composite, resource);
		encodingEditor.setPage(this);
		encodingEditor.load();

		encodingEditor.setPropertyChangeListener(new IPropertyChangeListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID))
					setValid(encodingEditor.isValid());

			}
		});
		
		if (resource.getType() == IResource.PROJECT) {
			lineDelimiterEditor = new LineDelimiterEditor(composite, resource.getProject());
			lineDelimiterEditor.doLoad();
		}

		return composite;
	}

	/**
	 * Return the label for the encoding field editor for the resource.
	 * 
	 * @param resource -
	 *            the resource to edit.
	 * @return String
	 */
	private String getFieldEditorLabel(IResource resource) {
		if (resource instanceof IContainer)
			return CONTAINER_ENCODING_TITLE;
		return FILE_ENCODING_TITLE;
	}

	/**
	 * Create the isEditable button and it's associated label as a child of
	 * parent using the editableValue of the receiver. The Composite will be the
	 * parent of the button.
	 * 
	 * @param composite
	 *            the parent of the button
	 */
	private void createEditableButton(Composite composite) {

		this.editableBox = new Button(composite, SWT.CHECK | SWT.RIGHT);
		this.editableBox.setAlignment(SWT.LEFT);
		this.editableBox.setText(READ_ONLY);
		this.editableBox.setSelection(this.previousReadOnlyValue);
		this.editableBox.setFont(composite.getFont());
		GridData data = new GridData();
		data.horizontalSpan = 2;
		this.editableBox.setLayoutData(data);
	}

	/**
	 * Create the isExecutable button and it's associated label as a child of
	 * parent using the editableValue of the receiver. The Composite will be the
	 * parent of the button.
	 * 
	 * @param composite
	 *            the parent of the button
	 */
	private void createExecutableButton(Composite composite) {

		this.executableBox = new Button(composite, SWT.CHECK | SWT.RIGHT);
		this.executableBox.setAlignment(SWT.LEFT);
		this.executableBox.setText(EXECUTABLE);
		this.executableBox.setSelection(this.previousExecutableValue);
		this.executableBox.setFont(composite.getFont());
		GridData data = new GridData();
		data.horizontalSpan = 2;
		this.executableBox.setLayoutData(data);
	}

	/**
	 * Create the isArchive button and it's associated label as a child of
	 * parent using the editableValue of the receiver. The Composite will be the
	 * parent of the button.
	 * 
	 * @param composite
	 *            the parent of the button
	 */
	private void createArchiveButton(Composite composite) {

		this.archiveBox = new Button(composite, SWT.CHECK | SWT.RIGHT);
		this.archiveBox.setAlignment(SWT.LEFT);
		this.archiveBox.setText(ARCHIVE);
		this.archiveBox.setSelection(this.previousArchiveValue);
		this.archiveBox.setFont(composite.getFont());
		GridData data = new GridData();
		data.horizontalSpan = 2;
		this.archiveBox.setLayoutData(data);
	}

	/**
	 * Create the derived button and it's associated label as a child of parent
	 * using the derived of the receiver. The Composite will be the parent of
	 * the button.
	 * 
	 * @param composite
	 *            the parent of the button
	 */
	private void createDerivedButton(Composite composite) {

		this.derivedBox = new Button(composite, SWT.CHECK | SWT.RIGHT);
		this.derivedBox.setAlignment(SWT.LEFT);
		this.derivedBox.setText(DERIVED);
		this.derivedBox.setSelection(this.previousDerivedValue);
		this.derivedBox.setFont(composite.getFont());
		GridData data = new GridData();
		data.horizontalSpan = 2;
		this.derivedBox.setLayoutData(data);
	}

	/**
	 * Create a separator that goes across the entire page
	 * 
	 * @param composite
	 *            The parent of the seperator
	 */
	private void createSeparator(Composite composite) {

		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	/**
	 * Create the group that shows the read only state and the timestamp.
	 * 
	 * @param parent
	 *            the composite the group will be created in
	 * @param resource
	 *            the resource the information is being taken from.
	 */
	private void createStateGroup(Composite parent, IResource resource) {

		Font font = parent.getFont();

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		composite.setFont(font);

		Label timeStampLabel = new Label(composite, SWT.NONE);
		timeStampLabel.setText(TIMESTAMP_TITLE);
		timeStampLabel.setFont(font);

		// timeStamp value label
		Text timeStampValue = new Text(composite, SWT.READ_ONLY);
		timeStampValue.setText(getDateStringValue(resource));
		timeStampValue.setFont(font);
		timeStampValue.setBackground(timeStampValue.getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		timeStampValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));

		//Not relevant to projects
		if (resource.getType() != IResource.PROJECT) {
			createEditableButton(composite);
			createExecutableButton(composite);
			createArchiveButton(composite);
			createDerivedButton(composite);
		}
	}

	private String getContentTypeString(IFile file) {
		IContentDescription description = getContentDescription(file);
		if (description != null) {
			IContentType contentType = description.getContentType();
			if (contentType != null)
				return contentType.getName();
		}
		return null;
	}

	private IContentDescription getContentDescription(IFile file) {
		if (cachedContentDescription == null) {
			try {
				cachedContentDescription = file.getContentDescription();
			} catch (CoreException e) {
				// silently ignore
			}
		}
		return cachedContentDescription;
	}

	/**
	 * Return the value for the date String for the timestamp of the supplied
	 * resource.
	 * 
	 * @param resource
	 *            The resource to query
	 * @return String
	 */
	private String getDateStringValue(IResource resource) {
		if (!resource.isLocal(IResource.DEPTH_ZERO))
			return NOT_LOCAL_TEXT;

		IPath location = resource.getLocation();
		if (location == null) {
			if (resource.isLinked())
				return MISSING_PATH_VARIABLE_TEXT;

			return NOT_EXIST_TEXT;
		}

		File localFile = location.toFile();
		if (localFile.exists()) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG,
					DateFormat.MEDIUM);
			return format.format(new Date(localFile.lastModified()));
		}
		return NOT_EXIST_TEXT;

	}

	/**
	 * Get the location of a resource
	 * 
	 * @param resource
	 * @return String the text to display the location
	 */
	private String getLocationText(IResource resource) {
		if (!resource.isLocal(IResource.DEPTH_ZERO))
			return NOT_LOCAL_TEXT;

		IPath resolvedLocation = resource.getLocation();
		IPath location = resolvedLocation;
		if (resource.isLinked()) {
			location = resource.getRawLocation();
		}
		if (location == null) {
			return NOT_EXIST_TEXT;
		}

		String locationString = location.toOSString();
		if (resolvedLocation != null && !isPathVariable(resource)) {
			// No path variable used. Display the file not exist message
			// in the location. Fixes bug 33318.
			File file = resolvedLocation.toFile();
			if (!file.exists()) {
				locationString += " " + FILE_NOT_EXIST_TEXT; //$NON-NLS-1$ 
			}
		}
		return locationString;

	}

	/**
	 * Get the resolved location of a resource. This resolves path variables if
	 * present in the resource path.
	 * 
	 * @param resource
	 * @return String
	 */
	private String getResolvedLocationText(IResource resource) {
		if (!resource.isLocal(IResource.DEPTH_ZERO))
			return NOT_LOCAL_TEXT;

		IPath location = resource.getLocation();
		if (location == null) {
			if (resource.isLinked())
				return MISSING_PATH_VARIABLE_TEXT;

			return NOT_EXIST_TEXT;
		}

		String locationString = location.toOSString();
		File file = location.toFile();

		if (!file.exists()) {
			locationString += " " + FILE_NOT_EXIST_TEXT; //$NON-NLS-1$ 
		}
		return locationString;

	}

	/**
	 * Return a String that indicates the size of the supplied file.
	 * 
	 * @param file
	 * @return String
	 */
	private String getSizeString(IFile file) {
		if (!file.isLocal(IResource.DEPTH_ZERO))
			return NOT_LOCAL_TEXT;

		IPath location = file.getLocation();
		if (location == null) {
			if (file.isLinked())
				return MISSING_PATH_VARIABLE_TEXT;

			return NOT_EXIST_TEXT;
		}

		File localFile = location.toFile();

		if (localFile.exists()) {
			String bytesString = Long.toString(localFile.length());
			return MessageFormat.format(BYTES_LABEL,
					new Object[] { bytesString });
		}
		return NOT_EXIST_TEXT;

	}

	/**
	 * Get the string that identifies the type of this resource.
	 * 
	 * @param resource
	 * @return String
	 */
	private String getTypeString(IResource resource) {

		if (resource.getType() == IResource.FILE) {
			if (resource.isLinked())
				return LINKED_FILE_LABEL;

			if (resource instanceof IFile) {
				String contentType = getContentTypeString((IFile) resource);
				if (contentType != null)
					return MessageFormat.format(FILE_TYPE_FORMAT,
							new String[] { contentType });
			}
			return FILE_LABEL;
		}

		if (resource.getType() == IResource.FOLDER) {
			if (resource.isLinked())
				return LINKED_FOLDER_LABEL;

			return FOLDER_LABEL;
		}

		if (resource.getType() == IResource.PROJECT)
			return PROJECT_LABEL;

		//Should not be possible
		return UNKNOWN_LABEL;
	}

	/**
	 * Returns whether the given resource is a linked resource bound to a path
	 * variable.
	 * 
	 * @param resource
	 *            resource to test
	 * @return boolean <code>true</code> the given resource is a linked
	 *         resource bound to a path variable. <code>false</code> the given
	 *         resource is either not a linked resource or it is not using a
	 *         path variable.
	 */
	private boolean isPathVariable(IResource resource) {
		if (!resource.isLinked())
			return false;

		IPath resolvedLocation = resource.getLocation();
		if (resolvedLocation == null) {
			// missing path variable
			return true;
		}
		IPath rawLocation = resource.getRawLocation();
		if (resolvedLocation.equals(rawLocation))
			return false;

		return true;
	}

	/**
	 * Reset the editableBox to the false.
	 */
	protected void performDefaults() {

		//Nothing to update if we never made the box
		if (this.editableBox != null)
			this.editableBox.setSelection(false);
		
		//Nothing to update if we never made the box
		if (this.executableBox != null)
			this.executableBox.setSelection(false);

		//Nothing to update if we never made the box
		if (this.derivedBox != null)
			this.derivedBox.setSelection(false);

		encodingEditor.loadDefault();
		
		if (lineDelimiterEditor != null)
			lineDelimiterEditor.loadDefault();

	}

	/**
	 * Apply the read only state and the encoding to the resource.
	 */
	public boolean performOk() {

		IResource resource = (IResource) getElement();

		encodingEditor.store();
		
		if (lineDelimiterEditor != null)
			lineDelimiterEditor.store();

		try {
			//Nothing to update if we never made the box
			if (editableBox != null && executableBox != null
					&& archiveBox != null) {
				boolean localReadOnlyValue = editableBox.getSelection();
				boolean localExecutableValue = executableBox.getSelection();
				boolean localArchiveValue = archiveBox.getSelection();
				ResourceAttributes attrs = resource.getResourceAttributes();
				if (attrs != null) {
					attrs.setExecutable(localExecutableValue);
					attrs.setReadOnly(localReadOnlyValue);
					attrs.setArchive(localArchiveValue);
					if (previousReadOnlyValue != localReadOnlyValue
							|| previousExecutableValue != localExecutableValue
							|| previousArchiveValue != localArchiveValue) {
						resource.setResourceAttributes(attrs);
						attrs = resource.getResourceAttributes();
						if (attrs != null) {
							previousReadOnlyValue = attrs.isReadOnly();
							previousExecutableValue = attrs.isExecutable();
							previousArchiveValue = attrs.isArchive();
							editableBox.setSelection(attrs.isReadOnly());
							executableBox.setSelection(attrs.isExecutable());
							archiveBox.setSelection(attrs.isArchive());
						}
					}
				}
			}

			//Nothing to update if we never made the box
			if (this.derivedBox != null) {
				boolean localDerivedValue = derivedBox.getSelection();
				if (previousDerivedValue != localDerivedValue) {
					resource.setDerived(localDerivedValue);
					boolean isDerived = resource.isDerived();
					previousDerivedValue = isDerived;
					derivedBox.setSelection(isDerived);
				}
			}
		} catch (CoreException exception) {
			ErrorDialog.openError(getShell(), IDEWorkbenchMessages.InternalError,
					exception.getLocalizedMessage(), exception.getStatus());
			return false;
		}
		return true;
	}
}
