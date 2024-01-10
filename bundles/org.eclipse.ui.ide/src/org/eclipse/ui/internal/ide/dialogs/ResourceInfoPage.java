/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 175069 [Preferences] ResourceInfoPage is not setting dialog font on all widgets
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474273
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 486777
 *     Dinesh Palanisamy (ETAS GmbH) - Issue #998 Copy as path for resources
 *     Dinesh Palanisamy (ETAS GmbH) - Issue #1440 Improvement in generics
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.LineDelimiterEditor;
import org.eclipse.ui.internal.ide.handlers.ShowInSystemExplorerHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The ResourceInfoPage is the page that shows the basic info about the
 * resource.
 */
public class ResourceInfoPage extends PropertyPage {
	private interface IResourceChange {
		public String getMessage();

		public void performChange(IResource resource) throws CoreException;
	}

	private Button editableBox;

	private Button executableBox;

	private Button archiveBox;

	private Button derivedBox;

	private Button immutableBox;

	private Button permissionBoxes[];

	private boolean previousReadOnlyValue;

	private boolean previousExecutableValue;

	private boolean previousArchiveValue;

	private boolean previousDerivedValue;

	private int previousPermissionsValue;

	private IContentDescription cachedContentDescription;

	private ResourceEncodingFieldEditor encodingEditor;

	private LineDelimiterEditor lineDelimiterEditor;

	private static String READ_ONLY = IDEWorkbenchMessages.ResourceInfo_readOnly;

	private static String EXECUTABLE = IDEWorkbenchMessages.ResourceInfo_executable;

	private static String LOCKED = IDEWorkbenchMessages.ResourceInfo_locked;

	private static String ARCHIVE = IDEWorkbenchMessages.ResourceInfo_archive;

	private static String DERIVED = IDEWorkbenchMessages.ResourceInfo_derived;

	private static String DERIVED_HAS_DERIVED_ANCESTOR = IDEWorkbenchMessages.ResourceInfo_derivedHasDerivedAncestor;

	private static String TYPE_TITLE = IDEWorkbenchMessages.ResourceInfo_type;

	private static String LOCATION_TITLE = IDEWorkbenchMessages.ResourceInfo_location;

	private static String LOCATION_BUTTON_TOOLTIP = IDEWorkbenchMessages.ResourceInfo_location_button_tooltip;

	private static String PATH_BUTTON_TOOLTIP = IDEWorkbenchMessages.ResourceInfo_path_button_tooltip;

	private static String RESOLVED_LOCATION_TITLE = IDEWorkbenchMessages.ResourceInfo_resolvedLocation;

	private static String SIZE_TITLE = IDEWorkbenchMessages.ResourceInfo_size;

	private static String PATH_TITLE = IDEWorkbenchMessages.ResourceInfo_path;

	private static String TIMESTAMP_TITLE = IDEWorkbenchMessages.ResourceInfo_lastModified;

	private static String FILE_ENCODING_TITLE = IDEWorkbenchMessages.WorkbenchPreference_encoding;

	private static String CONTAINER_ENCODING_TITLE = IDEWorkbenchMessages.ResourceInfo_fileEncodingTitle;

	private static String EDIT_TITLE = IDEWorkbenchMessages.ResourceInfo_edit;

	private Text resolvedLocationValue = null;
	private Text locationValue = null;
	private Text sizeValue = null;
	private IPath newResourceLocation = null;

	// Max value width in characters before wrapping
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
		initializeDialogUnits(parent);

		Composite basicInfoComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		basicInfoComposite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		basicInfoComposite.setLayoutData(data);

		// The group for path
		Label pathLabel = new Label(basicInfoComposite, SWT.NONE);
		pathLabel.setText(PATH_TITLE);
		GridData gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		pathLabel.setLayoutData(gd);

		// path value label
		Text pathValueText = new Text(basicInfoComposite, SWT.WRAP
				| SWT.READ_ONLY);
		String pathString = TextProcessor.process(resource.getFullPath()
				.toString());
		pathValueText.setText(pathString);
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 3;
		pathValueText.setLayoutData(gd);
		pathValueText.setBackground(pathValueText.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));

		// The group for types
		Label typeTitle = new Label(basicInfoComposite, SWT.LEFT);
		typeTitle.setText(TYPE_TITLE);

		Text typeValue = new Text(basicInfoComposite, SWT.LEFT | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().span(3, SWT.DEFAULT).applyTo(typeValue);
		typeValue.setText(IDEResourceInfoUtils.getTypeString(resource,
				getContentDescription(resource)));
		typeValue.setBackground(typeValue.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));

		if (resource.isLinked() && !resource.isVirtual()) {
			// The group for location
			Label locationTitle = new Label(basicInfoComposite, SWT.LEFT);
			locationTitle.setText(LOCATION_TITLE);
			gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			locationTitle.setLayoutData(gd);

			Composite locationComposite = new Composite(basicInfoComposite,
					SWT.NULL);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			locationComposite.setLayout(layout);
			gd = new GridData();
			gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = SWT.TOP;
			gd.horizontalAlignment = GridData.FILL;
			gd.horizontalSpan = 3;
			locationComposite.setLayoutData(gd);

			locationValue = new Text(locationComposite, SWT.WRAP
					| SWT.READ_ONLY);
			String locationStr = TextProcessor.process(IDEResourceInfoUtils
					.getLocationText(resource));
			locationValue.setText(locationStr);
			gd = new GridData();
			gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
			gd.grabExcessHorizontalSpace = true;
			gd.verticalAlignment = SWT.TOP;
			gd.horizontalAlignment = GridData.FILL;
			locationValue.setLayoutData(gd);
			locationValue.setBackground(locationValue.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));

			Button editButton = new Button(locationComposite, SWT.PUSH);
			editButton.setText(EDIT_TITLE);
			setButtonLayoutData(editButton);
			((GridData) editButton.getLayoutData()).verticalAlignment = SWT.TOP;
			int locationValueHeight = locationValue.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			int editButtonHeight = editButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
			int verticalIndent = (editButtonHeight - locationValueHeight) / 2;
			((GridData) locationTitle.getLayoutData()).verticalIndent = verticalIndent;
			((GridData) locationValue.getLayoutData()).verticalIndent = verticalIndent;
			editButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					editLinkLocation();
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					editLinkLocation();
				}
			});

			// displayed in all cases since the link can be changed to a path variable any time by the user in this dialog
			Label resolvedLocationTitle = new Label(basicInfoComposite,
					SWT.LEFT);
			resolvedLocationTitle.setText(RESOLVED_LOCATION_TITLE);
			gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			resolvedLocationTitle.setLayoutData(gd);

			resolvedLocationValue = new Text(basicInfoComposite, SWT.WRAP
					| SWT.READ_ONLY);
			resolvedLocationValue.setText(IDEResourceInfoUtils
					.getResolvedLocationText(resource));
			gd = new GridData();
			gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.horizontalSpan = 3;
			resolvedLocationValue.setLayoutData(gd);
			resolvedLocationValue.setBackground(resolvedLocationValue
					.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		} else if (!resource.isVirtual()) {
			// The group for location
			Label locationTitle = new Label(basicInfoComposite, SWT.LEFT);
			locationTitle.setText(LOCATION_TITLE);
			gd = new GridData();
			locationTitle.setLayoutData(gd);

			locationValue = new Text(basicInfoComposite, SWT.WRAP
					| SWT.READ_ONLY);
			final String locationStr = TextProcessor.process(IDEResourceInfoUtils
					.getLocationText(resource));
			locationValue.setText(locationStr);
			gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			locationValue.setLayoutData(gd);
			locationValue.setBackground(locationValue.getDisplay()
					.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

			Button goToLocationButton = new Button(basicInfoComposite, SWT.PUSH);
			gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			goToLocationButton.setLayoutData(gd);
			ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(),
					goToLocationButton);
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			URL goToFolderUrl = FileLocator.find(bundle, IPath.fromOSString("icons/full/obj16/goto_input.png"), //$NON-NLS-1$
					null);
			goToLocationButton.setImage(resourceManager.create(ImageDescriptor.createFromURL(goToFolderUrl)));
			goToLocationButton.setToolTipText(LOCATION_BUTTON_TOOLTIP);
			goToLocationButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ECommandService commandService = PlatformUI.getWorkbench().getService(ECommandService.class);
					EHandlerService handlerService = PlatformUI.getWorkbench().getService(EHandlerService.class);

					Command command = commandService.getCommand(ShowInSystemExplorerHandler.ID);
					if (command.isDefined()) {
						ParameterizedCommand parameterizedCommand = commandService
								.createCommand(ShowInSystemExplorerHandler.ID, Collections.singletonMap(
										ShowInSystemExplorerHandler.RESOURCE_PATH_PARAMETER, locationStr));
						if (handlerService.canExecute(parameterizedCommand)) {
							handlerService.executeHandler(parameterizedCommand);
						}
					}
				}
			});

			Button pathCopy = new Button(basicInfoComposite, SWT.PUSH);
			pathCopy.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
			pathCopy.setToolTipText(PATH_BUTTON_TOOLTIP);
			pathCopy.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					StringSelection strSelection = new StringSelection(locationStr);
					Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					systemClipboard.setContents(strSelection, null);
				}
			});
		}
		if (resource.getType() == IResource.FILE) {
			// The group for size
			Label sizeTitle = new Label(basicInfoComposite, SWT.LEFT);
			sizeTitle.setText(SIZE_TITLE);

			sizeValue = new Text(basicInfoComposite, SWT.LEFT
					| SWT.READ_ONLY);
			sizeValue.setText(IDEResourceInfoUtils.getSizeString(resource));
			gd = new GridData();
			gd.widthHint = convertWidthInCharsToPixels(MAX_VALUE_WIDTH);
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.horizontalSpan = 3;
			sizeValue.setLayoutData(gd);
			sizeValue.setBackground(sizeValue.getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));
		}

		Label timeStampLabel = new Label(basicInfoComposite, SWT.NONE);
		timeStampLabel.setText(TIMESTAMP_TITLE);

		// timeStamp value label
		Text timeStampValue = new Text(basicInfoComposite, SWT.READ_ONLY);
		timeStampValue.setText(IDEResourceInfoUtils
				.getDateStringValue(resource));
		timeStampValue.setBackground(timeStampValue.getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 3;
		timeStampValue.setLayoutData(gridData);

		return basicInfoComposite;
	}

	protected void editLinkLocation() {
		IResource resource = Adapters.adapt(getElement(), IResource.class);
		String locationFormat = resource.getPathVariableManager().convertFromUserEditableFormat(locationValue.getText(), true);
		IPath location = IPath.fromOSString(locationFormat);

		PathVariableDialog dialog = new PathVariableDialog(getShell(),
				PathVariableDialog.EDIT_LINK_LOCATION, resource.getType(),
				resource.getPathVariableManager(), null);
		dialog.setLinkLocation(location);
		dialog.setResource(resource);
		// opens the dialog - just returns if the user cancels it
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		location = IPath.fromOSString(dialog.getVariableValue());
		newResourceLocation = location;
		refreshLinkLocation();
	}

	private void refreshLinkLocation() {
		IResource resource = Adapters.adapt(getElement(), IResource.class);

		String userEditableFormat = resource.getPathVariableManager().convertToUserEditableFormat(newResourceLocation.toOSString(), true);
		locationValue.setText(userEditableFormat);

		URI resolvedURI = resource.getPathVariableManager()
				.resolveURI(URIUtil.toURI(newResourceLocation));
		IPath resolved = URIUtil.toPath(resolvedURI);
		if (!IDEResourceInfoUtils.exists(resolved.toOSString())) {
			resolvedLocationValue
					.setText(IDEWorkbenchMessages.ResourceInfo_undefinedPathVariable);
			if (sizeValue != null)
				sizeValue.setText(IDEWorkbenchMessages.ResourceInfo_notExist);
		} else {
			resolvedLocationValue.setText(resolved.toPortableString());
			if (sizeValue != null) {
				IFileInfo info = IDEResourceInfoUtils.getFileInfo(resolved
						.toPortableString());
				if (info != null)
					sizeValue.setText(NLS.bind(
							IDEWorkbenchMessages.ResourceInfo_bytes, Long
									.toString(info.getLength())));
				else
					sizeValue
							.setText(IDEWorkbenchMessages.ResourceInfo_unknown);
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IIDEHelpContextIds.RESOURCE_INFO_PROPERTY_PAGE);

		// layout the page
		IResource resource = Adapters.adapt(getElement(), IResource.class);

		if (resource == null) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(IDEWorkbenchMessages.ResourceInfoPage_noResource);
			return label;
		}

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

		createBasicInfoGroup(composite, resource);
		// Attributes are not relevant to projects
		if (resource.getType() != IResource.PROJECT) {
			createSeparator(composite);
			int fsAttributes = getFileSystemAttributes(resource);
			if (isPermissionsSupport(fsAttributes))
				previousPermissionsValue = fetchPermissions(resource);
			createStateGroup(composite, resource, fsAttributes);
			if (isPermissionsSupport(fsAttributes)) {
				createSeparator(composite);
				createPermissionsGroup(composite);
				setPermissionsSelection(previousPermissionsValue);
			}
		}
		// We can't save and load the preferences for closed project
		if (resource.getProject().isOpen()) {
			encodingEditor = new ResourceEncodingFieldEditor(
					getFieldEditorLabel(resource), composite, resource);
			encodingEditor.setPage(this);
			encodingEditor.load();

			encodingEditor.setPropertyChangeListener(event -> {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) {
					setValid(encodingEditor.isValid());
				}
			});

			if (resource.getType() == IResource.PROJECT) {
				lineDelimiterEditor = new LineDelimiterEditor(composite, resource
						.getProject());
				lineDelimiterEditor.doLoad();
			}
		}

		Dialog.applyDialogFont(composite);

		return composite;
	}

	private int fetchPermissions(IResource resource) {
		IFileStore store = null;
		try {
			store = EFS.getStore(resource.getLocationURI());
		} catch (CoreException e) {
			return 0;
		}
		IFileInfo info = store.fetchInfo();
		int permissions = 0;
		if (info.exists()) {
			permissions |= info.getAttribute(EFS.ATTRIBUTE_OWNER_READ) ? EFS.ATTRIBUTE_OWNER_READ
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE) ? EFS.ATTRIBUTE_OWNER_WRITE
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE) ? EFS.ATTRIBUTE_OWNER_EXECUTE
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_GROUP_READ) ? EFS.ATTRIBUTE_GROUP_READ
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE) ? EFS.ATTRIBUTE_GROUP_WRITE
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE) ? EFS.ATTRIBUTE_GROUP_EXECUTE
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_OTHER_READ) ? EFS.ATTRIBUTE_OTHER_READ
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE) ? EFS.ATTRIBUTE_OTHER_WRITE
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE) ? EFS.ATTRIBUTE_OTHER_EXECUTE
					: 0;
			permissions |= info.getAttribute(EFS.ATTRIBUTE_IMMUTABLE) ? EFS.ATTRIBUTE_IMMUTABLE
					: 0;
		}
		return permissions;
	}

	private int getDefaulPermissions(boolean folder) {
		int permissions = EFS.ATTRIBUTE_OWNER_READ | EFS.ATTRIBUTE_OWNER_WRITE
				| EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE
				| EFS.ATTRIBUTE_OTHER_READ;
		if (folder)
			permissions |= EFS.ATTRIBUTE_OWNER_EXECUTE
					| EFS.ATTRIBUTE_GROUP_EXECUTE | EFS.ATTRIBUTE_OTHER_EXECUTE;
		return permissions;
	}

	private void setPermissionsSelection(int permissions) {
		permissionBoxes[0].setSelection((permissions & EFS.ATTRIBUTE_OWNER_READ) != 0);
		permissionBoxes[1].setSelection((permissions & EFS.ATTRIBUTE_OWNER_WRITE) != 0);
		permissionBoxes[2].setSelection((permissions & EFS.ATTRIBUTE_OWNER_EXECUTE) != 0);
		permissionBoxes[3].setSelection((permissions & EFS.ATTRIBUTE_GROUP_READ) != 0);
		permissionBoxes[4].setSelection((permissions & EFS.ATTRIBUTE_GROUP_WRITE) != 0);
		permissionBoxes[5].setSelection((permissions & EFS.ATTRIBUTE_GROUP_EXECUTE) != 0);
		permissionBoxes[6].setSelection((permissions & EFS.ATTRIBUTE_OTHER_READ) != 0);
		permissionBoxes[7].setSelection((permissions & EFS.ATTRIBUTE_OTHER_WRITE) != 0);
		permissionBoxes[8].setSelection((permissions & EFS.ATTRIBUTE_OTHER_EXECUTE) != 0);
		if (immutableBox != null)
			immutableBox.setSelection((permissions & EFS.ATTRIBUTE_IMMUTABLE) != 0);
	}

	private int getPermissionsSelection() {
		int permissions = 0;
		permissions |= permissionBoxes[0].getSelection() ? EFS.ATTRIBUTE_OWNER_READ : 0;
		permissions |= permissionBoxes[1].getSelection() ? EFS.ATTRIBUTE_OWNER_WRITE : 0;
		permissions |= permissionBoxes[2].getSelection() ? EFS.ATTRIBUTE_OWNER_EXECUTE : 0;
		permissions |= permissionBoxes[3].getSelection() ? EFS.ATTRIBUTE_GROUP_READ : 0;
		permissions |= permissionBoxes[4].getSelection() ? EFS.ATTRIBUTE_GROUP_WRITE : 0;
		permissions |= permissionBoxes[5].getSelection() ? EFS.ATTRIBUTE_GROUP_EXECUTE : 0;
		permissions |= permissionBoxes[6].getSelection() ? EFS.ATTRIBUTE_OTHER_READ : 0;
		permissions |= permissionBoxes[7].getSelection() ? EFS.ATTRIBUTE_OTHER_WRITE : 0;
		permissions |= permissionBoxes[8].getSelection() ? EFS.ATTRIBUTE_OTHER_EXECUTE : 0;
		if (immutableBox != null)
			permissions |= immutableBox.getSelection() ? EFS.ATTRIBUTE_IMMUTABLE : 0;
		return permissions;
	}

	private boolean putPermissions(IResource resource, int permissions) {
		IFileStore store = null;
		try {
			store = EFS.getStore(resource.getLocationURI());
		} catch (CoreException e) {
			return false;
		}
		IFileInfo fileInfo = store.fetchInfo();
		if (!fileInfo.exists())
			return false;
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_READ, (permissions & EFS.ATTRIBUTE_OWNER_READ) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE, (permissions & EFS.ATTRIBUTE_OWNER_WRITE) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, (permissions & EFS.ATTRIBUTE_OWNER_EXECUTE) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_READ, (permissions & EFS.ATTRIBUTE_GROUP_READ) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, (permissions & EFS.ATTRIBUTE_GROUP_WRITE) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, (permissions & EFS.ATTRIBUTE_GROUP_EXECUTE) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_READ, (permissions & EFS.ATTRIBUTE_OTHER_READ) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, (permissions & EFS.ATTRIBUTE_OTHER_WRITE) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, (permissions & EFS.ATTRIBUTE_OTHER_EXECUTE) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_IMMUTABLE, (permissions & EFS.ATTRIBUTE_IMMUTABLE) != 0);
		try {
			store.putInfo(fileInfo, EFS.SET_ATTRIBUTES, null);
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	/**
	 * Return the label for the encoding field editor for the resource.
	 *
	 * @param resource -
	 *            the resource to edit.
	 * @return String
	 */
	private String getFieldEditorLabel(IResource resource) {
		if (resource instanceof IContainer) {
			return CONTAINER_ENCODING_TITLE;
		}
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
	}

	/**
	 * Create the isLocked button and it's associated label as a child of
	 * parent using the editableValue of the receiver. The Composite will be the
	 * parent of the button.
	 *
	 * @param composite
	 *            the parent of the button
	 */
	private void createImmutableButton(Composite composite) {
		this.immutableBox = new Button(composite, SWT.CHECK | SWT.RIGHT);
		this.immutableBox.setAlignment(SWT.LEFT);
		this.immutableBox.setText(LOCKED);
		this.immutableBox.setSelection((this.previousPermissionsValue & EFS.ATTRIBUTE_IMMUTABLE) != 0);
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
	}

	/**
	 * Create the derived button and it's associated label as a child of parent
	 * using the derived of the receiver. The Composite will be the parent of
	 * the button.
	 *
	 * @param composite
	 *            the parent of the button
	 * @param resource
	 *            the resource the information is being taken from
	 */
	private void createDerivedButton(Composite composite, IResource resource) {

		this.derivedBox = new Button(composite, SWT.CHECK | SWT.RIGHT);
		this.derivedBox.setAlignment(SWT.LEFT);
		if (resource.getParent().isDerived(IResource.CHECK_ANCESTORS))
			this.derivedBox.setText(DERIVED_HAS_DERIVED_ANCESTOR);
		else
			this.derivedBox.setText(DERIVED);
		this.derivedBox.setSelection(this.previousDerivedValue);
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
	private void createStateGroup(Composite parent, IResource resource, int fsAttributes) {
		Font font = parent.getFont();

		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		Label attributesLabel = new Label(composite, SWT.LEFT);
		attributesLabel.setText(IDEWorkbenchMessages.ResourceInfo_attributes);

		if (!resource.isVirtual()) {
			if ((fsAttributes & EFS.ATTRIBUTE_READ_ONLY) != 0
					&& !isPermissionsSupport(fsAttributes))
				createEditableButton(composite);
			if ((fsAttributes & EFS.ATTRIBUTE_EXECUTABLE) != 0
					&& !isPermissionsSupport(fsAttributes))
				createExecutableButton(composite);
			if ((fsAttributes & EFS.ATTRIBUTE_ARCHIVE) != 0)
				createArchiveButton(composite);
			if ((fsAttributes & EFS.ATTRIBUTE_IMMUTABLE) != 0)
				createImmutableButton(composite);
		}
		createDerivedButton(composite, resource);
		// create warning for executable flag
		if (executableBox != null && resource.getType() == IResource.FOLDER)
			createExecutableWarning(composite, font);
	}

	private void createPermissionsGroup(Composite parent) {
		Font font = parent.getFont();

		permissionBoxes = new Button[9];
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Label permissionsLabel = new Label(composite, SWT.NONE);
		permissionsLabel.setText(IDEWorkbenchMessages.ResourceInfo_permissions);

		Table table = new Table(composite, SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		for (int i = 0; i < 4; i++) {
			new TableColumn(table, SWT.NONE).setResizable(false);
		}
		table.getColumn(1).setText(IDEWorkbenchMessages.ResourceInfo_read);
		table.getColumn(2).setText(IDEWorkbenchMessages.ResourceInfo_write);
		table.getColumn(3).setText(IDEWorkbenchMessages.ResourceInfo_execute);

		table.getColumn(3).pack();
		int columnWidth = table.getColumn(3).getWidth();
		table.getColumn(0).setWidth(columnWidth);
		table.getColumn(1).setWidth(columnWidth);
		table.getColumn(2).setWidth(columnWidth);

		TableItem ownerItem = new TableItem(table, SWT.NONE);
		ownerItem.setText(IDEWorkbenchMessages.ResourceInfo_owner);
		permissionBoxes[0] = createPermissionEditor(table, ownerItem, 1);
		permissionBoxes[1] = createPermissionEditor(table, ownerItem, 2);
		permissionBoxes[2] = createPermissionEditor(table, ownerItem, 3);

		TableItem groupItem = new TableItem(table, SWT.NONE);
		groupItem.setText(IDEWorkbenchMessages.ResourceInfo_group);
		permissionBoxes[3] = createPermissionEditor(table, groupItem, 1);
		permissionBoxes[4] = createPermissionEditor(table, groupItem, 2);
		permissionBoxes[5] = createPermissionEditor(table, groupItem, 3);

		TableItem otherItem = new TableItem(table, SWT.NONE);
		otherItem.setText(IDEWorkbenchMessages.ResourceInfo_other);
		permissionBoxes[6] = createPermissionEditor(table, otherItem, 1);
		permissionBoxes[7] = createPermissionEditor(table, otherItem, 2);
		permissionBoxes[8] = createPermissionEditor(table, otherItem, 3);

		GridData tableData = new GridData();
		tableData.heightHint = table.getHeaderHeight() + 3 * table.getItemHeight();
		table.setLayoutData(tableData);
		createExecutableWarning(composite, font);
	}

	private Button createPermissionEditor(Table table, TableItem item, int index) {
		Button button = new Button(table, SWT.CHECK);
		button.pack();
		TableEditor editor = new TableEditor(table);
		editor.grabVertical = true;
		editor.verticalAlignment = SWT.CENTER;
		editor.minimumWidth = button.getSize().x;
		editor.setEditor(button, item, index);
		editor.getEditor();
		return button;
	}

	private Composite createExecutableWarning(Composite composite, Font font) {
		Composite noteComposite = createNoteComposite(font, composite,
				IDEWorkbenchMessages.Preference_note,
				IDEWorkbenchMessages.ResourceInfo_exWarning);
		GridData data = new GridData();
		data.widthHint = convertWidthInCharsToPixels(IDEWorkbenchMessages.ResourceInfo_exWarning.length());
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		noteComposite.setLayoutData(data);
		return noteComposite;
	}

	private int getFileSystemAttributes(IResource resource) {
		URI location = resource.getLocationURI();
		if (location == null || location.getScheme() == null)
			return 0;
		IFileSystem fs;
		try {
			fs = EFS.getFileSystem(location.getScheme());
		} catch (CoreException e) {
			return 0;
		}
		return fs.attributes();
	}

	private boolean isPermissionsSupport(int fsAttributes) {
		int unixPermissions = EFS.ATTRIBUTE_OWNER_READ
				| EFS.ATTRIBUTE_OWNER_WRITE | EFS.ATTRIBUTE_OWNER_EXECUTE
				| EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE
				| EFS.ATTRIBUTE_GROUP_EXECUTE | EFS.ATTRIBUTE_OTHER_READ
				| EFS.ATTRIBUTE_OTHER_WRITE | EFS.ATTRIBUTE_OTHER_EXECUTE;
		if ((fsAttributes & unixPermissions) == unixPermissions)
			return true;
		return false;
	}

	private IContentDescription getContentDescription(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			return null;
		}

		if (cachedContentDescription == null) {
			try {
				cachedContentDescription = ((IFile) resource)
						.getContentDescription();
			} catch (CoreException e) {
				// silently ignore
			}
		}
		return cachedContentDescription;
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {

		IResource resource = Adapters.adapt(getElement(), IResource.class);

		if (resource == null)
			return;

		if (newResourceLocation != null) {
			newResourceLocation = null;

			resolvedLocationValue.setText(IDEResourceInfoUtils
					.getResolvedLocationText(resource));

			String locationStr = TextProcessor.process(IDEResourceInfoUtils
					.getLocationText(resource));
			locationValue.setText(locationStr);

			if (sizeValue != null)
				sizeValue.setText(IDEResourceInfoUtils.getSizeString(resource));
		}

		// Nothing to update if we never made the box
		if (this.editableBox != null) {
			this.editableBox.setSelection(false);
		}

		// Nothing to update if we never made the box
		if (this.executableBox != null) {
			this.executableBox.setSelection(false);
		}

		// Nothing to update if we never made the box
		if (this.archiveBox != null) {
			this.archiveBox.setSelection(true);
		}

		// Nothing to update if we never made the box
		if (this.immutableBox != null) {
			this.immutableBox.setSelection(false);
		}

		// Nothing to update if we never made the box
		if (this.derivedBox != null) {
			this.derivedBox.setSelection(false);
		}

		if (permissionBoxes != null) {
			int defaultPermissionValues = getDefaulPermissions(resource.getType() == IResource.FOLDER);
			setPermissionsSelection(defaultPermissionValues);
		}

		if (encodingEditor != null) {
			encodingEditor.loadDefault();
		}

		if (lineDelimiterEditor != null) {
			lineDelimiterEditor.loadDefault();
		}

	}

	private String getSimpleChangeName(boolean isSet, String name) {
		String message = "\t"; //$NON-NLS-1$
		message += isSet ? IDEWorkbenchMessages.ResourceInfo_recursiveChangesSet
				: IDEWorkbenchMessages.ResourceInfo_recursiveChangesUnset;
		message += " " + name + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		return message;
	}

	private IResourceChange getAttributesChange(final boolean changedAttrs[],
			final boolean finalAttrs[]) {
		return new IResourceChange() {
			@Override
			public String getMessage() {
				String message = ""; //$NON-NLS-1$
				if (changedAttrs[0])
					message += getSimpleChangeName(finalAttrs[0],
							IDEWorkbenchMessages.ResourceInfo_readOnly);
				if (changedAttrs[1])
					message += getSimpleChangeName(finalAttrs[1],
							IDEWorkbenchMessages.ResourceInfo_executable);
				if (changedAttrs[2])
					message += getSimpleChangeName(finalAttrs[2],
							IDEWorkbenchMessages.ResourceInfo_archive);
				return message;
			}

			@Override
			public void performChange(IResource resource) throws CoreException {
				ResourceAttributes attrs = resource.getResourceAttributes();
				if (attrs != null) {
					if (changedAttrs[0])
						attrs.setReadOnly(finalAttrs[0]);
					if (changedAttrs[1])
						attrs.setExecutable(finalAttrs[1]);
					if (changedAttrs[2])
						attrs.setArchive(finalAttrs[2]);
					resource.setResourceAttributes(attrs);
				}
			}
		};
	}

	private IResourceChange getPermissionsChange(final int changedPermissions,
			final int finalPermissions) {
		return new IResourceChange() {
			@Override
			public String getMessage() {
				// iterated with [j][i]
				int permissionMasks[][] = new int[][] {
						{ EFS.ATTRIBUTE_OWNER_READ, EFS.ATTRIBUTE_OWNER_WRITE,
								EFS.ATTRIBUTE_OWNER_EXECUTE },
						{ EFS.ATTRIBUTE_GROUP_READ, EFS.ATTRIBUTE_GROUP_WRITE,
								EFS.ATTRIBUTE_GROUP_EXECUTE },
						{ EFS.ATTRIBUTE_OTHER_READ, EFS.ATTRIBUTE_OTHER_WRITE,
								EFS.ATTRIBUTE_OTHER_EXECUTE } };
				// iterated with [j]
				String groupNames[] = new String[] {
						IDEWorkbenchMessages.ResourceInfo_owner,
						IDEWorkbenchMessages.ResourceInfo_group,
						IDEWorkbenchMessages.ResourceInfo_other };
				// iterated with [i]
				String permissionNames[] = new String[] {
						IDEWorkbenchMessages.ResourceInfo_read,
						IDEWorkbenchMessages.ResourceInfo_write,
						IDEWorkbenchMessages.ResourceInfo_execute };

				StringBuilder message = new StringBuilder(""); //$NON-NLS-1$
				if ((changedPermissions & EFS.ATTRIBUTE_IMMUTABLE) != 0)
					message.append(getSimpleChangeName(
							(finalPermissions & EFS.ATTRIBUTE_IMMUTABLE) != 0,
							IDEWorkbenchMessages.ResourceInfo_locked));

				for (int j = 0; j < 3; j++) {
					for (int i = 0; i < 3; i++) {
						if ((changedPermissions & permissionMasks[j][i]) != 0)
							message.append(getSimpleChangeName(
									(finalPermissions & permissionMasks[j][i]) != 0,
									groupNames[j] + " " + permissionNames[i])); //$NON-NLS-1$
					}
				}
				return message.toString();
			}

			@Override
			public void performChange(IResource resource) {
				int permissions = fetchPermissions(resource);
				// add permissions
				permissions |= changedPermissions & finalPermissions;
				// remove permissions
				permissions &= ~changedPermissions | finalPermissions;
				putPermissions(resource, permissions);
			}
		};
	}

	private List<IResource> getResourcesToVisit(IResource resource) throws CoreException {
		// use set for fast lookup
		final Set<URI> visited = new HashSet<>();
		// use list to preserve the order of visited resources
		final List<IResource> toVisit = new ArrayList<>();
		visited.add(resource.getLocationURI());
		resource.accept(proxy -> {
			IResource childResource = proxy.requestResource();
			URI uri = childResource.getLocationURI();
			if (!visited.contains(uri)) {
				visited.add(uri);
				toVisit.add(childResource);
			}
			return true;
		}, IResource.NONE);
		return toVisit;
	}

	private boolean shouldPerformRecursiveChanges(List<IResourceChange> changes) {
		if (!changes.isEmpty()) {
			StringBuilder message = new StringBuilder(IDEWorkbenchMessages.ResourceInfo_recursiveChangesSummary)
					.append('\n');
			for (Object change : changes) {
				message.append(((IResourceChange) change).getMessage());
			}
			message.append(IDEWorkbenchMessages.ResourceInfo_recursiveChangesQuestion);

			MessageDialog dialog = new MessageDialog(getShell(),
					IDEWorkbenchMessages.ResourceInfo_recursiveChangesTitle,
					null, message.toString(), MessageDialog.QUESTION, 1,
					IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL);

			return dialog.open() == 0;
		}
		return false;
	}

	private void scheduleRecursiveChangesJob(final IResource resource, final List<IResourceChange> changes) {
		Job.create(IDEWorkbenchMessages.ResourceInfo_recursiveChangesJobName, monitor -> {
			try {
				List<IResource> toVisit = getResourcesToVisit(resource);

				// Prepare the monitor for the given amount of work
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						IDEWorkbenchMessages.ResourceInfo_recursiveChangesJobName,
						toVisit.size());

				// Apply changes recursively
				for (Iterator<IResource> it = toVisit.iterator(); it.hasNext();) {
					SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(changes.size());
					IResource childResource = it.next();
					iterationMonitor.subTask(NLS
							.bind(IDEWorkbenchMessages.ResourceInfo_recursiveChangesSubTaskName,
							childResource.getFullPath()));
					for (Object change : changes) {
						iterationMonitor.split(1);
						((IResourceChange) change)
								.performChange(childResource);
					}
				}
			} catch (CoreException e1) {
				IDEWorkbenchPlugin
						.log(IDEWorkbenchMessages.ResourceInfo_recursiveChangesError,
								e1.getStatus());
				return e1.getStatus();
			} catch (OperationCanceledException e2) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}).schedule();
	}

	/**
	 * Apply the read only state and the encoding to the resource.
	 */
	@Override
	public boolean performOk() {

		IResource resource = Adapters.adapt(getElement(), IResource.class);

		if (resource == null)
			return true;

		if (lineDelimiterEditor != null) {
			lineDelimiterEditor.store();
		}

		try {
			if (newResourceLocation != null) {
				if (resource.getType() == IResource.FILE)
					((IFile)resource).createLink(newResourceLocation, IResource.REPLACE,
							new NullProgressMonitor());
				if (resource.getType() == IResource.FOLDER)
					((IFolder)resource).createLink(newResourceLocation, IResource.REPLACE,
							new NullProgressMonitor());
			}

			List<IResourceChange> changes = new ArrayList<>();

			ResourceAttributes attrs = resource.getResourceAttributes();
			if (attrs != null) {
				boolean finalValues[] = new boolean[] { false, false, false };
				boolean changedAttrs[] = new boolean[] { false, false, false };
				// Nothing to update if we never made the box
				if (editableBox != null
						&& editableBox.getSelection() != previousReadOnlyValue) {
					attrs.setReadOnly(editableBox.getSelection());
					finalValues[0] = editableBox.getSelection();
					changedAttrs[0] = true;
				}
				if (executableBox != null
						&& executableBox.getSelection() != previousExecutableValue) {
					attrs.setExecutable(executableBox.getSelection());
					finalValues[1] = executableBox.getSelection();
					changedAttrs[1] = true;
				}
				if (archiveBox != null
						&& archiveBox.getSelection() != previousArchiveValue) {
					attrs.setArchive(archiveBox.getSelection());
					finalValues[2] = archiveBox.getSelection();
					changedAttrs[2] = true;
				}
				if (changedAttrs[0] || changedAttrs[1] || changedAttrs[2]) {
					resource.setResourceAttributes(attrs);
					attrs = resource.getResourceAttributes();
					if (attrs != null) {
						previousReadOnlyValue = attrs.isReadOnly();
						previousExecutableValue = attrs.isExecutable();
						previousArchiveValue = attrs.isArchive();
						if (editableBox != null) {
							editableBox.setSelection(attrs.isReadOnly());
						}
						if (executableBox != null) {
							executableBox.setSelection(attrs.isExecutable());
						}
						if (archiveBox != null) {
							archiveBox.setSelection(attrs.isArchive());
						}
						if (resource.getType() == IResource.FOLDER) {
							changes.add(getAttributesChange(changedAttrs,
									finalValues));
						}
					}
				}
			}

			if (permissionBoxes != null) {
				int permissionValues = getPermissionsSelection();
				if (previousPermissionsValue != permissionValues) {
					int changedPermissions = previousPermissionsValue ^ permissionValues;
					putPermissions(resource, permissionValues);
					previousPermissionsValue = fetchPermissions(resource);
					if (previousPermissionsValue != permissionValues) {
						// We failed to set some of the permissions
						setPermissionsSelection(previousPermissionsValue);
					}
					if (resource.getType() == IResource.FOLDER) {
						changes.add(getPermissionsChange(changedPermissions,
								permissionValues));
					}
				}
			}

			if (shouldPerformRecursiveChanges(changes))
				scheduleRecursiveChangesJob(resource, changes);

			// Nothing to update if we never made the box
			if (this.derivedBox != null) {
				boolean localDerivedValue = derivedBox.getSelection();
				if (previousDerivedValue != localDerivedValue) {
					resource.setDerived(localDerivedValue, null);
					boolean isDerived = resource.isDerived();
					previousDerivedValue = isDerived;
					derivedBox.setSelection(isDerived);
				}
			}
		} catch (CoreException exception) {
			ErrorDialog.openError(getShell(),
					IDEWorkbenchMessages.InternalError, exception
							.getLocalizedMessage(), exception.getStatus());
			return false;
		} finally {
			// This must be invoked after the 'derived' property has been set,
			// because it may influence the place where encoding is stored.
			if (encodingEditor != null) {
				encodingEditor.store();
			}
		}
		return true;
	}
}
