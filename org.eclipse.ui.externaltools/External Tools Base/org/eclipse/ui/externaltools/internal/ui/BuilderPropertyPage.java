package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolRegistry;
import org.eclipse.ui.externaltools.internal.view.NewExternalToolAction;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.IStorageListener;

/**
 * Property page to add external tools in between builders.
 */
public final class BuilderPropertyPage extends PropertyPage {
	private static final int BUILDER_TABLE_WIDTH = 250;
	private static final String NEW_NAME= "BuilderPropertyPageName"; //$NON-NLS-1$
	private static final String IMG_BUILDER= "icons/full/obj16/builder.gif"; //$NON-NLS-1$;
	private static final String IMG_INVALID_BUILD_TOOL = "icons/full/obj16/invalid_build_tool.gif"; //$NON-NLS-1$

	private Table builderTable;
	private Button upButton, downButton, newButton, editButton, removeButton;
	private ArrayList imagesToDispose = new ArrayList();
	private Image builderImage, invalidBuildToolImage;
	private IStorageListener storageListener= new StorageListener();

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
			ExternalToolRegistry registry= ExternalToolsPlugin.getDefault().getToolRegistry(getShell());
			for (int i = 0; i < commands.length; i++) {
				ExternalTool tool = ExternalToolRegistry.toolFromBuildCommandArgs(commands[i].getArguments(), NEW_NAME);
				if (registry.hasToolNamed(tool.getName())) {
					addTool(tool, -1, false);
				} else {
					// If the tool generated from the command is not in the registry, it's
					// just a command, not a tool.
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
		if (select) builderTable.setSelection(position);
	}
	
	private void addTool(ExternalTool tool, int position, boolean select) {
		TableItem newItem;
		if (position < 0) {
			newItem = new TableItem(builderTable, SWT.NONE);
		} else {
			newItem = new TableItem(builderTable, SWT.NONE, position);
		}
		newItem.setData(tool);
		updateToolItem(newItem, tool);
		if (select) builderTable.setSelection(position);
	}
	
	private void updateToolItem(TableItem item, ExternalTool tool) {
		item.setText(tool.getName());
		Image toolImage= ExternalToolsPlugin.getDefault().getTypeRegistry().getToolType(tool.getType()).getImageDescriptor().createImage();
		imagesToDispose.add(toolImage);
		item.setImage(toolImage);
	}
	
	/**
	 * Configures and creates a new build command
	 * that invokes an external tool.  Returns the new command,
	 * or <code>null</code> if no command was created.
	 */
//	private ICommand createTool() {
//		try {
//			EditDialog dialog;
//			dialog = new EditDialog(getShell(), null);
//			if (dialog.open() == Window.OK) {
//				ExternalTool tool = dialog.getExternalTool();
//				ICommand command = getInputProject().getDescription().newCommand();
//				return tool.toBuildCommand(command);
//			} else {
//				return null;	
//			}
//		} catch(CoreException e) {
//			handleException(e);
//			return null;
//		}		
//	}
	
	public ICommand toBuildCommand(ExternalTool tool, ICommand command) {
		Map args= ExternalToolRegistry.toolToBuildCommandArgs(tool);
		command.setBuilderName(ExternalToolBuilder.ID);
		command.setArguments(args);
		
		return command;
	}
	
	/**
	 * Edits an exisiting build command that invokes an external tool.
	 */
//	private void editTool(ICommand command) {
//		ExternalTool tool = ExternalToolRegistry.toolFromBuildCommandArgs(command.getArguments(), NEW_NAME);
//		if (tool == null)
//			return;
//		EditDialog dialog;
//		dialog = new EditDialog(getShell(), tool);
//		if (dialog.open() == Window.OK) {
//			tool = dialog.getExternalTool();
//			tool.toBuildCommand(command);
//		}
//	}
	
	/**
	 * Creates and returns a button with the given label, id, and enablement.
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		button.setLayoutData(data); 
		button.setText(label);
		button.setEnabled(false);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleButtonPressed((Button)e.widget);
			}
		});
		return button;
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage.
	 */
	protected Control createContents(Composite parent) {
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
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		newButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.newButton")); //$NON-NLS-1$
		editButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.editButton")); //$NON-NLS-1$
		removeButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.removeButton")); //$NON-NLS-1$
		new Label(buttonArea, SWT.LEFT);
		upButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.upButton")); //$NON-NLS-1$
		downButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.downButton")); //$NON-NLS-1$
	
		newButton.setEnabled(true);
		
		//populate widget contents	
		addBuildersToTable();
		initializeStorageListener();
		
		return topLevel;
	}
	
	private void initializeStorageListener() {
		ExternalToolStorage.addStorageListener(storageListener);
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
		ExternalToolStorage.removeStorageListener(storageListener);
	}
	
	/**
	 * Returns the project that is the input for this property page,
	 * or <code>null</code>.
	 */
	private IProject getInputProject() {
		IAdaptable element = getElement();
		if (element instanceof IProject) {
			return (IProject)element;
		}
		Object resource = element.getAdapter(IResource.class);
		if (resource instanceof IProject) {
			return (IProject)resource;
		}
		return null;
	}
	
	/**
	 * One of the buttons has been pressed, act accordingly.
	 */
	private void handleButtonPressed(Button button) {
		if (button == newButton) {
			new NewExternalToolAction().run();
		} else if (button == editButton) {
			TableItem[] selection = builderTable.getSelection();
			if (selection != null) {
				new PropertyDialogAction(getShell(), new ISelectionProvider() {
					public void addSelectionChangedListener(ISelectionChangedListener listener) {
					}

					public ISelection getSelection() {
						TableItem[] items= builderTable.getSelection();
						List list= new ArrayList(items.length);
						for (int i= 0, numItems= items.length; i < numItems; i++) {
							list.add(items[i].getData());
						}
						return new StructuredSelection(list);
					}

					public void removeSelectionChangedListener(ISelectionChangedListener listener) {
					}

					public void setSelection(ISelection selection) {
					}					
				}).run();
				Object data= selection[0].getData();
				if (data instanceof ExternalTool) {
					// The table contains ExternalTools and ICommands,
					// but we only edit ExternalTools
					updateToolItem(selection[0], (ExternalTool) data);
				}
			}
		} else if (button == removeButton) {
			TableItem[] selection = builderTable.getSelection();
			if (selection != null) {
				for (int i = 0; i < selection.length; i++) {
					selection[i].dispose();
				}
			}
		} else if (button == upButton) {
			moveSelectionUp();
		} else if (button == downButton) {
			moveSelectionDown();
		}
		handleTableSelectionChanged();
		builderTable.setFocus();
	}
	
	/**
	 * Handles unexpected internal exceptions
	 */
	private void handleException(Exception e) {
		IStatus status;
		if (e instanceof CoreException) {
			status = ((CoreException)e).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("BuilderPropertyPage.statusMessage"), e); //$NON-NLS-1$
		}
		ErrorDialog.openError(
			getShell(),
			ToolMessages.getString("BuilderPropertyPage.errorTitle"), //$NON-NLS-1$
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
			Object data= item.getData();
			if (data instanceof ExternalTool) {
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
				int selection = builderTable.getSelectionIndex();
				int max = builderTable.getItemCount();
				upButton.setEnabled(selection != 0);
				downButton.setEnabled(selection < max-1);
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
		Object data = item.getData();
		String text = item.getText();
		Image image= item.getImage();
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
				move(builderTable.getItem(currentIndex), currentIndex+1);
				builderTable.setSelection(currentIndex+1);
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
			move(builderTable.getItem(currentIndex), currentIndex-1);
			builderTable.setSelection(currentIndex-1);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IPreferencePage.
	 */
	public boolean performOk() {
		IProject project = getInputProject();
		//get all the build commands
		int numCommands = builderTable.getItemCount();
		ICommand[] commands = new ICommand[numCommands];
		for (int i = 0; i < numCommands; i++) {
			Object data= builderTable.getItem(i).getData();
			if (data instanceof ICommand) {
			} else if (data instanceof ExternalTool) {
				// Translate ExternalTools to ICommands for storage
				ICommand newCommand= null;
				try {
					newCommand= project.getDescription().newCommand();
				} catch (CoreException exception) {
					MessageDialog.openError(getShell(), "Command error", "An error occurred while saving the project's build commands");
					return true;
				}
				data= toBuildCommand(((ExternalTool)data), newCommand);
			}
			commands[i] = (ICommand)data;
		}
		//set the build spec
		try {
			IProjectDescription desc = project.getDescription();
			desc.setBuildSpec(commands);
			project.setDescription(desc, null);
		} catch(CoreException e) {
			handleException(e);
		}
		return super.performOk();
	}
	
	/**
	 * Update the table item with the given build command
	 */
	private void updateCommandItem(TableItem item, ICommand command) {
		String builderID = command.getBuilderName();
		if (builderID.equals(ExternalToolBuilder.ID)) {
			ExternalTool tool = ExternalToolRegistry.toolFromBuildCommandArgs(command.getArguments(), NEW_NAME);
			if (tool == null) {
				item.setText(ToolMessages.getString("BuilderPropertyPage.invalidBuildTool")); //$NON-NLS-1$
				item.setImage(invalidBuildToolImage);
				return;
			}
			item.setText(tool.getName());
			Image toolImage= ExternalToolsPlugin.getDefault().getTypeRegistry().getToolType(tool.getType()).getImageDescriptor().createImage();
			imagesToDispose.add(toolImage);
			item.setImage(toolImage);
		} else {
			// Get the human-readable name of the builder
			IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, 	ResourcesPlugin.PT_BUILDERS, builderID);
			String builderName;
			if (extension != null)
				builderName = extension.getLabel();
			else
				builderName = ToolMessages.format("BuilderPropertyPage.missingBuilder", new Object[] {builderID}); //$NON-NLS-1$
			item.setText(builderName);
			item.setImage(builderImage);
		}
	}
	
	private class StorageListener implements IStorageListener {
		/**
		 * @see IStorageListener#toolDeleted(ExternalTool)
		 */
		public void toolDeleted(ExternalTool tool) {
		}
		/**
		 * @see IStorageListener#toolCreated(ExternalTool)
		 */
		public void toolCreated(ExternalTool tool) {
			try {
				final ICommand newCommand;
				newCommand= getInputProject().getDescription().newCommand();
				toBuildCommand(tool, newCommand);
				if (newCommand != null) {
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							int insertPosition = builderTable.getSelectionIndex() + 1;
							addCommand(newCommand, insertPosition, true);
						}
					});
				}
			} catch (CoreException e) {
			}
		}
		/**
		 * @see IStorageListener#toolModified(ExternalTool)
		 */
		public void toolModified(ExternalTool tool) {
		}
		/**
		 * @see IStorageListener#toolsRefreshed()
		 */
		public void toolsRefreshed() {
		}
	}
}
