/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ui;


import java.text.MessageFormat;
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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
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
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.launchConfigurations.IgnoreWhiteSpaceComparator;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolMigration;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Property page to add external tools in between builders.
 */
public final class BuilderPropertyPage extends PropertyPage {
	private static final int BUILDER_TABLE_WIDTH = 250;

	private static final String IMG_BUILDER = "icons/full/obj16/builder.gif"; //$NON-NLS-1$;
	private static final String IMG_INVALID_BUILD_TOOL = "icons/full/obj16/invalid_build_tool.gif"; //$NON-NLS-1$

	private static final String LAUNCH_CONFIG_HANDLE = "LaunchConfigHandle"; //$NON-NLS-1$

	//locally mark a command's enabled state so it can be processed correctly on performOK
	private static final String COMMAND_ENABLED= "CommandEnabled"; //$NON-NLS-1$
	
	// Extension point constants.
	private static final String TAG_CONFIGURATION_MAP= "configurationMap"; //$NON-NLS-1$
	private static final String TAG_SOURCE_TYPE= "sourceType"; //$NON-NLS-1$
	private static final String TAG_BUILDER_TYPE= "builderType"; //$NON-NLS-1$

	private Table builderTable;
	private Button upButton, downButton, newButton, copyButton, editButton, removeButton, enableButton, disableButton;
	private List imagesToDispose = new ArrayList();
	private Image builderImage, invalidBuildToolImage;
	
	private boolean userHasMadeChanges= false;
	
	private List configsToBeDeleted= null;
	
	private ILabelProvider labelProvider;
	
	private class BuilderPageLabelProvider extends LabelProvider {
		
		private IDebugModelPresentation debugModelPresentation;
		
		public BuilderPageLabelProvider() {
			debugModelPresentation= DebugUITools.newDebugModelPresentation();			
		}

		public Image getImage(Object element) {
			if (element instanceof ILaunchConfiguration) {
				try {
					ILaunchConfiguration config= (ILaunchConfiguration) element;
					String disabledBuilderName= config.getAttribute(IExternalToolConstants.ATTR_DISABLED_BUILDER, (String)null);
					if (disabledBuilderName != null) {
						//really a disabled builder wrapped as a launch configuration
						return builderImage;
					}
				} catch (CoreException e) {
				}
			}
			return debugModelPresentation.getImage(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			StringBuffer buffer= new StringBuffer(debugModelPresentation.getText(element));
			if (element instanceof ILaunchConfiguration) {
				try {
					ILaunchConfiguration config= (ILaunchConfiguration) element;
					String disabledBuilderName= config.getAttribute(IExternalToolConstants.ATTR_DISABLED_BUILDER, (String)null);
					if (disabledBuilderName != null) {
						buffer= new StringBuffer(getBuilderName(disabledBuilderName));
					} 
					if (!ExternalToolsUtil.isBuilderEnabled(config)) {
						buffer.append(ExternalToolsUIMessages.getString("BuilderPropertyPage.38")); //$NON-NLS-1$
					}
				} catch (CoreException e) {
				}
			}
			return buffer.toString();
		}
		
	}
	
	/**
	 * Error configs are objects representing entries pointing to
	 * invalid launch configurations
	 */
	private class ErrorConfig {
	}
	
	/**
	 * Collection of configurations created while the page is open.
	 * Stored here so they can be deleted if the page is cancelled.
	 */
	private List newConfigList= new ArrayList();
	
	private SelectionListener buttonListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};
	
	/**
	 * Launch configuration listener which is responsible for updating items in
	 * the tree when the user renames configurations in the dialog.
	 * 
	 * This is necessary because when we tell the configuration dialog to open
	 * on a launch config and the user renames that config, the old config (the
	 * one in the tree) is made obsolete and a new config is created. This
	 * listener hears when new configurations are created this way and replaces
	 * the old configuration with the new.
	 */
	private ILaunchConfigurationListener configurationListener= new ILaunchConfigurationListener() {
		/**
		 * A launch configuration has been added. If this config has been
		 * movedFrom a configuration in the tree, replace the old config with
		 * the new.
		 */
		public void launchConfigurationAdded(final ILaunchConfiguration configuration) {
			ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
			final ILaunchConfiguration oldConfig= manager.getMovedFrom(configuration);
			if (oldConfig == null) {
				return;
			}
			
			Display.getDefault().asyncExec(new Runnable() {	
				public void run() {
					TableItem[] items= builderTable.getItems();
					for (int i = 0; i < items.length; i++) {
						TableItem item = items[i];
						Object data= item.getData();
						if (data == oldConfig) {
							// Found the movedFrom config in the tree. Replace it with the new config 
							item.setData(configuration);
							updateConfigItem(item, configuration);
							break;
						}
					}
					//Also replace the movedFrom config in the list of newly created configs
					if (newConfigList.remove(oldConfig)) {
						newConfigList.add(configuration);
					}
				}
			});
		}
		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		}
		public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
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
				ILaunchConfiguration config = ExternalToolsUtil.configFromBuildCommandArgs(commands[i].getArguments());
				if (config != null) {
					if (!config.isWorkingCopy() && !config.exists()) {
						IStatus status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, MessageFormat.format(ExternalToolsUIMessages.getString("BuilderPropertyPage.Exists"), new String[]{config.getLocation().toOSString()}), null); 	 //$NON-NLS-1$
						ErrorDialog.openError(getShell(), ExternalToolsUIMessages.getString("BuilderPropertyPage.errorTitle"), //$NON-NLS-1$
										MessageFormat.format(ExternalToolsUIMessages.getString("BuilderPropertyPage.External_Tool_Builder_{0}_Not_Added_2"), new String[]{config.getName()}),  //$NON-NLS-1$
										status);
						userHasMadeChanges= true;
					} else {
						addConfig(config, false);
					}
				} else {
					String builderID = commands[i].getBuilderName();
					if (builderID.equals(ExternalToolBuilder.ID) && commands[i].getArguments().get(LAUNCH_CONFIG_HANDLE) != null) {
						// An invalid external tool entry.
						addErrorConfig(new ErrorConfig(), -1, false);
					} else {
						addCommand(commands[i], -1, false);
					}
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
		if (select) {
			builderTable.setSelection(position);
		}
	}
	
	/**
	 * Adds the given erroneous configuration entry to the table
	 * and selects it if <code>select</code> is <code>true</code>.
	 */
	private void addErrorConfig(ErrorConfig config, int position, boolean select) {
		TableItem newItem;
		if (position < 0) {
			newItem = new TableItem(builderTable, SWT.NONE);
		} else {
			newItem = new TableItem(builderTable, SWT.NONE, position);
		}
		newItem.setData(config);
		newItem.setText(ExternalToolsUIMessages.getString("BuilderPropertyPage.invalidBuildTool")); //$NON-NLS-1$
		newItem.setImage(invalidBuildToolImage);
		if (select) {
			builderTable.setSelection(position);
		}
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
		item.setText(labelProvider.getText(config));
		Image configImage = labelProvider.getImage(config);
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
	private ICommand toBuildCommand(ILaunchConfiguration config, ICommand command) throws CoreException {
		Map args= null;
		if (isUnmigratedConfig(config)) {
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
		} else {
			if (config instanceof ILaunchConfigurationWorkingCopy) {
				ILaunchConfigurationWorkingCopy workingCopy= (ILaunchConfigurationWorkingCopy) config;
				if (workingCopy.getOriginal() != null) {
					config= workingCopy.getOriginal();
				}
			}
			args= new HashMap();
			// Launch configuration builders are stored by storing their handle
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
		button.addSelectionListener(buttonListener);
		return button;
	}

	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
		
		WorkbenchHelp.setHelp(parent, IExternalToolsHelpContextIds.EXTERNAL_TOOLS_BUILDER_PROPERTY_PAGE);
		
		Font font = parent.getFont();
		
		labelProvider = new BuilderPageLabelProvider();
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
		description.setText(ExternalToolsUIMessages.getString("BuilderPropertyPage.description")); //$NON-NLS-1$
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
		builderTable = new Table(tableAndButtons, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = BUILDER_TABLE_WIDTH;
		builderTable.setLayoutData(data);
		builderTable.setFont(font);
		builderTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});
		
		builderTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				handleEditButtonPressed();
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
		newButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.newButton")); //$NON-NLS-1$
		copyButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.&Copy..._3")); //$NON-NLS-1$
		editButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.editButton")); //$NON-NLS-1$
		removeButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.removeButton")); //$NON-NLS-1$
		enableButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.36")); //$NON-NLS-1$
		disableButton= createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.37")); //$NON-NLS-1$
		new Label(buttonArea, SWT.LEFT);
		upButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.upButton")); //$NON-NLS-1$
		downButton = createButton(buttonArea, ExternalToolsUIMessages.getString("BuilderPropertyPage.downButton")); //$NON-NLS-1$

		newButton.setEnabled(true);
		copyButton.setEnabled(true);

		//populate widget contents	
		addBuildersToTable();

		return topLevel;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
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
	 * Turns autobuilding on or off in the workspace.
	 */
	private void setAutobuild(boolean newState) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceDescription wsDescription= workspace.getDescription();
		boolean oldState= wsDescription.isAutoBuilding();
		if (oldState != newState) {
			wsDescription.setAutoBuilding(newState);
			workspace.setDescription(wsDescription);
		}
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
		} else if (button == enableButton) {
			handleToggleEnabledButtonPressed(true);
		} else if (button == disableButton) {
			handleToggleEnabledButtonPressed(false);
		}
		handleTableSelectionChanged();
		builderTable.setFocus();
	}

	/**
	 * A button which toggles the builder enabled state has been 
	 * pressed. The selected builder should be enabled or disabled.
	 */
	private void handleToggleEnabledButtonPressed(boolean enable) {
		TableItem[] selection = builderTable.getSelection();
		TableItem item;
		for (int i = 0; i < selection.length; i++) {
			item= selection[i];
			Object data= item.getData();
			if (data instanceof ILaunchConfiguration) {
				enableLaunchConfiguration(item, (ILaunchConfiguration) data, enable);
			} else if (data instanceof ICommand) {
				enableCommand(item, (ICommand)data, enable);
			}
		}
	}
	
	private void enableLaunchConfiguration(TableItem item, ILaunchConfiguration configuration, boolean enable) {
		ILaunchConfigurationWorkingCopy workingCopy;
		try {
			if (configuration instanceof ILaunchConfigurationWorkingCopy) {
				workingCopy = (ILaunchConfigurationWorkingCopy) configuration;
			} else {
				// Replace the config with a working copy
				workingCopy = configuration.getWorkingCopy();
				item.setData(workingCopy);
			}
			workingCopy.setAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, enable);
		} catch (CoreException e) {
			return;
		}
		userHasMadeChanges= true;
		updateConfigItem(item, workingCopy);
	}
	
	private void enableCommand(TableItem item, ICommand command, boolean enable) {
		Map args= command.getArguments();
		if (args == null) {
			args= new HashMap(1);
		}
		args.put(COMMAND_ENABLED, Boolean.valueOf(enable));
		command.setArguments(args);
	
		updateCommandItem(item, command);
		userHasMadeChanges= true;
	}

	/**
	 * The user has pressed the copy button. Prompt them to select a
	 * configuration to copy.
	 */	
	private void handleCopyButtonPressed() {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		List toolTypes= getConfigurationTypes(IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_CATEGORY);
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
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(ExternalToolsUIMessages.getString("BuilderPropertyPage.Copy_configuration_4")); //$NON-NLS-1$
		dialog.setMessage(ExternalToolsUIMessages.getString("BuilderPropertyPage.&Choose_a_configuration_to_copy__5")); //$NON-NLS-1$
		dialog.setElements(configurations.toArray());
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		Object results[]= dialog.getResult();
		if (results.length == 0) { //OK pressed with nothing selected
			return;
		}
		ILaunchConfiguration config= (ILaunchConfiguration) results[0];
		ILaunchConfiguration newConfig= null;
		boolean wasAutobuilding= ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
		try {
			setAutobuild(false);
			newConfig= duplicateConfiguration(config);
		} catch (CoreException e) {
			handleException(e);
		} finally {
			try {
				setAutobuild(wasAutobuilding);
			} catch (CoreException e) {
				handleException(e);
			}
		}
		if (newConfig != null) {
			userHasMadeChanges= true;
			addConfig(newConfig, true);
			newConfigList.add(newConfig);
		}
	}
	
	/**
	 * Returns a duplicate of the given configuration. The new configuration
	 * will be of the same type as the given configuration or of the duplication
	 * type registered for the given configuration via the extension point
	 * IExternalToolConstants.EXTENSION_POINT_CONFIGURATION_DUPLICATION_MAPS.
	 */
	private ILaunchConfiguration duplicateConfiguration(ILaunchConfiguration config) throws CoreException {
		Map attributes= null;
		attributes= config.getAttributes();
		String newName= config.getName() + ExternalToolsUIMessages.getString("BuilderPropertyPage._[Builder]_6"); //$NON-NLS-1$
		newName= DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(newName);
		ILaunchConfigurationType newType= getConfigurationDuplicationType(config);
		ILaunchConfigurationWorkingCopy newWorkingCopy= newType.newInstance(getBuilderFolder(true), newName);
		newWorkingCopy.setAttributes(attributes);
		return newWorkingCopy.doSave();
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
			if (configsToBeDeleted == null) {
				configsToBeDeleted= new ArrayList(selection.length);
			}
			userHasMadeChanges= true;
			for (int i = 0; i < selection.length; i++) {
				Object data= selection[i].getData();
				if (data instanceof ILaunchConfiguration) {
					configsToBeDeleted.add(data);
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
		boolean wasAutobuilding= ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
		try {
			ILaunchConfigurationWorkingCopy workingCopy = null;
			String name= DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(ExternalToolsUIMessages.getString("BuilderPropertyPage.New_Builder_7")); //$NON-NLS-1$
			workingCopy = type.newInstance(getBuilderFolder(true), name);		
			
			StringBuffer buffer= new StringBuffer(IExternalToolConstants.BUILD_TYPE_FULL);
			buffer.append(',');
			buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
			buffer.append(',');
			workingCopy.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
			
			ILaunchConfiguration config = null;
			setAutobuild(false);
			config = workingCopy.doSave();
			//needs to be added here in case the user hits apply in the edit dialog
			//then we can correctly update the list with the new config.
			newConfigList.add(config);
			int code= editConfiguration(config);
			if (code == Window.CANCEL) {
				// If the user cancelled, delete the newly created config
				newConfigList.remove(config);
				config.delete();
			} else {
				userHasMadeChanges= true;
				//retrieve the last "new" config
				//may have been changed by the user pressing apply in the edit dialog
				addConfig((ILaunchConfiguration)newConfigList.get(newConfigList.size() - 1), true);
			}
		} catch (CoreException e) {
			handleException(e);
		} finally {
			try {
				setAutobuild(wasAutobuilding);
			} catch (CoreException e) {
				handleException(e);
			}
		}
	}
	
	/**
	 * Prompts the user to edit the given launch configuration. Returns the
	 * return code from opening the launch configuration dialog.
	 */
	private int editConfiguration(ILaunchConfiguration config) {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		manager.addLaunchConfigurationListener(configurationListener);
		int code= DebugUITools.openLaunchConfigurationPropertiesDialog(getShell(), config, IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP);
		manager.removeLaunchConfigurationListener(configurationListener);
		return code;
	}
	
	/**
	 * Prompts the user to choose a launch configuration type to create and
	 * returns the type the user selected or <code>null</code> if the user
	 * cancelled.
	 * 
	 * @return the configuration type selected by the user or <code>null</code>
	 * if the user cancelled.
	 */
	private ILaunchConfigurationType promptForConfigurationType() {
		List externalToolTypes= getConfigurationTypes(IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_CATEGORY);

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(externalToolTypes.toArray());
		dialog.setMultipleSelection(false);
		dialog.setTitle(ExternalToolsUIMessages.getString("BuilderPropertyPage.Choose_configuration_type_8")); //$NON-NLS-1$
		dialog.setMessage(ExternalToolsUIMessages.getString("BuilderPropertyPage.Choose_an_external_tool_type_to_create_9")); //$NON-NLS-1$
		dialog.open();
		Object result[] = dialog.getResult();
		if (result == null || result.length == 0) {
			return null;
		}
		return (ILaunchConfigurationType) result[0];
	}
	
	/**
	 * Returns the launch configuration types of the given category
	 */
	private List getConfigurationTypes(String category) {
		ILaunchConfigurationType types[] = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		List externalToolTypes = new ArrayList();
		for (int i = 0; i < types.length; i++) {
			ILaunchConfigurationType configurationType = types[i];
			if (category.equals(configurationType.getCategory())) {
				externalToolTypes.add(configurationType);
			}
		}
		return externalToolTypes;
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
				if (isUnmigratedConfig(config)) {
					if (!shouldProceedWithMigration()) {
						return;
					}
					try {
						config= migrateBuilderConfiguration((ILaunchConfigurationWorkingCopy) config);
					} catch (CoreException e) {
						handleException(e);
						return;
					}
					// Replace the working copy in the table with the migrated configuration
					selection.setData(config);
				}
				userHasMadeChanges= true;
				boolean wasAutobuilding= ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
				try {
					setAutobuild(false);
					editConfiguration(config);
				} catch (CoreException e) {
					handleException(e);
				} finally {
					try {
						setAutobuild(wasAutobuilding);
					} catch (CoreException e) {
						handleException(e);
					}
				}
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
		workingCopy.setContainer(getBuilderFolder(true));
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
		return MessageDialogWithToggle.openQuestion(getShell(), ExternalToolsUIMessages.getString("BuilderPropertyPage.Migrate_project_builder_10"), //$NON-NLS-1$
			ExternalToolsUIMessages.getString("BuilderPropertyPage.Not_Support"), //$NON-NLS-1$
			IPreferenceConstants.PROMPT_FOR_MIGRATION,
			ExternalToolsUIMessages.getString("BuilderPropertyPage.Prompt"), //$NON-NLS-1$
			ExternalToolsPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Returns the folder where project builders should be stored or
	 * <code>null</code> if the folder could not be created
	 */
	private IFolder getBuilderFolder(boolean create) {
		IFolder folder = getInputProject().getFolder(".externalToolBuilders"); //$NON-NLS-1$
		if (!folder.exists() && create) {
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
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, ExternalToolsUIMessages.getString("BuilderPropertyPage.statusMessage"), e); //$NON-NLS-1$
		}
		ErrorDialog.openError(getShell(), ExternalToolsUIMessages.getString("BuilderPropertyPage.errorTitle"), //$NON-NLS-1$
				ExternalToolsUIMessages.getString("BuilderPropertyPage.errorMessage"), //$NON-NLS-1$
				status);
	}

	/**
	 * The user has selected a different builder in table.
	 * Update button enablement.
	 */
	private void handleTableSelectionChanged() {
		newButton.setEnabled(true);
		TableItem[] items = builderTable.getSelection();
		boolean validSelection= items != null && items.length > 0;
		boolean enableEdit= validSelection;
		boolean enableRemove= validSelection;
		boolean enableUp= validSelection;
		boolean enableDown= validSelection;
		boolean enableEnable= validSelection;
		boolean enableDisable= validSelection;
		if (validSelection) {
			if (items.length > 1) {
				enableEdit= false;
			}
			int indices[]= builderTable.getSelectionIndices();
			int max = builderTable.getItemCount();
			enableUp= indices[0] != 0;
			enableDown= indices[indices.length - 1] < max - 1;
			boolean disabledSelected= false; // Any disabled configs selected?
			boolean enabledSelected= false; // Any enabled configs selected?
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				Object data= item.getData();
				if (data instanceof ILaunchConfiguration) {
					ILaunchConfiguration config= (ILaunchConfiguration) data;
					boolean configEnabled= true;
					try {
						configEnabled = ExternalToolsUtil.isBuilderEnabled(config);
					} catch (CoreException e) {
						ExternalToolsPlugin.getDefault().log(e);
					}
					if (configEnabled) {
						enabledSelected= true;
					} else {
						disabledSelected= true;
					}
					if (isUnmigratedConfig(config)) {
						enableEnable= false;
						enableDisable= false;
					}
				} else {
					if (data instanceof ICommand) {
						ICommand command= (ICommand)data;
						Boolean enabled= (Boolean)command.getArguments().get(COMMAND_ENABLED);
						if (enabled != null) {
							enableEnable= !enabled.booleanValue();
							enableDisable= enabled.booleanValue();
							enabledSelected=  enabled.booleanValue();
							disabledSelected= !enabled.booleanValue();
						} else {
							enableDisable= true;
							enabledSelected= true;
							enableEnable= false;
						}
					} else {
						enableEnable= false;
						enableDisable= false;
					}
					enableEdit= false;
					enableUp= false;
					enableDown= false;
					
					if (data instanceof ErrorConfig) {
						continue;
					}
					enableRemove= false;
					break;
				}
			}
			if (!disabledSelected) {
				enableEnable= false;
			}
			if (!enabledSelected) {
				enableDisable= false;
			}
		}
		editButton.setEnabled(enableEdit);
		removeButton.setEnabled(enableRemove);
		upButton.setEnabled(enableUp);
		downButton.setEnabled(enableDown);
		enableButton.setEnabled(enableEnable);
		disableButton.setEnabled(enableDisable);
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
		int indices[]= builderTable.getSelectionIndices();
		if (indices.length < 1) {
			return;
		}
		int newSelection[]= new int[indices.length];
		int max= builderTable.getItemCount() - 1;
		for (int i = indices.length - 1; i >= 0; i--) {
			int index= indices[i];
			if (index < max) {
				move (builderTable.getItem(index), index + 1);
				newSelection[i]= index + 1;
			}
		}
		builderTable.setSelection(newSelection);
	}

	/**
	 * Move the current selection in the build list up.
	 */
	private void moveSelectionUp() {
		int indices[]= builderTable.getSelectionIndices();
		int newSelection[]= new int[indices.length];
		for (int i = 0; i < indices.length; i++) {
			int index= indices[i];
			if (index > 0) {
				move (builderTable.getItem(index), index - 1);
				newSelection[i]= index - 1;
			}
		}
		builderTable.setSelection(newSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
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
				ICommand command= (ICommand)data;
				Map args= command.getArguments();
				Boolean enabled= (Boolean)args.get(COMMAND_ENABLED);
				if (enabled != null && enabled.equals(Boolean.FALSE)) {
					ILaunchConfiguration config= disableCommand(command);
					if (config != null) {
						data= translateLaunchConfigurationToCommand(config, project);
					}
				} else {
					args.remove(COMMAND_ENABLED);
					command.setArguments(args);
				}
			} else if (data instanceof ILaunchConfiguration) {
				ILaunchConfiguration config= (ILaunchConfiguration) data;
				String disabledBuilderName;
				try {
					disabledBuilderName = config.getAttribute(IExternalToolConstants.ATTR_DISABLED_BUILDER, (String)null);
					if (disabledBuilderName != null && ExternalToolsUtil.isBuilderEnabled(config)) {
						commands[i]= translateBackToCommand(config, project);
						continue;
					}
				} catch (CoreException e1) {
				}
				
				if (!isUnmigratedConfig(config) && (config instanceof ILaunchConfigurationWorkingCopy)) {
					ILaunchConfigurationWorkingCopy workingCopy= ((ILaunchConfigurationWorkingCopy) config);
					// Save any changes to the config (such as enable/disable)
					if (workingCopy.isDirty()) {
						try {
							workingCopy.doSave();
						} catch (CoreException e) {
							MessageDialog.openError(getShell(), ExternalToolsUIMessages.getString("BuilderPropertyPage.39"), MessageFormat.format(ExternalToolsUIMessages.getString("BuilderPropertyPage.40"), new String[] {workingCopy.getName()})); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				data= translateLaunchConfigurationToCommand(config, project);
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
		
		if (configsToBeDeleted != null) {
			deleteConfigurations();
		}
		
		return super.performOk();
	}
	
	/**
	 * A non-external tool builder builder was disabled.
	 * It has been re-enabled. Translate the disabled external tool builder launch configuration
	 * wrapper back into the full fledged builder command.
	 */
	private ICommand translateBackToCommand(ILaunchConfiguration config, IProject project) {
		try {
			ICommand newCommand = project.getDescription().newCommand();
			String builderName= config.getAttribute(IExternalToolConstants.ATTR_DISABLED_BUILDER, (String)null);
			Map args= config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, new HashMap(0));
			
			newCommand.setBuilderName(builderName);
			newCommand.setArguments(args);
			if (configsToBeDeleted == null) {
				configsToBeDeleted= new ArrayList();
			}
			configsToBeDeleted.add(config);
			return newCommand;
		} catch (CoreException exception) {
			MessageDialog.openError(getShell(), ExternalToolsUIMessages.getString("BuilderPropertyPage.Command_error_13"), ExternalToolsUIMessages.getString("BuilderPropertyPage.error")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}
	
	/**
	 * 
	 * Translates a launch configuration to an ICommand for storage
	 */
	private ICommand translateLaunchConfigurationToCommand(ILaunchConfiguration config, IProject  project) {
		ICommand newCommand = null;
		try {
			newCommand = project.getDescription().newCommand();
			newCommand = toBuildCommand(config, newCommand);
		} catch (CoreException exception) {
			MessageDialog.openError(getShell(), ExternalToolsUIMessages.getString("BuilderPropertyPage.Command_error_13"), ExternalToolsUIMessages.getString("BuilderPropertyPage.error")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		return newCommand;
	}
	
	/**
	 * Disables a builder by wrappering the builder command as a disabled external tool builder.
	 * The details of the command is persisted in the launch configuration.
	 */
	private ILaunchConfiguration disableCommand(ICommand command) {
		Map arguments= command.getArguments();
		if (arguments != null) {
			arguments.remove(COMMAND_ENABLED);
		}
		List externalToolTypes= getConfigurationTypes(IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_CATEGORY);
		if (externalToolTypes.size() == 0) {
			return null;
		}
		ILaunchConfigurationType type= (ILaunchConfigurationType)externalToolTypes.get(0);
		if (type == null) {
			return null;
		}
		boolean wasAutobuilding= ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
		try {
			ILaunchConfigurationWorkingCopy workingCopy = null;
			String builderName = command.getBuilderName();
			String name= DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(builderName);
			workingCopy = type.newInstance(getBuilderFolder(true), name);		
					
			workingCopy.setAttribute(IExternalToolConstants.ATTR_DISABLED_BUILDER, builderName);
			if (arguments != null) {
				workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
			}
			workingCopy.setAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, false);
			ILaunchConfiguration config = null;
			setAutobuild(false);
			config = workingCopy.doSave();
			return config;
		} catch (CoreException e) {
			handleException(e);
		} finally {
			try {
				setAutobuild(wasAutobuilding);
			} catch (CoreException e) {
				handleException(e);
			}
		}
		return null;
	}
	
	/**
	 * Returns whether the given configuration is an "unmigrated" builder.
	 * Unmigrated builders are external tools that are stored in an old format
	 * but have not been migrated by the user. Old format builders are always
	 * translated into launch config working copies in memory, but they're not
	 * considered "migrated" until the config has been saved and the project spec
	 * updated.
	 * @param config the config to examine
	 * @return whether the given config represents an unmigrated builder
	 */
	private boolean isUnmigratedConfig(ILaunchConfiguration config) {
		return config.isWorkingCopy() && ((ILaunchConfigurationWorkingCopy) config).getOriginal() == null;
	}

	private void deleteConfigurations() {
		boolean wasAutobuilding= ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
		try {
			setAutobuild(false);
		
			Iterator itr= configsToBeDeleted.iterator();
			while (itr.hasNext()) {
				ILaunchConfiguration element = (ILaunchConfiguration) itr.next();
				element.delete();
			}
		} catch (CoreException e) {
			handleException(e);
		} finally {
			try {
				setAutobuild(wasAutobuilding);
			} catch (CoreException e) {
				handleException(e);
			}
		}
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
			IgnoreWhiteSpaceComparator comparator= new IgnoreWhiteSpaceComparator();
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
					Object oldArg= oldArgs.get(key);
					Object newArg= newArgs.get(key);
					if (oldArg instanceof String && newArg instanceof String) {
						if (comparator.compare(oldArg, newArg) != 0) {
							return true;
						}
					} else if (!oldArg.equals(newArg)){
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
				item.setText(ExternalToolsUIMessages.getString("BuilderPropertyPage.invalidBuildTool")); //$NON-NLS-1$
				item.setImage(invalidBuildToolImage);
				return;
			}
			item.setText(config.getName());
			Image configImage = labelProvider.getImage(config);
			if (configImage != null) {
				imagesToDispose.add(configImage);
				item.setImage(configImage);
			} else {
				item.setImage(builderImage);
			}
		} else {
			StringBuffer builderName = new StringBuffer(getBuilderName(builderID));
			Boolean enabled= (Boolean)command.getArguments().get(COMMAND_ENABLED);
			if (enabled != null && !enabled.booleanValue()) {
				builderName.append(ExternalToolsUIMessages.getString("BuilderPropertyPage.38")); //$NON-NLS-1$
			}
			item.setText(builderName.toString());
			item.setImage(builderImage);
		}
	}
	
	private String getBuilderName(String builderID) {
		// Get the human-readable name of the builder
		IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderID);
		String builderName;
		if (extension != null) {
			builderName = extension.getLabel();
		} else {
			builderName = MessageFormat.format(ExternalToolsUIMessages.getString("BuilderPropertyPage.missingBuilder"), new Object[] { builderID }); //$NON-NLS-1$
		}
		return builderName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
	public boolean performCancel() {
		Iterator iter= newConfigList.iterator();
		while (iter.hasNext()) {
			try {
				((ILaunchConfiguration) iter.next()).delete();
			} catch (CoreException e) {
				handleException(e);
			}
		}
		try {
			IFolder builderFolder= getBuilderFolder(false);
			if (builderFolder != null && builderFolder.exists() && builderFolder.members().length == 0) {
				// All files in the builder folder were newly created. Clean up
				builderFolder.delete(true, false, null);
			}
		} catch (CoreException e) {
			handleException(e);
		}
		
		//remove the local marking of the enabled state of the commands
		int numCommands = builderTable.getItemCount();
		for (int i = 0; i < numCommands; i++) {
			Object data = builderTable.getItem(i).getData();
			if (data instanceof ICommand) {
				ICommand command= (ICommand)data;
				Map args= command.getArguments();
				args.remove(COMMAND_ENABLED);
				command.setArguments(args);
			}
		}
		return super.performCancel();
	}
}
