package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.internal.core.*;

/**
 * Property page to add external tools in between builders.
 */
public final class BuilderPropertyPage extends PropertyPage {
	private static final int BUILDER_TABLE_WIDTH = 250;

	private Table builderTable;
	private Button upButton, downButton, newButton, editButton, removeButton;
	private ArrayList imagesToDispose = new ArrayList();
	private Image antImage, builderImage, externalToolImage, invalidBuildToolImage;

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
				addCommand(commands[i], -1, false);
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
	
	/**
	 * Configures and creates a new build command
	 * that invokes an external tool.  Returns the new command,
	 * or <code>null</code> if no command was created.
	 */
	private ICommand createTool() {
		try {
			EditDialog dialog;
			dialog = new EditDialog(getShell(), null);
			if (dialog.open() == Window.OK) {
				ExternalTool tool = dialog.getExternalTool();
				ICommand command = getInputProject().getDescription().newCommand();
				return tool.toBuildCommand(command);
			} else {
				return null;	
			}
		} catch(CoreException e) {
			handleException(e);
			return null;
		}		
	}
	
	/**
	 * Edits an exisiting build command that invokes an external tool.
	 */
	private void editTool(ICommand command) {
		ExternalTool tool = ExternalTool.fromArgumentMap(command.getArguments());
		if (tool == null)
			return;
		EditDialog dialog;
		dialog = new EditDialog(getShell(), tool);
		if (dialog.open() == Window.OK) {
			tool = dialog.getExternalTool();
			tool.toBuildCommand(command);
		}
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
		externalToolImage = ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_EXTERNAL_TOOL).createImage();
		antImage = ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_ANT_TOOL).createImage();
		builderImage = ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_BUILDER).createImage();
		invalidBuildToolImage = ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_INVALID_BUILD_TOOL).createImage();

		imagesToDispose.add(externalToolImage);
		imagesToDispose.add(antImage);
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
		Label buttonSpacer = new Label(buttonArea, SWT.LEFT);
		upButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.upButton")); //$NON-NLS-1$
		downButton = createButton(buttonArea, ToolMessages.getString("BuilderPropertyPage.downButton")); //$NON-NLS-1$
	
		newButton.setEnabled(true);
		
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
			ICommand newCommand = createTool();
			if (newCommand != null) {
				int insertPosition = builderTable.getSelectionIndex() + 1;
				addCommand(newCommand, insertPosition, true);
			}
		} else if (button == editButton) {
			TableItem[] selection = builderTable.getSelection();
			if (selection != null) {
				editTool((ICommand)selection[0].getData());
				updateCommandItem(selection[0],(ICommand)selection[0].getData());
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
			status = new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, ToolMessages.getString("BuilderPropertyPage.statusMessage"), e); //$NON-NLS-1$
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
			ICommand buildCommand = (ICommand)item.getData();
			if (buildCommand.getBuilderName().equals(ExternalToolsBuilder.ID)) {
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
		//get all the build commands
		int numCommands = builderTable.getItemCount();
		ICommand[] commands = new ICommand[numCommands];
		for (int i = 0; i < numCommands; i++) {
			commands[i] = (ICommand)builderTable.getItem(i).getData();
		}
		//set the build spec
		IProject project = getInputProject();
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
		if (builderID.equals(ExternalToolsBuilder.ID)) {
			ExternalTool tool = ExternalTool.fromArgumentMap(command.getArguments());
			if (tool == null) {
				item.setText(ToolMessages.getString("BuilderPropertyPage.invalidBuildTool")); //$NON-NLS-1$
				item.setImage(invalidBuildToolImage);
				return;
			}
			item.setText(tool.getName());
			if (tool.TOOL_TYPE_ANT.equals(tool.getType())) {
				item.setImage(antImage);
			} else {
				item.setImage(externalToolImage);
			}
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
}
