package org.eclipse.ui.externaltools.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
	protected Text argumentField;
	protected Button promptArgButton;
	private Button variableButton;
	
	private SelectionAdapter selectionAdapter;
	
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
		updatePromptForArgument(configuration);
		updateRunBackground(configuration);
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
		
		String arguments= argumentField.getText().trim();
		if (arguments.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
		}
		
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
