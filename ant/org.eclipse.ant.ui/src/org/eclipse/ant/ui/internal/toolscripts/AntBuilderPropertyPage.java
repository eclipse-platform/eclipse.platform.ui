package org.eclipse.ant.ui.internal.toolscripts;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.ant.core.AntPlugin;
import org.eclipse.ant.core.toolscripts.*;
import org.eclipse.ant.core.toolscripts.AntBuilder;
import org.eclipse.ant.core.toolscripts.ToolScript;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
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
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @version 	1.0
 * @author
 */
public class AntBuilderPropertyPage extends PropertyPage {
	protected Table builderTable;
	protected Button upButton, downButton, addButton, removeButton;
	protected final ArrayList imagesToDispose = new ArrayList();
	protected Image antImage, builderImage;

/**
 * Constructor for AntBuilderPropertyPage.
 */
public AntBuilderPropertyPage() {
	super();
	noDefaultAndApplyButton();
}
protected void addBuildersToTable() {
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
 * Adds a build command to the table.
 * @param command the command to be added
 * @param position the insertion position, or -1 to add at the end
 * @param select whether to select the newly created item.
 */
protected void addCommand(ICommand command, int position, boolean select) {
	TableItem newItem;
	if (position < 0) {
		newItem = new TableItem(builderTable, SWT.NONE);
	} else {
		newItem = new TableItem(builderTable, SWT.NONE, position);
	}
	newItem.setData(command);
	String builderName = command.getBuilderName();
	if (builderName.equals(AntBuilder.ID)) {
		ToolScript script = ToolScript.createFromArguments(command.getArguments());
		newItem.setText(script.toString());
		switch (script.getKind()) {
			case ToolScript.KIND_ANT_SCRIPT:
				newItem.setImage(antImage);
				break;
			case ToolScript.KIND_EXTERNAL_COMMAND:
				Image image = imageForProgram(((ExternalToolScript)script).getCommand());
				newItem.setImage(image);
				break;
		}
	} else {
		newItem.setText(builderName);
		newItem.setImage(builderImage);
	}
	if (select) builderTable.setSelection(position);
}

/**
 * Configures and creates a new build command
 * that invokes a tool script.  Returns the new command,
 * or null if no command was created.
 */
protected ICommand createToolScript() {
	try {
		RunToolScriptDialog dialog = new RunToolScriptDialog(getShell());
		dialog.open();
		ToolScript script = dialog.getToolScript();
		if (script == null) {
			return null;
		}
		script.setRefreshContainer(getInputProject());
		ICommand command = getInputProject().getDescription().newCommand();
		command.setBuilderName(AntBuilder.ID);
		command.setArguments(script.createBuilderArguments());
		return command;
	} catch(CoreException e) {
		handleException(e);
		return null;
	}		
}
/**
 * Creates and returns a button with the given label, id, and enablement.
 */
protected Button createButton(Composite parent, String label) {
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

/*
 * @see PreferencePage#createContents(Composite)
 */
protected Control createContents(Composite parent) {
	antImage = AntUIPlugin.getPlugin().getImageDescriptor(AntUIPlugin.IMG_ANT_SCRIPT).createImage();
	builderImage = AntUIPlugin.getPlugin().getImageDescriptor(AntUIPlugin.IMG_BUILDER).createImage();
	imagesToDispose.add(antImage);
	imagesToDispose.add(builderImage);
	
	Composite topLevel = new Composite(parent, SWT.NONE);
	topLevel.setLayout(new GridLayout());
	topLevel.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	Label description = new Label(topLevel, SWT.WRAP);
	description.setText(Policy.bind("builderProperties.description"));
	description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	//table and button group -- have to do this way because of 1FUMNGH - GridLayout weakness
	Composite tableAndButtons = new Composite(topLevel, SWT.NONE);
	tableAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.numColumns = 2;
	tableAndButtons.setLayout(layout);

	//table of builders and scripts		
	builderTable = new Table(tableAndButtons, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
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
	upButton = createButton(buttonArea, "&Up");
	downButton = createButton(buttonArea, "&Down");
	addButton = createButton(buttonArea, "&Add...");
	removeButton = createButton(buttonArea, "&Remove");

	//populate widget contents	
	addBuildersToTable();
	
	return topLevel;
}
/**
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
 * or null.
 */
protected IProject getInputProject() {
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
protected void handleButtonPressed(Button button) {
	if (button == addButton) {
		ICommand newCommand = createToolScript();
		if (newCommand != null) {
			int insertPosition = builderTable.getSelectionIndex() + 1;
			addCommand(newCommand, insertPosition, true);
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
protected void handleException(Exception e) {
	//XXX cleanup exception handling
	IStatus status;
	if (e instanceof CoreException) {
		status = ((CoreException)e).getStatus();
	} else {
		status = new Status(IStatus.ERROR, AntPlugin.PI_ANT, 1, "An error occurred while running an ant script", e);
	}
	ErrorDialog.openError(getShell(), "Error Configuring Tool Scripts", "Unhandled exception configuring tool scripts", status);
}
/**
 * The user has selected a different builder table.  Update button enablement.
 */
protected void handleTableSelectionChanged() {
	addButton.setEnabled(true);
	TableItem[] items = builderTable.getSelection();
	if (items != null && items.length == 1) {
		TableItem item = items[0];
		ICommand buildCommand = (ICommand)item.getData();
		if (buildCommand.getBuilderName().equals(AntBuilder.ID)) {
			//ant builders can be removed
			removeButton.setEnabled(true);
			//ant builders can be reorganized
			int selection = builderTable.getSelectionIndex();
			int max = builderTable.getItemCount();
			upButton.setEnabled(selection != 0);
			downButton.setEnabled(selection < max-1);
			return;
		}
	}
	//in all other cases we can't do any of these.
	removeButton.setEnabled(false);
	upButton.setEnabled(false);
	downButton.setEnabled(false);
}
/**
 * Returns the image to use for the given command line
 * string.  Returns a default image if none could be computed.
 */
protected Image imageForProgram(String commandLine) {
	Image image = antImage;
	int firstSpace = commandLine.indexOf(' ');
	String name = firstSpace > 0 ? commandLine.substring(0, firstSpace) : commandLine;
	int lastDot = name.lastIndexOf('.');
	name = name.substring(lastDot+1);
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
protected void move(TableItem item, int index) {
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
protected void moveSelectionDown() {
	//Only do this operation on a single selection
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
protected void moveSelectionUp() {
	int currentIndex = builderTable.getSelectionIndex();
	//Only do this operation on a single selection
	if (currentIndex > 0 && builderTable.getSelectionCount() == 1) {
		move(builderTable.getItem(currentIndex), currentIndex-1);
		builderTable.setSelection(currentIndex-1);
	}
}
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
	/*
	 * @see IDialogPage#dispose()
	 */


	/*
	 * @see IPreferencePage#performOk()
	 */


}
