package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.externaltools.internal.dialog.ExternalToolVariableForm;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolVariable;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * Group of components applicable to most external tools. This group
 * will collect from the user the options:
 * <ul>
 * <li>log tool messages to console</li>
 * <li>run tool in background</li>
 * <li>open perspective on run</li>
 * <li>tool arguments</li>
 * <li>prompt for arguments on run</li>
 * </ul>
 * <p>
 * This group can be used or extended by clients.
 * </p>
 */
public class ExternalToolOptionGroup extends ExternalToolGroup {
	private boolean initialCaptureOutput = true;
	private boolean initialShowConsole = true;
	private boolean initialRunBackground = true;
	private String initialOpenPersp = null;
	private String initialArgument = ""; //$NON-NLS-1$
	private boolean initialPromptArg = false;
	private boolean initialShowInMenu = false;
	private boolean initialSaveDirtyEditors = false;
	
	private String promptArgLabel = null;
	
	protected Button captureOutputButton;
	protected Button showConsoleButton;
	protected Button runBackgroundButton;
	protected Button openPerspButton;
	protected Combo openPerspNameField;
	protected Text argumentField;
	protected Button promptArgButton;
	protected Button showInMenuButton;
	protected Button saveDirtyEditorsButton;
	private Button variableButton;
	
	private IPerspectiveDescriptor[] perspectives;
	
	/**
	 * Creates the group
	 */
	public ExternalToolOptionGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolGroup.
	 */
	protected Control createGroupContents(Composite parent, ExternalTool tool) {
		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createRunBackgroundComponent(mainComposite);
		createCaptureOutputComponent(mainComposite);
		createShowConsoleComponent(mainComposite);
		createOpenPerspComponent(mainComposite);
		createShowInMenuComponent(mainComposite);
		createSaveDirtyEditorsComponent(mainComposite);
		createSpacer(parent);
		createArgumentComponent(mainComposite);
		createPromptForArgumentComponent(mainComposite);
		createSpacer(parent);

		if (runBackgroundButton != null) {
			runBackgroundButton.setSelection(isEditMode() ? tool.getRunInBackground() : initialRunBackground);
		}
		
		if (captureOutputButton != null) {
			captureOutputButton.setSelection(isEditMode() ? tool.getCaptureOutput() : initialCaptureOutput);
		}
		
		if (showConsoleButton != null) {
			showConsoleButton.setSelection(isEditMode() ? tool.getShowConsole() : initialShowConsole);
		}
		
		if (openPerspButton != null) {
			String perspId = isEditMode() ? tool.getOpenPerspective() : initialOpenPersp;
			openPerspButton.setSelection(perspId != null);
		}
		
		if (openPerspNameField != null) {
			int index = -1;
			if (isEditMode())
				index = getPerspectiveIndex(tool.getOpenPerspective());
			else
				index = getPerspectiveIndex(initialOpenPersp);
			if (index != -1)
				openPerspNameField.select(index);
			updateOpenPerspNameField();
		}
		
		if (showInMenuButton != null) {
			showInMenuButton.setSelection(isEditMode() ? tool.getShowInMenu() : initialShowInMenu);	
		}
		
		if (saveDirtyEditorsButton != null) {
			saveDirtyEditorsButton.setSelection(isEditMode() ? tool.getSaveDirtyEditors() : initialSaveDirtyEditors);	
		}

		if (argumentField != null) {
			argumentField.setText(isEditMode() ? tool.getArguments() : initialArgument);
		}
		
		if (promptArgButton != null) {
			promptArgButton.setSelection(isEditMode() ? tool.getPromptForArguments() : initialPromptArg);
		}
		
		validate();

		return mainComposite;
	}
	
	/**
	 * Creates the controls needed to edit the argument and
	 * prompt for argument attributes of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createArgumentComponent(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(layout);
		comp.setLayoutData(data);

		Label label = new Label(comp, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolOptionGroup.argumentLabel")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		argumentField = new Text(comp, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		argumentField.setLayoutData(data);

		variableButton= new Button(comp, SWT.PUSH);
		variableButton.setText(ToolMessages.getString("ExternalToolOptionGroup.argumentVariableLabel")); //$NON-NLS-1$
		variableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleButtonPressed((Button)e.getSource());
			}
		});
		getPage().setButtonGridData(variableButton);

		Label instruction = new Label(comp, SWT.NONE);
		instruction.setText(ToolMessages.getString("ExternalToolOptionGroup.argumentInstruction")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		instruction.setLayoutData(data);
	}
	
	private void handleButtonPressed(Button button) {
		if (button == variableButton) {
			VariableSelectionDialog dialog= new VariableSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			if (dialog.open() == SelectionDialog.OK) {
				Object[] objects= dialog.getResult();
				argumentField.append(((ExternalToolVariable)objects[0]).getTag());
			}
		}
	}
	
	private class VariableSelectionDialog extends SelectionDialog {
		private VariableSelectionDialog(Shell parent) {
			super(parent);
			setTitle("Select variable");
		}
		protected Control createDialogArea(Composite parent) {
			// Create the dialog area
			Composite composite= (Composite)super.createDialogArea(parent);
			ExternalToolVariable[] variables= ExternalToolsPlugin.getDefault().getArgumentVariableRegistry().getArgumentVariables();
			ExternalToolVariableForm form= new ExternalToolVariableForm("Choose a variable", variables);
			form.createContents(composite, new IGroupDialogPage() {
				public GridData setButtonGridData(Button button) {
					GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
					data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
					int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
					data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
					button.setLayoutData(data);
					return data;
				}

				public void setMessage(String newMessage, int newType) {
					VariableSelectionDialog.this.setMessage(newMessage);
				}

				public void updateValidState() {
				}

				public int convertHeightHint(int chars) {
					return convertHeightInCharsToPixels(chars);
				}

				public String getMessage() {
					return VariableSelectionDialog.this.getMessage();
				}

				public int getMessageType() {
					return 0;
				}
			});
			return composite;
		}

	}
	
	/**
	 * Creates the controls needed to edit the show console
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createCaptureOutputComponent(Composite parent) {
		captureOutputButton = new Button(parent, SWT.CHECK);
		captureOutputButton.setText(ToolMessages.getString("ExternalToolOptionGroup.captureOutputLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		captureOutputButton.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the prompt for argument
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createPromptForArgumentComponent(Composite parent) {
		promptArgButton = new Button(parent, SWT.CHECK);
		if (promptArgLabel != null)
			promptArgButton.setText(promptArgLabel);
		else
			promptArgButton.setText(ToolMessages.getString("ExternalToolOptionGroup.promptArgLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		promptArgButton.setLayoutData(data);		
	}

	/**
	 * Creates the controls needed to edit the open perspective
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createOpenPerspComponent(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(layout);
		comp.setLayoutData(data);

		openPerspButton = new Button(comp, SWT.CHECK);
		openPerspButton.setText(ToolMessages.getString("ExternalToolOptionGroup.openPerspLabel")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		openPerspButton.setLayoutData(data);
		openPerspButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateOpenPerspNameField();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		openPerspNameField = new Combo(comp, (SWT.DROP_DOWN | SWT.READ_ONLY));
		openPerspNameField.setItems(getOpenPerspectiveNames());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		openPerspNameField.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the run in background
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createRunBackgroundComponent(Composite parent) {
		runBackgroundButton = new Button(parent, SWT.CHECK);
		runBackgroundButton.setText(ToolMessages.getString("ExternalToolOptionGroup.runBackgroundLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		runBackgroundButton.setLayoutData(data);
	}

	/**
	 * Creates the controls needed to edit the save dirty editors
	 * attribute of an external tool.
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createSaveDirtyEditorsComponent(Composite parent) {
		saveDirtyEditorsButton = new Button(parent, SWT.CHECK);
		saveDirtyEditorsButton.setText(ToolMessages.getString("ExternalToolOptionGroup.saveDirtyEditorsLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		saveDirtyEditorsButton.setLayoutData(data);		
	}

	/**
	 * Creates the controls needed to edit the show console
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createShowConsoleComponent(Composite parent) {
		showConsoleButton = new Button(parent, SWT.CHECK);
		showConsoleButton.setText(ToolMessages.getString("ExternalToolOptionGroup.showConsoleLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		showConsoleButton.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the show in menu
	 * attribute of an external tool.
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createShowInMenuComponent(Composite parent) {
		showInMenuButton = new Button(parent, SWT.CHECK);
		showInMenuButton.setText(ToolMessages.getString("ExternalToolOptionGroup.showInMenuLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		showInMenuButton.setLayoutData(data);
	}
	
	/**
	 * Creates a vertical space between controls.
	 */
	protected final void createSpacer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 1;
		label.setLayoutData(data);
	}

	/**
	 * Returns the proposed initial argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial argument when editing new tool.
	 */
	public final String getInitialArgument() {
		return initialArgument;
	}

	/**
	 * Returns the proposed initial capture output for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial capture output when editing new tool.
	 */
	public final boolean getInitialCaptureOutput() {
		return initialCaptureOutput;
	}
	
	/**
	 * Returns the proposed initial open perspective id for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial open perspective id when editing new tool.
	 */
	public final String getInitialOpenPerspective() {
		return initialOpenPersp;
	}

	/**
	 * Returns the proposed initial prompt for argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial prompt for argument when editing new tool.
	 */
	public final boolean getInitialPromptForArgument() {
		return initialPromptArg;
	}

	/**
	 * Returns the proposed initial run in background for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial run in background when editing new tool.
	 */
	public final boolean getInitialRunInBackground() {
		return initialRunBackground;
	}

	/**
	 * Returns the proposed initial save dirty editors for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial save dirty editors when editing new tool.
	 */	
	public final boolean getInitialSaveDirtyEditors() {
		return initialSaveDirtyEditors;
	}

	/**
	 * Returns the proposed initial show console for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial show console when editing new tool.
	 */
	public final boolean getInitialShowConsole() {
		return initialShowConsole;
	}
	
	/**
	 * Returns the proposed initial show in menu for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial show in menu when editing new tool.
	 */	
	public final boolean getInitialShowInMenu() {
		return initialShowInMenu;
	}

	/**
	 * Returns the list of perspective names to place in
	 * the open perspective combo box. This list contains
	 * all the available perspectives in the workbench.
	 */
	protected final String[] getOpenPerspectiveNames() {
		String[] names = new String[getPerspectives().length];

		for (int i = 0; i < getPerspectives().length; i++) {
			names[i] = getPerspectives()[i].getLabel();
		}
		
		return names;
	}

	/**
	 * Returns the ID for the perspective in the combo box
	 * at the specified index, or <code>null</code> if
	 * none.
	 */
	protected final String getPerspectiveId(int index) {
		if (index < 0 || index > getPerspectives().length)
			return null;
		return getPerspectives()[index].getId();
	}
		
	/**
	 * Returns the index in the perspective combo that
	 * matches the specified perspective ID, or -1 if
	 * none found.
	 */
	protected final int getPerspectiveIndex(String perspId) {
		if (perspId == null)
			return -1;

		for (int i = 0; i < getPerspectives().length; i++) {
			if (perspId.equals(getPerspectives()[i].getId()))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Returns the list of perspectives known to the workbench.
	 * The list is also sorted by name in alphabetical order.
	 */
	protected final IPerspectiveDescriptor[] getPerspectives() {
		if (perspectives == null) {
			perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
			Arrays.sort(perspectives, new Comparator() {
				private Collator collator = Collator.getInstance();
			
				public int compare(Object o1, Object o2) {
					String name1 = ((IPerspectiveDescriptor)o1).getLabel();
					String name2 = ((IPerspectiveDescriptor)o2).getLabel();
					return collator.compare(name1, name2);
				}
			});
		}
		return perspectives;
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void restoreValues(ExternalTool tool) {
		if (runBackgroundButton != null)
			runBackgroundButton.setSelection(tool.getRunInBackground());
		if (captureOutputButton != null)
			captureOutputButton.setSelection(tool.getCaptureOutput());
		if (showConsoleButton != null)
			showConsoleButton.setSelection(tool.getShowConsole());
		if (showInMenuButton != null)
			showInMenuButton.setSelection(tool.getShowInMenu());
		if (saveDirtyEditorsButton != null)
			saveDirtyEditorsButton.setSelection(tool.getSaveDirtyEditors());
		if (openPerspButton != null)
			openPerspButton.setSelection(tool.getOpenPerspective() != null);
		if (openPerspNameField != null) {
			int index = getPerspectiveIndex(tool.getOpenPerspective());
			if (index != -1)
				openPerspNameField.select(index);
		}
		updateOpenPerspNameField();
		if (argumentField != null)
			argumentField.setText(tool.getArguments());
		if (promptArgButton != null)
			promptArgButton.setSelection(tool.getPromptForArguments());
	}
	
	/**
	 * Sets the proposed initial argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialArgument the proposed initial argument when editing new tool.
	 */
	public final void setInitialArgument(String initialArgument) {
		if (initialArgument != null)
			this.initialArgument = initialArgument;
	}

	/**
	 * Sets the proposed initial capture output for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialCaptureOutput the proposed initial capture output when editing new tool.
	 */
	public final void setInitialCaptureOutput(boolean initialCaptureOutput) {
		this.initialCaptureOutput = initialCaptureOutput;
	}
	
	/**
	 * Sets the proposed initial open perspective id for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialOpenPersp the proposed initial open perspective id when editing new tool.
	 */
	public final void setInitialOpenPerspective(String initialOpenPersp) {
		this.initialOpenPersp = initialOpenPersp;
	}

	/**
	 * Sets the proposed initial prompt for argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialPromptArg the proposed initial prompt for argument when editing new tool.
	 */
	public final void setInitialPromptForArgument(boolean initialPromptArg) {
		this.initialPromptArg = initialPromptArg;
	}
	
	/**
	 * Sets the proposed initial run in background for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialRunBackground the proposed initial run in background when editing new tool.
	 */
	public final void setInitialRunInBackground(boolean initialRunBackground) {
		this.initialRunBackground = initialRunBackground;
	}

	/**
	 * Sets the proposed initial save dirty editors for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialSaveDirtyEditors the proposed save dirty editors when editing new tool.
	 */
	public final void setInitialSaveDirtyEditors(boolean initialSaveDirtyEditors) {
		this.initialSaveDirtyEditors = initialSaveDirtyEditors;
	}

	/**
	 * Sets the proposed initial show console for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialShowConsole the proposed initial show console when editing new tool.
	 */
	public final void setInitialShowConsole(boolean initialShowConsole) {
		this.initialShowConsole = initialShowConsole;
	}
	
	/**
	 * Sets the proposed initial show in menu for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialShowInMenu the proposed show in menu when editing new tool.
	 */
	public final void setInitialShowInMenu(boolean initialShowInMenu) {
		this.initialShowInMenu = initialShowInMenu;
	}

	/**
	 * Sets the label for the prompt for argument option
	 * of the tool. Does nothing if passed <code>null</code>.
	 */
	public final void setPromptForArgumentLabel(String label) {
		if (label == null)
			promptArgLabel = null;
		else {
			promptArgLabel = label.trim();
			if (promptArgLabel.length() == 0)
				promptArgLabel = null;
		}
	}

	/**
	 * Updates the enablement state of the open perspective
	 * combo box if required.
	 */
	protected void updateOpenPerspNameField() {
		if (openPerspNameField != null && openPerspButton != null)
			openPerspNameField.setEnabled(openPerspButton.getSelection());
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void updateTool(ExternalTool tool) {
		if (runBackgroundButton != null)
			tool.setRunInBackground(runBackgroundButton.getSelection());
		if (captureOutputButton != null)
			tool.setCaptureOutput(captureOutputButton.getSelection());
		if (showConsoleButton != null)
			tool.setShowConsole(showConsoleButton.getSelection());
		if (showInMenuButton != null)
			tool.setShowInMenu(showInMenuButton.getSelection());
		if (saveDirtyEditorsButton != null)
			tool.setSaveDirtyEditors(saveDirtyEditorsButton.getSelection());
		if (openPerspButton != null && openPerspNameField != null) {
			if (openPerspButton.getSelection())
				tool.setOpenPerspective(getPerspectiveId(openPerspNameField.getSelectionIndex()));
			else
				tool.setOpenPerspective(null);
		}
		if (argumentField != null)
			tool.setArguments(argumentField.getText().trim());
		if (promptArgButton != null)
			tool.setPromptForArguments(promptArgButton.getSelection());
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void validate() {
		// do nothing
	}
}
