package org.eclipse.ui.externaltools.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.externaltools.group.IGroupDialogPage;
import org.eclipse.ui.externaltools.internal.dialog.ExternalToolVariableForm;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolVariable;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

public class ExternalToolsOptionTab extends AbstractLaunchConfigurationTab {

	String promptArgLabel= null;
	
	protected Button runBackgroundButton;
	protected Button openPerspButton;
	protected Combo openPerspNameField;
	protected Text argumentField;
	protected Button promptArgButton;
	protected Button showInMenuButton;
	protected Button saveModifiedButton;
	private Button variableButton;
	
	private SelectionAdapter selectionAdapter;
	
	private IPerspectiveDescriptor[] perspectives;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createVerticalSpacer(mainComposite, 1);
		createRunBackgroundComponent(mainComposite);
		createOpenPerspComponent(mainComposite);
		createShowInMenuComponent(mainComposite);
		createSaveDirtyEditorsComponent(mainComposite);
		createVerticalSpacer(parent, 1);
		createArgumentComponent(mainComposite);
		createPromptForArgumentComponent(mainComposite);
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
		label.setText("Arguments: ");
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		argumentField = new Text(comp, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		argumentField.setLayoutData(data);
		argumentField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		variableButton= createPushButton(comp, "Variables...", null);
		variableButton.setText("Variables...");
		variableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				VariableSelectionDialog dialog= new VariableSelectionDialog(getShell());
				if (dialog.open() == SelectionDialog.OK) {
					argumentField.append(dialog.getForm().getSelectedVariable());
				}
			}
		});

		Label instruction = new Label(comp, SWT.NONE);
		instruction.setText("Note: Enclose an argument containing spaces using double-quotes (\"). Not applicable for variables.");
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		instruction.setLayoutData(data);
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
		openPerspButton.setText("Open perspective when tool is run:");
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		openPerspButton.setLayoutData(data);
		openPerspButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openPerspNameField.setEnabled(openPerspButton.getSelection());
				updateLaunchConfigurationDialog();
			}
		});

		openPerspNameField = new Combo(comp, (SWT.DROP_DOWN | SWT.READ_ONLY));
		openPerspNameField.setItems(getOpenPerspectiveNames());
		data = new GridData(GridData.FILL_HORIZONTAL);
		openPerspNameField.setLayoutData(data);
		openPerspNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
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

	/**
	 * Creates the controls needed to edit the prompt for argument
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createPromptForArgumentComponent(Composite parent) {
		promptArgButton = new Button(parent, SWT.CHECK);
		if (promptArgLabel != null) {
			promptArgButton.setText(promptArgLabel);
		} else {
			promptArgButton.setText("Prompt for arguments before running tool"); //$NON-NLS-1$
		}
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		promptArgButton.setLayoutData(data);
		promptArgButton.addSelectionListener(getSelectionAdapter());
	}
	
	/**
	 * Creates the controls needed to edit the run in background
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createRunBackgroundComponent(Composite parent) {
		runBackgroundButton = new Button(parent, SWT.CHECK);
		runBackgroundButton.setText("Run tool in background");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		runBackgroundButton.setLayoutData(data);
		runBackgroundButton.addSelectionListener(getSelectionAdapter());
	}
	
	/**
	 * Creates the controls needed to edit the save dirty editors
	 * attribute of an external tool.
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createSaveDirtyEditorsComponent(Composite parent) {
		saveModifiedButton= new Button(parent, SWT.CHECK);
		saveModifiedButton.setText("Save all modified resources automatically before running tool");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		saveModifiedButton.setLayoutData(data);
		saveModifiedButton.addSelectionListener(getSelectionAdapter());
	}
	
	/**
	 * Creates the controls needed to edit the show in menu
	 * attribute of an external tool.
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createShowInMenuComponent(Composite parent) {
		showInMenuButton = new Button(parent, SWT.CHECK);
		showInMenuButton.setText("Show in Run->External Tools menu");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		showInMenuButton.setLayoutData(data);
		showInMenuButton.addSelectionListener(getSelectionAdapter());	
	}
	/**
	 * Method getSelectionAdapter.
	 * @return SelectionListener
	 */
	private SelectionListener getSelectionAdapter() {
		if (selectionAdapter == null) {
			selectionAdapter= new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}
			};
		}
		return selectionAdapter;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, true);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateArgument(configuration);
		updateOpenPrespective(configuration);
		updatePromptForArgument(configuration);
		updateRunBackground(configuration);
		updateSaveModified(configuration);
		updateShowInMenu(configuration);
	}
	
	private void updateShowInMenu(ILaunchConfiguration configuration) {
		boolean  show= true;
		try {
			show= configuration.getAttribute(IExternalToolConstants.ATTR_SHOW_IN_EXTERNAL_TOOLS_MENU, false);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		
		showInMenuButton.setSelection(show);
	}

	private void updateSaveModified(ILaunchConfiguration configuration) {
		boolean  save= false;
		try {
			save= configuration.getAttribute(IExternalToolConstants.ATTR_SAVE_DIRTY_EDITORS, false);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		saveModifiedButton.setSelection(save);
	}
	
	private void updatePromptForArgument(ILaunchConfiguration configuration) {
		boolean  prompt= false;
		try {
			prompt= configuration.getAttribute(IExternalToolConstants.ATTR_PROMPT_FOR_ARGUMENTS, false);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		promptArgButton.setSelection(prompt);
	}
	
	private void updateOpenPrespective(ILaunchConfiguration configuration) {
		String perspective= null;
		try {
			perspective= configuration.getAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		
		openPerspButton.setSelection(perspective != null);
		openPerspNameField.setEnabled(perspective != null);
		int index = getPerspectiveIndex(perspective);
		if (index != -1) {
			openPerspNameField.select(index);
		}
	}
	
	/**
	 * Returns the index in the perspective combo that
	 * matches the specified perspective ID, or -1 if
	 * none found.
	 */
	protected final int getPerspectiveIndex(String perspId) {
		if (perspId == null) {
			return -1;
		}

		for (int i = 0; i < getPerspectives().length; i++) {
			if (perspId.equals(getPerspectives()[i].getId())) {
				return i;
			}
		}
		
		return -1;
	}
	
	private void updateArgument(ILaunchConfiguration configuration) {
		String arguments= "";
		try {
			arguments= configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		argumentField.setText(arguments);
	}

	private void updateRunBackground(ILaunchConfiguration configuration) {
		boolean  runInBackgroud= true;
		try {
			runInBackgroud= configuration.getAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, true);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		runBackgroundButton.setSelection(runInBackgroud);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		setAttribute(IExternalToolConstants.ATTR_PROMPT_FOR_ARGUMENTS, configuration, promptArgButton.getSelection(), false);
		setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, configuration, runBackgroundButton.getSelection(), false);
		setAttribute(IExternalToolConstants.ATTR_SAVE_DIRTY_EDITORS, configuration, saveModifiedButton.getSelection(), false);
		setAttribute(IExternalToolConstants.ATTR_SHOW_IN_EXTERNAL_TOOLS_MENU, configuration, showInMenuButton.getSelection(), false);
		
		if (openPerspButton.getSelection()) {
			configuration.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, getPerspectiveId(openPerspNameField.getSelectionIndex()));
		} else {
			configuration.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, (String)null);
		}
		
		String arguments= argumentField.getText().trim();
		if (arguments.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
		}
		
	}
	
	/**
	 * Returns the ID for the perspective in the combo box
	 * at the specified index, or <code>null</code> if
	 * none.
	 */
	protected final String getPerspectiveId(int index) {
		if (index < 0 || index > getPerspectives().length) {
			return null;
		}
		return getPerspectives()[index].getId();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Options";
	}
	
	private class VariableSelectionDialog extends SelectionDialog {
		private ExternalToolVariableForm form;
		private VariableSelectionDialog(Shell parent) {
			super(parent);
			setTitle("Select variable");
		}
		protected Control createDialogArea(Composite parent) {
			// Create the dialog area
			Composite composite= (Composite)super.createDialogArea(parent);
			ExternalToolVariable[] variables= ExternalToolsPlugin.getDefault().getArgumentVariableRegistry().getArgumentVariables();
			form= new ExternalToolVariableForm("Choose a variable:", variables);
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
					if (!form.isValid()) {
						return "Invalid selection";
					}
					return null;
				}

				public int getMessageType() {
					if (!form.isValid()) {
						return IMessageProvider.ERROR;
					}
					return 0;
				}
			});
			return composite;
		}
		
		private ExternalToolVariableForm getForm() {
			return form;
		}
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages.getImage(IExternalToolConstants.IMG_TAB_OPTIONS);
	}	
}
