/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * This is a dummy page that is added to the property dialog
 * when multiple elements are selected. At the moment
 * we don't handle multiple selection in a meaningful way.
 */
public class ResourceInfoPage extends PropertyPage {
	
	private Button editableBox;
	private Button derivedBox;
	private boolean previousReadOnlyValue;
	private boolean previousDerivedValue;
	private IContentDescription cachedContentDescription;
	
	private Combo encodingCombo;
	private Button defaultEncodingButton;
	private Button otherEncodingButton;	
	
	private static String READ_ONLY = IDEWorkbenchMessages.getString("ResourceInfo.readOnly"); //$NON-NLS-1$
	private static String DERIVED = IDEWorkbenchMessages.getString("ResourceInfo.derived"); //$NON-NLS-1$
	private static String TYPE_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.type"); //$NON-NLS-1$
	private static String LOCATION_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.location"); //$NON-NLS-1$
	private static String RESOLVED_LOCATION_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.resolvedLocation"); //$NON-NLS-1$
	private static String SIZE_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.size"); //$NON-NLS-1$
	private static String BYTES_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.bytes"); //$NON-NLS-1$
	private static String FILE_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.file"); //$NON-NLS-1$
	private static String FILE_TYPE_FORMAT = IDEWorkbenchMessages.getString("ResourceInfo.fileTypeFormat"); //$NON-NLS-1$
	private static String FOLDER_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.folder"); //$NON-NLS-1$
	private static String PROJECT_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.project"); //$NON-NLS-1$
	private static String LINKED_FILE_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.linkedFile"); //$NON-NLS-1$
	private static String LINKED_FOLDER_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.linkedFolder"); //$NON-NLS-1$
	private static String UNKNOWN_LABEL = IDEWorkbenchMessages.getString("ResourceInfo.unknown"); //$NON-NLS-1$
	private static String NOT_LOCAL_TEXT = IDEWorkbenchMessages.getString("ResourceInfo.notLocal"); //$NON-NLS-1$
	private static String MISSING_PATH_VARIABLE_TEXT = IDEWorkbenchMessages.getString("ResourceInfo.undefinedPathVariable"); //$NON-NLS-1$
	private static String NOT_EXIST_TEXT = IDEWorkbenchMessages.getString("ResourceInfo.notExist"); //$NON-NLS-1$
	private static String PATH_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.path"); //$NON-NLS-1$
	private static String TIMESTAMP_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.lastModified"); //$NON-NLS-1$
	private static String FILE_NOT_EXIST_TEXT = IDEWorkbenchMessages.getString("ResourceInfo.fileNotExist"); //$NON-NLS-1$
	private static String FILE_ENCODING_TITLE = IDEWorkbenchMessages.getString("WorkbenchPreference.encoding"); //$NON-NLS-1$
	private static String CONTAINER_ENCODING_TITLE = IDEWorkbenchMessages.getString("ResourceInfo.fileEncodingTitle"); //$NON-NLS-1$
	private static String FILE_CONTENT_ENCODING_FORMAT = IDEWorkbenchMessages.getString("ResourceInfo.fileContentEncodingFormat"); //$NON-NLS-1$
	private static String FILE_CONTAINER_ENCODING_FORMAT = IDEWorkbenchMessages.getString("ResourceInfo.fileContainerEncodingFormat"); //$NON-NLS-1$
	private static String CONTAINER_ENCODING_FORMAT = IDEWorkbenchMessages.getString("ResourceInfo.containerEncodingFormat"); //$NON-NLS-1$

	//Max value width in characters before wrapping
	private static final int MAX_VALUE_WIDTH = 80;

/**
 * Create the group that shows the name, location, size and type.
 *
 * @param parent the composite the group will be created in
 * @param resource the resource the information is being taken from.
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
	Text pathValueText = new Text(basicInfoComposite, SWT.WRAP | SWT.READ_ONLY);
	pathValueText.setText(resource.getFullPath().toString());
	gd = new GridData();
	gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
	gd.grabExcessHorizontalSpace = true;
	gd.horizontalAlignment = GridData.FILL;
	pathValueText.setLayoutData(gd);
	pathValueText.setFont(font);
	
	//The group for types
	Label typeTitle = new Label(basicInfoComposite, SWT.LEFT);
	typeTitle.setText(TYPE_TITLE);
	typeTitle.setFont(font);
	
	Text typeValue = new Text(basicInfoComposite, SWT.LEFT | SWT.READ_ONLY);
	typeValue.setText(getTypeString(resource));
	typeValue.setFont(font);

	//The group for location
	Label locationTitle = new Label(basicInfoComposite, SWT.LEFT);
	locationTitle.setText(LOCATION_TITLE);
	gd = new GridData();
	gd.verticalAlignment = SWT.TOP;
	locationTitle.setLayoutData(gd);
	locationTitle.setFont(font);
	
	Text locationValue = new Text(basicInfoComposite, SWT.WRAP | SWT.READ_ONLY);
	locationValue.setText(getLocationText(resource));
	gd = new GridData();
	gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
	gd.grabExcessHorizontalSpace = true;
	gd.horizontalAlignment = GridData.FILL;
	locationValue.setLayoutData(gd);
	locationValue.setFont(font);
	
	if (isPathVariable(resource)) {
		Label resolvedLocationTitle = new Label(basicInfoComposite, SWT.LEFT);
		resolvedLocationTitle.setText(RESOLVED_LOCATION_TITLE);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		resolvedLocationTitle.setLayoutData(gd);
		resolvedLocationTitle.setFont(font);
		
		Text resolvedLocationValue = new Text(basicInfoComposite, SWT.WRAP | SWT.READ_ONLY);
		resolvedLocationValue.setText(getResolvedLocationText(resource));
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		resolvedLocationValue.setLayoutData(gd);
		resolvedLocationValue.setFont(font);
	}
	if (resource.getType() == IResource.FILE) {
		//The group for size
		Label sizeTitle = new Label(basicInfoComposite, SWT.LEFT);
		sizeTitle.setText(SIZE_TITLE);
		sizeTitle.setFont(font);
		
		Text sizeValue = new Text(basicInfoComposite, SWT.LEFT | SWT.READ_ONLY);
		sizeValue.setText(getSizeString((IFile) resource));
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		sizeValue.setLayoutData(gd);
		sizeValue.setFont(font);
	}

	return basicInfoComposite;
}
protected Control createContents(Composite parent) {

	WorkbenchHelp.setHelp(getControl(), IHelpContextIds.RESOURCE_INFO_PROPERTY_PAGE);

	// layout the page
	IResource resource = (IResource) getElement();
	if(resource.getType()!= IResource.PROJECT) {
		this.previousReadOnlyValue = resource.isReadOnly();
		this.previousDerivedValue = resource.isDerived();
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
	createStateGroup(composite,resource);
	new Label(composite, SWT.NONE);	// a vertical spacer
	createEncodingGroup(composite, resource);

	return composite;
}
/**
 * Create the isEditable button and it's associated label as a child of parent
 * using the editableValue of the receiver. The Composite will be the parent of
 * the button.
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
 * Create the derived button and it's associated label as a child of parent
 * using the derived of the receiver. The Composite will be the parent of
 * the button.
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
 */
private void createSeparator(Composite composite) {

	Label separator =
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	separator.setLayoutData(gridData);
}
/**
 * Create the group that shows the read only state and the timestamp.
 *
 * @return the composite for the group
 * @param parent the composite the group will be created in
 * @param resource the resource the information is being taken from.
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
	timeStampValue.setLayoutData(
		new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

	//Not relevant to projects
	if(resource.getType() != IResource.PROJECT) {
		createEditableButton(composite);
		createDerivedButton(composite);
	}
}

private void createEncodingGroup(Composite parent, IResource resource) {
	
	Font font = parent.getFont();
	Group group = new Group(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	group.setLayout(layout);
	if (resource instanceof IContainer)
		group.setText(CONTAINER_ENCODING_TITLE);
	else
		group.setText(FILE_ENCODING_TITLE);
	group.setFont(font);
	
	SelectionAdapter buttonListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			updateEncodingState(defaultEncodingButton.getSelection());
			updateValidState();
		}
	};
	
	defaultEncodingButton = new Button(group, SWT.RADIO);
	
	String encoding = getEncoding(resource);
	String format = CONTAINER_ENCODING_FORMAT;
	String defaultEnc = null;
	
	if (resource instanceof IFile) {
		defaultEnc= getEncodingFromContent((IFile) resource);
		format = (defaultEnc != null) ? FILE_CONTENT_ENCODING_FORMAT : FILE_CONTAINER_ENCODING_FORMAT;
	}
	if (defaultEnc == null)
		defaultEnc = getEncoding(resource.getParent());		
	
	defaultEncodingButton.setText(MessageFormat.format(format, new String[] { defaultEnc }));
	
	GridData data = new GridData();
	data.horizontalSpan = 2;
	defaultEncodingButton.setLayoutData(data);
	defaultEncodingButton.addSelectionListener(buttonListener);
	defaultEncodingButton.setFont(font);
	
	otherEncodingButton = new Button(group, SWT.RADIO);
	otherEncodingButton.setText(IDEWorkbenchMessages.getString("WorkbenchPreference.otherEncoding")); //$NON-NLS-1$
	otherEncodingButton.addSelectionListener(buttonListener);
	otherEncodingButton.setFont(font);
	
	encodingCombo = new Combo(group, SWT.NONE);
	data = new GridData();
	data.widthHint = convertWidthInCharsToPixels(15);
	encodingCombo.setFont(font);
	encodingCombo.setLayoutData(data);
	encodingCombo.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateValidState();
		}
	});

	ArrayList encodings = new ArrayList();
	int n = 0;
	try {
		n = Integer.parseInt(IDEWorkbenchMessages.getString("WorkbenchPreference.numDefaultEncodings")); //$NON-NLS-1$
	}
	catch (NumberFormatException e1) {
		// Ignore;
	}
	for (int i = 0; i < n; ++i) {
		String enc = IDEWorkbenchMessages.getString("WorkbenchPreference.defaultEncoding" + (i+1), null); //$NON-NLS-1$
		if (enc != null)
			encodings.add(enc);
	}
	
	String defaultEnc1 = System.getProperty("file.encoding", "UTF-8");  //$NON-NLS-1$  //$NON-NLS-2$
	if (!encodings.contains(defaultEnc1))
		encodings.add(defaultEnc1);
	
	String enc = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
	if (!encodings.contains(enc))
		encodings.add(enc);
	
	Collections.sort(encodings);
	for (int i = 0; i < encodings.size(); ++i)
		encodingCombo.add((String) encodings.get(i));
	
	encodingCombo.setText(encoding);

	updateEncodingState(usesDefaultEncoding(resource));
}

private String getEncoding(IResource resource) {
	try {
		if (resource instanceof IContainer)
			return ((IContainer)resource).getDefaultCharset();
		else if (resource instanceof IFile)
			return ((IFile)resource).getCharset();
	} catch (CoreException e) {
	}
	IContainer parent= resource.getParent();
	return getEncoding(parent);
}

private String getEncodingFromContent(IFile file) {
	IContentDescription description = getContentDescription(file);
	if (description != null) {
		byte[] bom = (byte[]) description.getProperty(IContentDescription.BYTE_ORDER_MARK);
		if (bom == null)
			return (String) description.getProperty(IContentDescription.CHARSET);
		if (bom == IContentDescription.BOM_UTF_8)
			return IDEWorkbenchMessages.getString("WorkbenchPreference.encoding.BOM_UTF_8"); //$NON-NLS-1$
		if (bom == IContentDescription.BOM_UTF_16BE)
			return IDEWorkbenchMessages.getString("WorkbenchPreference.encoding.BOM_UTF_16BE"); //$NON-NLS-1$
		if (bom == IContentDescription.BOM_UTF_16LE)
			return IDEWorkbenchMessages.getString("WorkbenchPreference.encoding.BOM_UTF_16LE"); //$NON-NLS-1$
	}
	return null;
}

private String getContentTypeString(IFile file) {
	IContentDescription description = getContentDescription(file);
	if (description != null) {
		IContentType contentType= description.getContentType();
		if (contentType != null)
			return contentType.getName();
	}
	return null;	
}

private IContentDescription getContentDescription(IFile file) {
	if (cachedContentDescription == null) {
		try {
			cachedContentDescription= file.getContentDescription();
		} catch (CoreException e) {
			// silently ignore
		}
	}
	return cachedContentDescription;
}

private boolean usesDefaultEncoding(IResource resource) {
	try {
		if (resource instanceof IContainer)
			return ((IContainer)resource).getDefaultCharset(false) == null;
		if (resource instanceof IFile)
			return ((IFile)resource).getCharset(false) == null;
	} catch (CoreException e) {
	}
	return true;
}

private void updateEncodingState(boolean useDefault) {
	defaultEncodingButton.setSelection(useDefault);
	otherEncodingButton.setSelection(!useDefault);
	encodingCombo.setEnabled(!useDefault);
	updateValidState();
}		

protected void updateValidState() {
	if (isEncodingValid()) {
		setErrorMessage(null);
		setValid(true);
	} else {
		setErrorMessage(IDEWorkbenchMessages.getString("WorkbenchPreference.unsupportedEncoding")); //$NON-NLS-1$
		setValid(false);
	}
}

private boolean isEncodingValid() {
	return defaultEncodingButton.getSelection() || isValidEncoding(encodingCombo.getText());
}

private boolean isValidEncoding(String enc) {
	try {
		new String(new byte[0], enc);
		return true;
	} catch (UnsupportedEncodingException e) {
		return false;
	}
}

/////////////////////////////

/**
 * Return the value for the date String for the timestamp of the supplied resource.
 * @return String
 * @param IResource - the resource to query
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
	else {
		File localFile = location.toFile();
		if (localFile.exists()) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
			return format.format(new Date(localFile.lastModified()));
		}
		return NOT_EXIST_TEXT;
	}
}
/**
 * Get the location of a resource
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
	else {
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
}
/**
 * Get the resolved location of a resource.
 * This resolves path variables if present in the resource path.
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
	else {
		String locationString = location.toOSString();
		File file = location.toFile();
		
		if (!file.exists()) {
			locationString += " " + FILE_NOT_EXIST_TEXT; //$NON-NLS-1$ 
		}
		return locationString;
	}
}
/**
 * Return a String that indicates the size of the supplied file.
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
	else {
		File localFile = location.toFile();
		
		if (localFile.exists()) {
			String bytesString = Long.toString(localFile.length());
			return MessageFormat.format(BYTES_LABEL, new Object[] {bytesString});
		}
		return NOT_EXIST_TEXT;
	}
}
/**
 * Get the string that identifies the type of this resource.
 */
private String getTypeString(IResource resource) {
	
	if(resource.getType() == IResource.FILE) {
		if (resource.isLinked())
			return LINKED_FILE_LABEL;
			
		if (resource instanceof IFile) {
			String contentType= getContentTypeString((IFile)resource);
			if (contentType != null)
				return MessageFormat.format(FILE_TYPE_FORMAT, new String[] { contentType });
		}
		return FILE_LABEL;
	}

	if(resource.getType() == IResource.FOLDER) {
		if (resource.isLinked())
			return LINKED_FOLDER_LABEL;
			
		return FOLDER_LABEL;
	}

	if(resource.getType() == IResource.PROJECT)
		return PROJECT_LABEL;

	//Should not be possible
	return UNKNOWN_LABEL;
}

/**
 * Returns whether the given resource is a linked resource bound 
 * to a path variable.
 * 
 * @param resource resource to test
 * @return boolean <code>true</code> the given resource is a linked 
 * 	resource bound to a path variable. <code>false</code> the given 
 * 	resource is either not a linked resource or it is not using a
 * 	path variable.  
 */
private boolean isPathVariable(IResource resource){
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
	if(this.editableBox != null)
		this.editableBox.setSelection(false);

	//Nothing to update if we never made the box
	if(this.derivedBox != null)
		this.derivedBox.setSelection(false);
	
	if (defaultEncodingButton != null)
		updateEncodingState(true);
}
/** 
 * Apply the read only state and the encoding to the resource.
 */
public boolean performOk() {
	
	IResource resource = (IResource) getElement();
	
	// set encoding
	if (isEncodingValid()) {
		String previousEncoding= null;
		if (!usesDefaultEncoding(resource))
			previousEncoding= getEncoding(resource);
		
		String newEncoding= null;
		if (!defaultEncodingButton.getSelection())
			newEncoding= encodingCombo.getText();
	
		if ((previousEncoding == null && newEncoding != null) ||
			(previousEncoding != null && newEncoding == null) ||
			(previousEncoding != null && newEncoding != null && !previousEncoding.equals(newEncoding))) {		
			try {
				if (resource instanceof IFile)
					((IFile)resource).setCharset(newEncoding);
				else if (resource instanceof IContainer)
					((IContainer)resource).setDefaultCharset(newEncoding);
			} catch (CoreException e) {
				ErrorDialog.openError(
						getShell(), 
						IDEWorkbenchMessages.getString("InternalError"), //$NON-NLS-1$
						e.getLocalizedMessage(),
						e.getStatus()); 
					return false;
			}
		}
	}
	
	//Nothing to update if we never made the box
	if(this.editableBox != null) {
		boolean localReadOnlyValue = editableBox.getSelection();
		if (previousReadOnlyValue != localReadOnlyValue) {
			resource.setReadOnly(localReadOnlyValue);
		}
	}
	
	//Nothing to update if we never made the box
	if(this.derivedBox != null){
		try{
			boolean localDerivedValue = derivedBox.getSelection();
			if (previousDerivedValue != localDerivedValue) {
				resource.setDerived(localDerivedValue);
			}
		}
		catch (CoreException exception){
			ErrorDialog.openError(
				getShell(), 
				IDEWorkbenchMessages.getString("InternalError"), //$NON-NLS-1$
				exception.getLocalizedMessage(),
				exception.getStatus()); 
			return false;
		}
	}		
	return true;
}
}
