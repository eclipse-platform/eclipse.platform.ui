package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolMigration;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Property page to add external tools in between builders.
 */
public final class BuilderPropertyPage extends PropertyPage {
	private static final int BUILDER_TABLE_WIDTH = 250;
	private static final String NEW_NAME = "BuilderPropertyPageName"; //$NON-NLS-1$
	private static final String IMG_BUILDER = "icons/full/obj16/builder.gif"; //$NON-NLS-1$;
	private static final String IMG_INVALID_BUILD_TOOL = "icons/full/obj16/invalid_build_tool.gif"; //$NON-NLS-1$

	private static final String LAUNCH_CONFIG_HANDLE = "LaunchConfigHandle"; //$NON-NLS-1$
	
	private static final String TAG_CONFIGURATION_MAP= "configurationMap"; //$NON-NLS-1$
	private static final String TAG_SOURCE_TYPE= "sourceType"; //$NON-NLS-1$
	private static final String TAG_BUILDER_TYPE= "builderType"; //$NON-NLS-1$

	private Table builderTable;
	private Button upButton, downButton, newButton, copyButton, editButton, removeButton;
	private ArrayList imagesToDispose = new ArrayList();
	private Image builderImage, invalidBuildToolImage;
	private IDebugModelPresentation debugModelPresentation;
	
	private boolean userHasMadeChanges= false;
	private SelectionListener fButtonListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
			}
		};

	/**
	 * Creates an initialized property page
	 */
	public BuilderPropertyPage() {
		super();
		noDefaultAndApplyButton();
	}

	/**
	 * Add the project's build to the table viewer.
	 */
	private void addBuildersToTable() {
		IProject project = getInputProject();
		if (project == null) {
			return;
		}
		//add build spec entries to the table
		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			for (int i = 0; i < commands.length; i++) {
				//ExternalTool tool = ExternalToolRegistry.toolFromBuildCommandArgs(commands[i].getArguments(), NEW_NAME);
				ILaunchConfiguration config = ExternalToolsUtil.configFromBuildCommandArgs(commands[i].getArguments());
				if (config != null) {
					addConfig(config, false);
				} else {
					addCommand(commands[i], -1, false);
				}
			}
		} catch (CoreException e) {
			handleException(e);
		}
	}

	/**
	 * Adds a build command to the table viewer.
	 * 
	 * @param command the command to be added
	 * @param position the insertion position, or -1 to add at the end
	 * @param select whether to select the newly created item.
	 */
	private void addCommand(ICommand command, int position, boolean select) {
		TableItem newItem;
		if (position < 0) {
			newItem = new TableItem(builderTable, SWT.NONE);
		} else {
			newItem = new TableItem(builderTable, SWT.NONE, position);
		}
		newItem.setData(command);
		updateCommandItem(newItem, command);
		if (select)
			builderTable.setSelection(position);
	}

	/**
	 * Adds the given launch configuration to the table and selects it if
	 * <code>select</code> is <code>true</code>.
	 */
	private void addConfig(ILaunchConfiguration config, boolean select) {
		TableItem newItem = new TableItem(builderTable, SWT.NONE);
		newItem.setData(config);
		updateConfigItem(newItem, config);
		if (select) {
			builderTable.setSelection(builderTable.getItemCount());
		}
	}

	private void updateConfigItem(TableItem item, ILaunchConfiguration config) {
		item.setText(config.getName());
		Image configImage = debugModelPresentation.getImage(config);
		if (configImage == null) {
			configImage= builderImage;
		}
		item.setImage(configImage);
	}
	
	/**
	 * Converts the given config to a build command which is stored in the
	 * given command.
	 *
	 * @return the configured build command
	 */
	public ICommand toBuildCommand(ILaunchConfiguration config, ICommand command) throws CoreException {
		Map args= null;
		if (config instanceof ILaunchConfigurationWorkingCopy) {
			if (((ILaunchConfigurationWorkingCopy) config).getLocation() == null) {
				// This config represents an old external tool builder that hasn't
				// been edited. Try to find the old ICommand and reuse the arguments.
				// The goal here is to not change the storage format of old, unedited builders.
				ICommand[] commands= getInputProject().getDescription().getBuildSpec();
				for (int i = 0; i < commands.length; i++) {
					ICommand projectCommand = commands[i];
					String name= ExternalToolMigration.getNameFromCommandArgs(projectCommand.getArguments());
					if (name != null && name.equals(config.getName())) {
						args= projectCommand.getArguments();
						break;
					}
				}
			}
		} 
		if (args == null) {
			// Launch configuration builders are stored by storing their handle
			args= new HashMap();
			args.put(LAUNCH_CONFIG_HANDLE, config.getMemento());
		}
		command.setBuilderName(ExternalToolBuilder.ID);
		command.setArguments(args);
		return command;
	}

	/**
	 * Creates and returns a button with the given label, id, and enablement.
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		button.setLayoutData(data);
		button.setFont(parent.getFont());
		button.setText(label);
		button.setEnabled(false);
		button.addSelectionListener(fButtonListener);
		return button;
	}

	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();
		
		debugModelPresentation = DebugUITools.newDebugModelPresentation();
		builderImage = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_BUILDER).createImage();
		invalidBuildToolImage = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_INVALID_BUILD_TOOL).createImage();

		imagesToDispose.add(builderImage);
		imagesToDispose.add(invalidBuildToolImage);

		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		topLevel.setLayout(layout);
		topLevel.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label description = new Label(topLevel, SWT.WRAP);
		description.setText(ToolMessages.getString("BuilderPropertyPage.description")); //$NON-NLS-1$
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		description.setFont(font);

		Composite tableAndButtons = new Composite(topLevel, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		tableAndButtons.setLayout(layout);

		// table of builders and tools		
		builderTable = new Table(tableAndButtons, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = BUILDER_TABLE_WIDTH;
		builderTable.setLayoutData(data);
		builderTable.setFont(font);
		builderTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});

		//button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setFont(font);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		newButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.newButton")); //$NON-NLS-1$
		copyButton = createButton(buttonArea, "Copy...");
		editButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.editButton")); //$NON-NLS-1$
		removeButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.removeButton")); //$NON-NLS-1$
		new Label(buttonArea, SWT.LEFT);
		upButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.upButton")); //$NON-NLS-1$
		downButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.downButton")); //$NON-NLS-1$

		newButton.setEnabled(true);
		copyButton.setEnabled(true);

		//populate widget contents	
		addBuildersToTable();

		return topLevel;
	}

	/* (non-Javadoc)
	 * Method declared on DialogPage.
	 */
	public void dispose() {
		super.dispose();
		for (Iterator i = imagesToDispose.iterator(); i.hasNext();) {
			Image image = (Image) i.next();
			image.dispose();
		}
		imagesToDispose.clear();
	}

	/**
	 * Returns the project that is the input for this property page,
	 * or <code>null</code>.
	 */
	private IProject getInputProject() {
		IAdaptable element = getElement();
		if (element instanceof IProject) {
			return (IProject) element;
		}
		Object resource = element.getAdapter(IResource.class);
		if (resource instanceof IProject) {
			return (IProject) resource;
		}
		return null;
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 */
	private void handleButtonPressed(Button button) {
		if (button == newButton) {
			handleNewButtonPressed();
		} else if (button == copyButton) {
			handleCopyButtonPressed();
		} else if (button == editButton) {
			handleEditButtonPressed();
		} else if (button == removeButton) {
			handleRemoveButtonPressed();
		} else if (button == upButton) {
			moveSelectionUp();
		} else if (button == downButton) {
			moveSelectionDown();
		}
		handleTableSelectionChanged();
		builderTable.setFocus();
	}

	/**
	 * The user has pressed the copy button. Prompt them to select a
	 * configuration to copy.
	 */	
	private void handleCopyButtonPressed() {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType types[]= manager.getLaunchConfigurationTypes();
		List toolTypes= new ArrayList(); 
		for (int i = 0; i < types.length; i++) {
			ILaunchConfigurationType type = types[i];
			if (IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_CATEGORY.equals(type.getCategory())) {
				toolTypes.add(type);
			}
		}
		List configurations= new ArrayList();
		Iterator iter= toolTypes.iterator();
		while (iter.hasNext()) {
			try {
				ILaunchConfiguration[] configs= manager.getLaunchConfigurations((ILaunchConfigurationType) iter.next());
				for (int i = 0; i < configs.length; i++) {
					configurations.add(configs[i]);	
				}
			} catch (CoreException e) {
			}
		}
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), debugModelPresentation);
		dialog.setTitle("Copy configuration");
		dialog.setMessage("Choose a configuration to copy");
		dialog.setElements(configurations.toArray());
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}
		Object results[]= dialog.getResult();
		ILaunchConfiguration config= (ILaunchConfiguration) results[0];
		Map attributes= null;
		try {
			attributes= config.getAttributes();
		} catch (CoreException e) {
			handleException(e);
			return;
		}
		String newName= config.getName() + " [Builder]";
		newName= manager.generateUniqueLaunchConfigurationNameFrom(newName);
		ILaunchConfiguration newConfig= null;
		try {
			ILaunchConfigurationType newType= getConfigurationDuplicationType(config);
			ILaunchConfigurationWorkingCopy newWorkingCopy= newType.newInstance(getBuilderFolder(), newName);
			newWorkingCopy.setAttributes(attributes);
			newConfig= newWorkingCopy.doSave();
		} catch (CoreException e) {
			handleException(e);
			return;
		}
		userHasMadeChanges= true;
		addConfig(newConfig, true);
	}
	
	/**
	 * Returns the type of launch configuration that should be created when
	 * duplicating the given configuration as a project builder. Queries to see
	 * if an extension has been specified to explicitly declare the mapping.
	 */
	private ILaunchConfigurationType getConfigurationDuplicationType(ILaunchConfiguration config) throws CoreException {
		IExtensionPoint ep= ExternalToolsPlugin.getDefault().getDescriptor().getExtensionPoint(IExternalToolConstants.EXTENSION_POINT_CONFIGURATION_DUPLICATION_MAPS); 
		IConfigurationElement[] elements = ep.getConfigurationElements();
		String sourceType= config.getType().getIdentifier();
		String builderType= null;
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (element.getName().equals(TAG_CONFIGURATION_MAP) && sourceType.equals(element.getAttribute(TAG_SOURCE_TYPE))) {
				builderType= element.getAttribute(TAG_BUILDER_TYPE);
				break;
			}
		}
		if (builderType != null) {
			ILaunchConfigurationType type= DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(builderType);
			if (type != null) {
				return type;
			}
		}
		return config.getType();
	}

	/**
	 * The user has pressed the remove button. Delete the selected builder.
	 */
	private void handleRemoveButtonPressed() {
		TableItem[] selection = builderTable.getSelection();
		if (selection != null) {
			userHasMadeChanges= true;
			for (int i = 0; i < selection.length; i++) {
				Object data= selection[i].getData();
				if (data instanceof ILaunchConfiguration) {
					try {
						((ILaunchConfiguration) data).delete();
					} catch (CoreException e) {
						handleException(e);
					}
				}
				selection[i].dispose();
			}
		}
	}
	
	/**
	 * The user has pressed the new button. Create a new configuration and open
	 * the launch configuration edit dialog on the new config.
	 */
	private void handleNewButtonPressed() {
		ILaunchConfigurationType type = promptForConfigurationType();
		if (type == null) {
			return;
		}
		try {
			ILaunchConfigurationWorkingCopy workingCopy = null;
			String name= DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom("New_Builder");
			workingCopy = type.newInstance(getBuilderFolder(), name);		
			workingCopy.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_NONE);
			ILaunchConfiguration config = null;
			config = workingCopy.doSave();
			int code= DebugUITools.openLaunchConfigurationPropertiesDialog(getShell(), config, IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP);
			if (code == Dialog.CANCEL) {
				// If the user cancelled, delete the newly created config
				config.delete();
			} else {
				userHasMadeChanges= true;
				addConfig(config, true);
			}
		} catch (CoreException e) {
		}
	}
	
	private ILaunchConfigurationType promptForConfigurationType() {
		ILaunchConfigurationType types[] = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		String category = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP).getCategory();
		List externalToolsTypes = new ArrayList();
		for (int i = 0; i < types.length; i++) {
			ILaunchConfigurationType configurationType = types[i];
			if (category.equals(configurationType.getCategory())) {
				externalToolsTypes.add(configurationType);
			}
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), debugModelPresentation);
		dialog.setElements(externalToolsTypes.toArray());
		dialog.setMultipleSelection(false);
		dialog.setTitle("Choose configuration type");
		dialog.setMessage("Choose an external tool type to create");
		dialog.open();
		Object result[] = dialog.getResult();
		if (result == null || result.length == 0) {
			return null;
		}
		return (ILaunchConfigurationType) result[0];
	}
	
	/**
	 * The user has pressed the edit button. Open the launch configuration edit
	 * dialog on the selection after migrating the tool if necessary.
	 */
	private void handleEditButtonPressed() {
		TableItem selection = builderTable.getSelection()[0];
		if (selection != null) {
			Object data = selection.getData();
			if (data instanceof ILaunchConfiguration) {
				ILaunchConfiguration config= (ILaunchConfiguration) data;
				if (data instanceof ILaunchConfigurationWorkingCopy) {
					if (!shouldProceedWithMigration()) {
						return;
					}
					try {
						config= migrateBuilderConfiguration((ILaunchConfigurationWorkingCopy) data);
					} catch (CoreException e) {
						handleException(e);
						return;
					}
					// Replace the working copy in the table with the migrated configuration
					selection.setData(config);
				}
				userHasMadeChanges= true;
				DebugUITools.openLaunchConfigurationPropertiesDialog(getShell(), config, IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP);
				updateConfigItem(selection, (ILaunchConfiguration) data);
			}
		}
	}
	
	/**
	 * Migrates the launch configuration working copy, which is based on an old-
	 * style external tool builder, to a new, saved launch configuration. The
	 * returned launch configuration will contain the same attributes as the
	 * given working copy with the exception of the configuration name, which
	 * may be changed during the migration. The name of the configuration will
	 * only be changed if the current name is not a valid name for a saved
	 * config.
	 * 
	 * @param workingCopy the launch configuration containing attributes from an
	 * old-style project builder.
	 * @return ILaunchConfiguration a new, saved launch configuration whose
	 * attributes match those of the given working copy as well as possible
	 * @throws CoreException if an exception occurs while attempting to save the
	 * new launch configuration
	 */
	private ILaunchConfiguration migrateBuilderConfiguration(ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		workingCopy.setContainer(getBuilderFolder());
		// Before saving, make sure the name is valid
		String name= workingCopy.getName();
		name.replace('/', '.');
		if (name.charAt(0) == ('.')) {
			name = name.substring(1);
		}
		IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
		if (!status.isOK()) {
			name = "ExternalTool"; //$NON-NLS-1$
		}
		name = DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
		workingCopy.rename(name);
		return workingCopy.doSave();
	}
	
	/**
	 * Prompts the user to proceed with the migration of a project builder from
	 * the old format to the new, launch configuration-based, format and returns
	 * whether or not the user wishes to proceed with the migration.
	 * 
	 * @return boolean whether or not the user wishes to proceed with migration
	 */
	private boolean shouldProceedWithMigration() {
		if (!ExternalToolsPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.PROMPT_FOR_MIGRATION)) {
			// User has asked not to be prompted
			return true;
		}
		// Warn the user that editing an old config will cause storage migration.
		return MessageDialogWithToggle.openQuestion(getShell(), "Migrate project builder",
			"This project builder is stored in a format that is no longer supported. If you wish to edit this builder, it will first be migrated to the new format. If you proceed, this project builder will not be understood by installations running versions 2.0 or earlier of the org.eclipse.ui.externaltools plugin.\n\nProceed with migration?",
			IPreferenceConstants.PROMPT_FOR_MIGRATION,
			"&Always prompt before migrating project builders",
			ExternalToolsPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Returns the folder where project builders should be stored or
	 * <code>null</code> if the folder could not be created
	 */
	private IFolder getBuilderFolder() {
		IFolder folder = getInputProject().getFolder(".externalToolBuilders");
		if (!folder.exists()) {
			try {
				folder.create(true, true, new NullProgressMonitor());
			} catch (CoreException e) {
				return null;
			}
		}
		return folder;
	}

	/**
	 * Handles unexpected internal exceptions
	 */
	private void handleException(Exception e) {
		IStatus status;
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("BuilderPropertyPage.statusMessage"), e); //$NON-NLS-1$
		}
		ErrorDialog.openError(getShell(), ToolMessages.getString("BuilderPropertyPage.errorTitle"), //$NON-NLS-1$
				ToolMessages.getString("BuilderPropertyPage.errorMessage"), //$NON-NLS-1$
				status);
	}

	/**
	 * The user has selected a different builder in table.
	 * Update button enablement.
	 */
	private void handleTableSelectionChanged() {
		newButton.setEnabled(true);
		TableItem[] items = builderTable.getSelection();
		if (items != null && items.length == 1) {
			TableItem item = items[0];
			Object data = item.getData();
			if (data instanceof ILaunchConfiguration) {
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
				int selection = builderTable.getSelectionIndex();
				int max = builderTable.getItemCount();
				upButton.setEnabled(selection != 0);
				downButton.setEnabled(selection < max - 1);
				return;
			}
		}
		//in all other cases we can't do any of these.
		editButton.setEnabled(false);
		removeButton.setEnabled(false);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
	}

	/**
	 * Moves an entry in the builder table to the given index.
	 */
	private void move(TableItem item, int index) {
		userHasMadeChanges= true;
		Object data = item.getData();
		String text = item.getText();
		Image image = item.getImage();
		item.dispose();
		TableItem newItem = new TableItem(builderTable, SWT.NONE, index);
		newItem.setData(data);
		newItem.setText(text);
		newItem.setImage(image);
	}

	/**
	 * Move the current selection in the build list down.
	 */
	private void moveSelectionDown() {
		// Only do this operation on a single selection
		if (builderTable.getSelectionCount() == 1) {
			int currentIndex = builderTable.getSelectionIndex();
			if (currentIndex < builderTable.getItemCount() - 1) {
				move(builderTable.getItem(currentIndex), currentIndex + 1);
				builderTable.setSelection(currentIndex + 1);
			}
		}
	}

	/**
	 * Move the current selection in the build list up.
	 */
	private void moveSelectionUp() {
		int currentIndex = builderTable.getSelectionIndex();
		// Only do this operation on a single selection
		if (currentIndex > 0 && builderTable.getSelectionCount() == 1) {
			move(builderTable.getItem(currentIndex), currentIndex - 1);
			builderTable.setSelection(currentIndex - 1);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IPreferencePage.
	 */
	public boolean performOk() {
		if (!userHasMadeChanges) {
			return super.performOk();
		}
		userHasMadeChanges= false;
		IProject project = getInputProject();
		//get all the build commands
		int numCommands = builderTable.getItemCount();
		ICommand[] commands = new ICommand[numCommands];
		for (int i = 0; i < numCommands; i++) {
			Object data = builderTable.getItem(i).getData();
			if (data instanceof ICommand) {
			} else if (data instanceof ILaunchConfiguration) {
				// Translate launch configs to ICommands for storage
				ICommand newCommand = null;
				try {
					newCommand = project.getDescription().newCommand();
					data = toBuildCommand(((ILaunchConfiguration) data), newCommand);
				} catch (CoreException exception) {
					MessageDialog.openError(getShell(), "Command error", "An error occurred while saving the project's build commands");
					return true;
				}
			}
			commands[i] = (ICommand) data;
		}
		
		if (checkCommandsForChange(commands)) {
			//set the build spec
			try {
				IProjectDescription desc = project.getDescription();
				desc.setBuildSpec(commands);
				project.setDescription(desc, IResource.FORCE, null);
			} catch (CoreException e) {
				handleException(e);
			}
		}
		return super.performOk();
	}
	
	/**
	 * Returns whether any of the commands have changed.
	 */
	private boolean checkCommandsForChange(ICommand[] newCommands) {
		try {
			ICommand[] oldCommands = getInputProject().getDescription().getBuildSpec();
			if (oldCommands.length != newCommands.length) {
				return true;
			}
			for (int i = 0; i < oldCommands.length; i++) {
				ICommand oldCommand = oldCommands[i];
				ICommand newCommand= newCommands[i];
				String oldName= oldCommand.getBuilderName();
				String newName= newCommand.getBuilderName();
				if (oldName == null && newName != null) {
					return true;
				}
				
				if(oldName != null && !oldName.equals(newName)) {
					return true;
				}
				Map oldArgs= oldCommand.getArguments();
				Map newArgs= newCommand.getArguments();
				if (oldArgs == null && newArgs != null) {
					return true;
				}
				if (oldArgs == null && newArgs == null) {
					continue;
				}
				if(oldArgs.size() != newArgs.size()) {
					return true;
				}
				Iterator keySet= oldArgs.keySet().iterator();
				while (keySet.hasNext()) {
					Object key = keySet.next();
					if (!oldArgs.get(key).equals(newArgs.get(key))) {
						return true;
					}
				}
			}
		} catch (CoreException ce) {
			return true;
		}
		return false;	
	}

	/**
	 * Update the table item with the given build command
	 */
	private void updateCommandItem(TableItem item, ICommand command) {
		String builderID = command.getBuilderName();
		if (builderID.equals(ExternalToolBuilder.ID)) {
			ILaunchConfiguration config = ExternalToolsUtil.configFromBuildCommandArgs(command.getArguments());
			if (config == null) {
				item.setText(ToolMessages.getString("BuilderPropertyPage.invalidBuildTool")); //$NON-NLS-1$
				item.setImage(invalidBuildToolImage);
				return;
			}
			item.setText(config.getName());
			Image configImage = debugModelPresentation.getImage(config);
			if (configImage != null) {
				imagesToDispose.add(configImage);
				item.setImage(configImage);
			} else {
				item.setImage(builderImage);
			}
		} else {
			// Get the human-readable name of the builder
			IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderID);
			String builderName;
			if (extension != null)
				builderName = extension.getLabel();
			else
				builderName = ToolMessages.format("BuilderPropertyPage.missingBuilder", new Object[] { builderID }); //$NON-NLS-1$
			item.setText(builderName);
			item.setImage(builderImage);
		}
	}
}
