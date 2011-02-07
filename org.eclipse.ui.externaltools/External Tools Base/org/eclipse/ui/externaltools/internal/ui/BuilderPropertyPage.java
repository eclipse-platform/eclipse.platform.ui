/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ui;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.model.BuilderCoreUtils;
import org.eclipse.core.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsMainTab;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.launchConfigurations.IgnoreWhiteSpaceComparator;
import org.eclipse.ui.externaltools.internal.model.BuilderUtils;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;
import org.eclipse.ui.progress.IProgressService;

/**
 * Property page to add external tools builders.
 */
public final class BuilderPropertyPage extends PropertyPage implements ICheckStateListener {

	//locally mark a command's enabled state so it can be processed correctly on performOK
	private static final String COMMAND_ENABLED= "CommandEnabled"; //$NON-NLS-1$

	private Button upButton, downButton, newButton, importButton, editButton, removeButton;
	
	private boolean userHasMadeChanges= false;
	
	private List configsToBeDeleted= null;
	private List commandsToBeDeleted= null;
	
	private CheckboxTableViewer viewer= null;
	
	private boolean fWarned = false;
	
	private ILabelProvider labelProvider= new BuilderLabelProvider();
	
	/**
	 * Error configs are objects representing entries pointing to
	 * invalid launch configurations
	 */
	public class ErrorConfig {
		private ICommand command;
		public ErrorConfig(ICommand command) {
			this.command= command;
		}
		public ICommand getCommand() {
			return command;
		}
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
            //Replace the movedFrom config in the list of newly created configs
            if (newConfigList.remove(oldConfig)) {
                newConfigList.add(configuration);
            }
			
			Display.getDefault().asyncExec(new Runnable() {	
				public void run() {
					TableItem[] items= viewer.getTable().getItems();
					for (int i = 0; i < items.length; i++) {
						TableItem item = items[i];
						Object data= item.getData();
						if (data == oldConfig) {
							// Found the movedFrom config in the tree. Replace it with the new config 
							item.setData(configuration);
							viewer.update(configuration, null);
							break;
						}
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
		ICommand[] commands= null;
		try {
			commands = project.getDescription().getBuildSpec();
		} catch (CoreException e) {
			handleException(e);
            return;
		}
	
		boolean projectNeedsMigration= false;
		for (int i = 0; i < commands.length; i++) {
			String[] version= new String[] {IExternalToolConstants.EMPTY_STRING};
			ILaunchConfiguration config = BuilderUtils.configFromBuildCommandArgs(project, commands[i].getArguments(), version);
			if (BuilderCoreUtils.VERSION_2_1.equals(version[0])) {
				// Storing the .project file of a project with 2.1 configs, will
				// edit the file in a way that isn't backwards compatible.
				projectNeedsMigration= true;
			}
			Object element= null;
			if (config != null) {
				if (!config.isWorkingCopy() && !config.exists()) {
                    Shell shell= getShell();
                    if (shell == null) {
                        return;
                    }
					IStatus status = new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, NLS.bind(ExternalToolsUIMessages.BuilderPropertyPage_Exists, new String[]{config.getName()}), null);
					ErrorDialog.openError(getShell(), ExternalToolsUIMessages.BuilderPropertyPage_errorTitle,
									NLS.bind(ExternalToolsUIMessages.BuilderPropertyPage_External_Tool_Builder__0__Not_Added_2, new String[]{config.getName()}),
									status);
					userHasMadeChanges= true;
				} else {
					element= config;
				}
			} else {
				String builderID = commands[i].getBuilderName();
				if (builderID.equals(ExternalToolBuilder.ID) && commands[i].getArguments().get(BuilderCoreUtils.LAUNCH_CONFIG_HANDLE) != null) {
					// An invalid external tool entry.
					element= new ErrorConfig(commands[i]);
				} else {
					element= commands[i];
				}
			}
			if (element != null) {
				viewer.add(element);
				viewer.setChecked(element, isEnabled(element));
			}
		}
		if (projectNeedsMigration) {
			IPreferenceStore store= ExternalToolsPlugin.getDefault().getPreferenceStore();
			boolean prompt= store.getBoolean(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION);
			boolean proceed= true;
			if (prompt) {
                Shell shell= getShell();
                if (shell == null) {
                    return;
                }
				MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(shell, ExternalToolsUIMessages.BuilderPropertyPage_0, ExternalToolsUIMessages.BuilderPropertyPage_1, ExternalToolsUIMessages.BuilderPropertyPage_2, false, null, null);
				proceed= dialog.getReturnCode() == IDialogConstants.YES_ID;
				store.setValue(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION, !dialog.getToggleState());
			}
			if (!proceed) {
				// Open the page read-only
				viewer.getTable().setEnabled(false);
				downButton.setEnabled(false);
				editButton.setEnabled(false);
				importButton.setEnabled(false);
				newButton.setEnabled(false);
				removeButton.setEnabled(false);
			}
		}
	}

	/**
	 * Creates and returns a button with the given label, id, and enablement.
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		button.setEnabled(false);
		button.addSelectionListener(buttonListener);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		return button;
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IExternalToolsHelpContextIds.EXTERNAL_TOOLS_BUILDER_PROPERTY_PAGE);
		
		Font font = parent.getFont();

		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		topLevel.setLayout(layout);
		topLevel.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label description = new Label(topLevel, SWT.WRAP);
		description.setText(ExternalToolsUIMessages.BuilderPropertyPage_description);
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
		viewer= CheckboxTableViewer.newCheckList(tableAndButtons, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.setLabelProvider(labelProvider);
		viewer.addCheckStateListener(this);
		Table builderTable= viewer.getTable();
		builderTable.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		newButton = createButton(buttonArea, ExternalToolsUIMessages.BuilderPropertyPage_newButton);
		importButton = createButton(buttonArea, ExternalToolsUIMessages.BuilderPropertyPage__Import____3);
		editButton = createButton(buttonArea, ExternalToolsUIMessages.BuilderPropertyPage_editButton);
		removeButton = createButton(buttonArea, ExternalToolsUIMessages.BuilderPropertyPage_removeButton);
		new Label(buttonArea, SWT.LEFT);
		upButton = createButton(buttonArea, ExternalToolsUIMessages.BuilderPropertyPage_upButton);
		downButton = createButton(buttonArea, ExternalToolsUIMessages.BuilderPropertyPage_downButton);

		newButton.setEnabled(true);
		importButton.setEnabled(true);

		//populate widget contents	
		addBuildersToTable();

		return topLevel;
	}
	
	/**
	 * Turns auto-building on or off in the workspace.
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
		} else if (button == importButton) {
			handleImportButtonPressed();
		} else if (button == editButton) {
			handleEditButtonPressed();
		} else if (button == removeButton) {
			handleRemoveButtonPressed();
		} else if (button == upButton) {
			moveSelectionUp();
		} else if (button == downButton) {
			moveSelectionDown();
		}
        if (getControl().isDisposed()) {
            return;
        }
		handleTableSelectionChanged();
		viewer.getTable().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element= event.getElement();
		boolean checked = event.getChecked();
		if (element instanceof ILaunchConfiguration) {
			enableLaunchConfiguration((ILaunchConfiguration) element, checked);
		} else if (element instanceof ICommand) {
            Shell shell= getShell();
            if (shell == null) {
                return;
            }
            if (checked) {
            	enableCommand((ICommand)element, checked);
            	return;
            } else if (!fWarned) {
            	if(MessageDialog.openConfirm(shell, ExternalToolsUIMessages.BuilderPropertyPage_6, ExternalToolsUIMessages.BuilderPropertyPage_7)) {
            		fWarned = true;
            	}
			} 
            if(fWarned) {
            	enableCommand((ICommand)element, checked);
            }
            else {
            	viewer.removeCheckStateListener(this);
    			viewer.setChecked(element, true);
    			viewer.addCheckStateListener(this);
			}
            
		}
	}

	private void enableLaunchConfiguration(ILaunchConfiguration configuration, boolean enable) {
		ILaunchConfigurationWorkingCopy workingCopy= null;
		try {
			if (configuration instanceof ILaunchConfigurationWorkingCopy) {
				workingCopy = (ILaunchConfigurationWorkingCopy) configuration;
			} else {
				// Replace the config with a working copy
				TableItem[] items= viewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {
					TableItem item = items[i];
					if (item.getData() == configuration) {
						workingCopy = configuration.getWorkingCopy();
						item.setData(workingCopy);
					}
				}
			}
			if (workingCopy != null) {
				workingCopy.setAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, enable);
			}
		} catch (CoreException e) {
			return;
		}
		userHasMadeChanges= true;
	}
	
	private void enableCommand(ICommand command, boolean enable) {
		Map args= command.getArguments();
		if (args == null) {
			args= new HashMap(1);
		}
		args.put(COMMAND_ENABLED, Boolean.valueOf(enable));
		command.setArguments(args);
		userHasMadeChanges= true;
	}

	/**
	 * The user has pressed the import button. Prompt them to select a
	 * configuration to import from the workspace.
	 */	
	private void handleImportButtonPressed() {
		ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
		List toolTypes= getConfigurationTypes(IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_CATEGORY);
		List configurations= new ArrayList();
		Iterator iter= toolTypes.iterator();
		while (iter.hasNext()) {
			try {
				ILaunchConfiguration[] configs= manager.getLaunchConfigurations((ILaunchConfigurationType) iter.next());
				for (int i = 0; i < configs.length; i++) {
					ILaunchConfiguration launchConfiguration = configs[i];
					if (!DebugUITools.isPrivate(launchConfiguration)) {
						configurations.add(launchConfiguration);
					}
				}
			} catch (CoreException e) {
			}
		}
        Shell shell= getShell();
        if (shell == null) {
            return;
        }
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new BuilderLabelProvider());
		dialog.setTitle(ExternalToolsUIMessages.BuilderPropertyPage_4);
		dialog.setMessage(ExternalToolsUIMessages.BuilderPropertyPage_5);
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
			newConfig= BuilderUtils.duplicateConfiguration(getInputProject(), config);
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
			viewer.add(newConfig);
			viewer.setChecked(newConfig, isEnabled(newConfig));
			newConfigList.add(newConfig);
		}
	}
	
	/**
	 * The user has pressed the remove button. Delete the selected builder.
	 */
	private void handleRemoveButtonPressed() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection != null) {
			int numSelected= selection.size();
			userHasMadeChanges= true;
			Iterator iterator= selection.iterator();
			while (iterator.hasNext()) {
				Object item= iterator.next();
				if (item instanceof ILaunchConfiguration) {
					if (configsToBeDeleted == null) {
						configsToBeDeleted= new ArrayList(numSelected);
					}
					configsToBeDeleted.add(item);
				} else if (item instanceof ICommand) {
					if (commandsToBeDeleted == null) {
						commandsToBeDeleted= new ArrayList(numSelected);
					}
					commandsToBeDeleted.add(item);
				}
				viewer.remove(item);
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
			String name= DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(ExternalToolsUIMessages.BuilderPropertyPage_New_Builder_7);
			workingCopy = type.newInstance(BuilderUtils.getBuilderFolder(getInputProject(), true), name);		
			
			StringBuffer buffer= new StringBuffer(IExternalToolConstants.BUILD_TYPE_FULL);
			buffer.append(',');
			buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
			buffer.append(',');
			workingCopy.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
			workingCopy.setAttribute(ExternalToolsMainTab.FIRST_EDIT, true);
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
				config= (ILaunchConfiguration)newConfigList.get(newConfigList.size() - 1);
				viewer.add(config);
				viewer.setChecked(config, isEnabled(config));
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
        Shell shell= getShell();
        if (shell == null) {
            return Window.CANCEL;
        }
		int code= DebugUITools.openLaunchConfigurationPropertiesDialog(shell, config, org.eclipse.ui.externaltools.internal.model.IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP);
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
        Shell shell= getShell();
        if (shell == null) {
            return null;
        }
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new BuilderLabelProvider());
		dialog.setElements(externalToolTypes.toArray());
		dialog.setMultipleSelection(false);
		dialog.setTitle(ExternalToolsUIMessages.BuilderPropertyPage_Choose_configuration_type_8);
		dialog.setMessage(ExternalToolsUIMessages.BuilderPropertyPage_Choose_an_external_tool_type_to_create_9);
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
	 * The user has pressed the edit button or double-clicked. Open the launch configuration edit
	 * dialog on the selection after migrating the tool if necessary.
	 */
	private void handleEditButtonPressed() {
		TableItem[] items= viewer.getTable().getSelection();
		if (items.length == 0) {
			return;
		}
		TableItem selection= items[0];
		if (selection != null) {
			Object data = selection.getData();
			if (data instanceof ILaunchConfiguration) {
				ILaunchConfiguration config= (ILaunchConfiguration) data;
				if (BuilderUtils.isUnmigratedConfig(config)) {
					if (!shouldProceedWithMigration()) {
						return;
					}
					try {
						config= BuilderUtils.migrateBuilderConfiguration(getInputProject(), (ILaunchConfigurationWorkingCopy) config);
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
			} else if (data instanceof ICommand) {
				ICommand command= (ICommand) data;
				if (command.isConfigurable()) {
					if (editCommand(command)) {
						userHasMadeChanges= true;	
					}
				}
			}
		}
	}
	
	private boolean editCommand(ICommand data) {
		EditCommandDialog dialog= new EditCommandDialog(getShell(), data);
		return Window.OK == dialog.open();
	}

	/**
	 * Prompts the user to proceed with the migration of a project builder from
	 * the old format to the new, launch configuration-based, format and returns
	 * whether or not the user wishes to proceed with the migration.
	 * 
	 * @return boolean whether or not the user wishes to proceed with migration
	 */
	private boolean shouldProceedWithMigration() {
		if (!ExternalToolsPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION)) {
			// User has asked not to be prompted
			return true;
		}
        Shell shell= getShell();
        if (shell == null) {
            return false;
        }
		// Warn the user that editing an old config will cause storage migration.
		MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), 
			ExternalToolsUIMessages.BuilderPropertyPage_Migrate_project_builder_10,
			ExternalToolsUIMessages.BuilderPropertyPage_Not_Support,
			ExternalToolsUIMessages.BuilderPropertyPage_Prompt,
			false,
			ExternalToolsPlugin.getDefault().getPreferenceStore(), 
			IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION);
		return dialog.getReturnCode() == IDialogConstants.YES_ID;
	}

	/**
	 * Handles unexpected internal exceptions
	 */
	private void handleException(Exception e) {
		final IStatus[] status= new IStatus[1];
		if (e instanceof CoreException) {
			status[0] = ((CoreException) e).getStatus();
		} else {
			status[0] = new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, ExternalToolsUIMessages.BuilderPropertyPage_statusMessage, e);
		}
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		        Shell shell= getShell();
		        if (shell != null) {
		            ErrorDialog.openError(shell, ExternalToolsUIMessages.BuilderPropertyPage_errorTitle,
		                    ExternalToolsUIMessages.BuilderPropertyPage_errorMessage,
		                    status[0]);
		        }
		    }
		});
	}

	/**
	 * The user has selected a different builder in table.
	 * Update button enablement.
	 */
	private void handleTableSelectionChanged() {
		newButton.setEnabled(true);
		Table builderTable= viewer.getTable();
		TableItem[] items = builderTable.getSelection();
		boolean enableEdit = false;
		boolean enableRemove = false;
		boolean enableUp = false;
		boolean enableDown = false;
		if(items != null) {
			boolean validSelection =  items.length > 0;
			enableEdit = validSelection;
			enableRemove = validSelection;
			enableUp = validSelection;
			enableDown = validSelection;
			if (items.length > 1) {
				enableEdit= false;
			}
			int indices[]= builderTable.getSelectionIndices();
			int max = builderTable.getItemCount();
			if(indices.length > 0) {
				enableUp = indices[0] != 0;
				enableDown = indices[indices.length - 1] < max - 1;
			}
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				Object data= item.getData();
				if (data instanceof ILaunchConfiguration) {
					ILaunchConfiguration config= (ILaunchConfiguration)data;
					String builderName= null;
					try {
						builderName = config.getAttribute(IExternalToolConstants.ATTR_DISABLED_BUILDER, (String)null);
					} catch (CoreException e) {
					}
					if (builderName != null) {
						//do not allow "wrapped" builders to be removed or edited if they are valid
						IExtension ext= Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderName);
						enableEdit= false;
						enableRemove= ext == null;
					}
				} else {
					if (data instanceof ErrorConfig) {
						enableEdit= false;
						continue;
					}
					ICommand command= (ICommand) data;
					enableEdit= command.isConfigurable();
					IExtension ext= Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, command.getBuilderName());
					enableRemove= ext == null;
					break;
				}
			}
		}
		editButton.setEnabled(enableEdit);
		removeButton.setEnabled(enableRemove);
		upButton.setEnabled(enableUp);
		downButton.setEnabled(enableDown);
	}
	
	/**
	 * Returns whether the given element (command or launch config)
	 * is enabled.
	 * 
	 * @param element the element
	 * @return whether the given element is enabled
	 */
	private boolean isEnabled(Object element) {
		if (element instanceof ICommand) {
			Boolean enabled= (Boolean)((ICommand) element).getArguments().get(COMMAND_ENABLED);
			if (enabled != null) {
				return enabled.booleanValue();
			}
		} else if (element instanceof ILaunchConfiguration) {
			try {
				return ExternalToolsUtil.isBuilderEnabled((ILaunchConfiguration) element);
			} catch (CoreException e) {
			}
		} else if (element instanceof ErrorConfig) {
			return false;
		}
		return true;
	}

	/**
	 * Moves an entry in the builder table to the given index.
	 */
	private void move(TableItem item, int index) {
		userHasMadeChanges= true;
		Object data = item.getData();
		item.dispose();
		viewer.insert(data, index);
		viewer.setChecked(data, isEnabled(data));
	}

	/**
	 * Move the current selection in the build list down.
	 */
	private void moveSelectionDown() {
		Table builderTable= viewer.getTable();
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
		Table builderTable= viewer.getTable();
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
		Table builderTable= viewer.getTable();
		int numCommands = builderTable.getItemCount();
		final Object[] itemData= new Object[numCommands];
		for (int i = 0; i < numCommands; i++) {
			itemData[i]= builderTable.getItem(i).getData();
		}
		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				doPerformOk(monitor, itemData);
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
			}
		};
		
		IProgressService service= PlatformUI.getWorkbench().getProgressService();
		try {
			service.busyCursorWhile(runnable);
		} catch (InvocationTargetException e) {
			return false;
		} catch (InterruptedException e) {
			return false;		
		}
		return super.performOk();
	}
	
	private void doPerformOk(IProgressMonitor monitor, Object[] itemData) {
		if (monitor.isCanceled()) {
			return;
		}
		
		IProject project = getInputProject();
		//get all the build commands
		int numCommands = itemData.length;
		monitor.beginTask(ExternalToolsUIMessages.BuilderPropertyPage_3, numCommands + 1);
		List possibleCommands= new ArrayList(numCommands);
		for (int i = 0; i < numCommands; i++) {
			Object data = itemData[i];
			if (data instanceof ICommand) {
				if (commandsToBeDeleted != null && commandsToBeDeleted.contains(data)) {
					//command specified to be removed
					data= null;
				}
				ICommand command= (ICommand)data;
				Map args= command.getArguments();
				Boolean enabled= (Boolean)args.get(COMMAND_ENABLED);
				if (enabled != null && enabled.equals(Boolean.FALSE)) {
					ILaunchConfiguration config= disableCommand(command);
					if (config != null) {
						data= BuilderUtils.commandFromLaunchConfig(project,config);
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
						possibleCommands.add(translateBackToCommand(config, project));
						continue;
					}
				} catch (CoreException e1) {
				}
				
				if (!BuilderUtils.isUnmigratedConfig(config) && (config instanceof ILaunchConfigurationWorkingCopy)) {
					ILaunchConfigurationWorkingCopy workingCopy= ((ILaunchConfigurationWorkingCopy) config);
					// Save any changes to the config (such as enable/disable)
					if (workingCopy.isDirty()) {
						try {
							workingCopy.doSave();
						} catch (CoreException e) {
                            Shell shell= getShell();
                            if (shell != null) {
                                MessageDialog.openError(shell, ExternalToolsUIMessages.BuilderPropertyPage_39, NLS.bind(ExternalToolsUIMessages.BuilderPropertyPage_40, new String[] {workingCopy.getName()}));
                            }
						}
					}
				}
				data= BuilderUtils.commandFromLaunchConfig(project, config);
			} else if (data instanceof ErrorConfig) {
				data= ((ErrorConfig) data).getCommand();
			}
			if (data != null) {
				possibleCommands.add(data);
			}
			monitor.worked(1);
		}
		ICommand[] commands= new ICommand[possibleCommands.size()];
		possibleCommands.toArray(commands);
		if (checkCommandsForChange(commands)) {
			//set the build spec
			try {
				IProjectDescription desc = project.getDescription();
				desc.setBuildSpec(commands);
				project.setDescription(desc, IResource.FORCE, monitor);
			} catch (CoreException e) {
				handleException(e);
                performCancel();
			}
		}
		
		if (configsToBeDeleted != null) {
			deleteConfigurations();
		}
		monitor.done();
	}
	
	private void checkBuilderFolder() {
		try {
			IFolder builderFolder= BuilderUtils.getBuilderFolder(getInputProject(), false);
			if (builderFolder != null && builderFolder.exists() && builderFolder.members().length == 0) {
				// All files in the builder folder have been deleted. Clean up
				builderFolder.delete(true, false, null);
			}
		} catch (CoreException e) {
			handleException(e);
		}
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
            Shell shell= getShell();
            if (shell != null) {
                MessageDialog.openError(shell, ExternalToolsUIMessages.BuilderPropertyPage_13, ExternalToolsUIMessages.BuilderPropertyPage_error);
            }
			return null;
		}
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
			String name= DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(builderName);
			workingCopy = type.newInstance(BuilderUtils.getBuilderFolder(getInputProject(), true), name);		
					
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

	private void deleteConfigurations() {
		boolean wasAutobuilding= ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
		try {
			setAutobuild(false);
		
			Iterator itr= configsToBeDeleted.iterator();
			while (itr.hasNext()) {
				ILaunchConfiguration element = (ILaunchConfiguration) itr.next();
				element.delete();
			}
			
			checkBuilderFolder();
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
				if (oldArgs == null) {
					if(newArgs != null) {
						return true;
					}
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
				
				if (oldCommand.isBuilding(IncrementalProjectBuilder.AUTO_BUILD) != newCommand.isBuilding(IncrementalProjectBuilder.AUTO_BUILD)
						|| oldCommand.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD) != newCommand.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD)
						|| oldCommand.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD) != newCommand.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD)
						|| oldCommand.isBuilding(IncrementalProjectBuilder.FULL_BUILD) != newCommand.isBuilding(IncrementalProjectBuilder.FULL_BUILD)) {
					return true;
				}
			}
		} catch (CoreException ce) {
			return true;
		}
		return false;	
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
		
		checkBuilderFolder();
		
		//remove the local marking of the enabled state of the commands
		Table builderTable= viewer.getTable();
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
    
    public Shell getShell() {
        if (getControl().isDisposed()) {
			return null;
        }
        return super.getShell();
    }
}
