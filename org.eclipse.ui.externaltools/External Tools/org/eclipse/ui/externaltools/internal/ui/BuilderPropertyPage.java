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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.externaltools.internal.core.ExternalTool;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsBuilder;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

/**
 * Property page to add tool scripts in between builders.
 */
public final class BuilderPropertyPage extends PropertyPage {
	private Table builderTable;
	private Button upButton, downButton, newButton, editButton, removeButton;
	private ArrayList imagesToDispose = new ArrayList();
	private Image antImage, builderImage;

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
			dialog.open();
			ExternalTool script = dialog.getExternalTool();
			if (script == null) {
				return null;
			}
			ICommand command = getInputProject().getDescription().newCommand();
			return script.toBuildCommand(command);
		} catch(CoreException e) {
			handleException(e);
			return null;
		}		
	}
	
	/**
	 * Edits an exisiting build command that invokes an external tool.
	 */
	private void editTool(ICommand command) {
		ExternalTool script = ExternalTool.fromArgumentMap(command.getArguments());
		if (script == null)
			return;
		EditDialog dialog;
		dialog = new EditDialog(getShell(), script);
		dialog.open();
		script = dialog.getExternalTool();
		script.toBuildCommand(command);
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
		antImage = ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_ANT_SCRIPT).createImage();
		builderImage = ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_BUILDER).createImage();
		imagesToDispose.add(antImage);
		imagesToDispose.add(builderImage);
		
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
	
		// table of builders and scripts		
		builderTable = new Table(tableAndButtons, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		builderTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout tableLayout = new TableLayout();
		builderTable.setLayout(tableLayout);
		TableColumn tc = new TableColumn(builderTable, SWT.NONE);
		tc.setResizable(false);
		tableLayout.addColumnData(new ColumnWeightData(100));
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
	 * Returns the image to use for the given command line
	 * string.  Returns a default image if none could be computed.
	 */
	private Image imageForProgram(String commandLine) {
		Image image = antImage;
		int lastDot = commandLine.lastIndexOf('.');
		String name = ""; //$NON-NLS-1$
		if (lastDot > 0)
			name = commandLine.substring(lastDot);
		Program program = Program.findProgram(name);
		if (program != null) {
			ImageData data = program.getImageData();
			if (data != null) {
				image = new Image(getShell().getDisplay(), data);
				imagesToDispose.add(image);
			}
		}
		return image;
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
		String builderName = command.getBuilderName();
		if (builderName.equals(ExternalToolsBuilder.ID)) {
			ExternalTool script = ExternalTool.fromArgumentMap(command.getArguments());
			item.setText(script.getName());
			if (script.TOOL_TYPE_ANT.equals(script.getType())) {
				item.setImage(antImage);
			} else {
				Image image = imageForProgram(script.getLocation());
				item.setImage(image);
			}
		} else {
			item.setText(builderName);
			item.setImage(builderImage);
		}
	}
}
