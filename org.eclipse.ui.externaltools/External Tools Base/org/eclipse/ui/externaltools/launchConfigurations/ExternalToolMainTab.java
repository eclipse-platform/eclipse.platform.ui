package org.eclipse.ui.externaltools.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

public class ExternalToolMainTab extends AbstractLaunchConfigurationTab {

	protected Text locationField;
	protected Text workDirectoryField;
	protected Text nameField;
	protected Text descriptionField;
	private Button fileLocationButton;
	private Button workspaceLocationButton;
	private Button fileWorkingDirectoryButton;
	private Button workspaceWorkingDirectoryButton;
	
	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			//validate();
		}
	};

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createNameComponent(mainComposite);
		createSpacer(parent);
		createDescriptionComponent(mainComposite);
	}
	
	/**
	 * Creates the controls needed to edit the name
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createNameComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Name:");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		nameField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		nameField.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the description
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createDescriptionComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Description:");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		descriptionField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		descriptionField.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the location
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createLocationComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Location: ");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		locationField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.verticalSpan= 2;
		locationField.setLayoutData(data);
		
		workspaceLocationButton= createPushButton(parent, "Browse Workspace...", null);
		fileLocationButton= createPushButton(parent, "Browse File System...", null);
	}
	
	/**
	 * Creates the controls needed to edit the working directory
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Working Directory:");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		workDirectoryField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.verticalSpan= 2;
		workDirectoryField.setLayoutData(data);
		
		workspaceWorkingDirectoryButton= createPushButton(parent, "Browse Workspace...", null);
		fileWorkingDirectoryButton= createPushButton(parent, "Browse File System...", null);

		createSpacer(parent);
	}
	
	/**
	 * Creates a vertical space between controls.
	 */
	protected void createSpacer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateLocation(configuration);
		updateWorkingDirectory(configuration);
		updateName(configuration);
		updateDescription(configuration);
	}
	/**
	 * Method updateDescription.
	 * @param configuration
	 */
	private void updateDescription(ILaunchConfiguration configuration) {
		String desc= "";
		try {
			desc= configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_DESCRIPTION, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		descriptionField.setText(desc);
		descriptionField.addModifyListener(modifyListener);
	}
	
	private void updateName(ILaunchConfiguration configuration) {
		String name= configuration.getName();
		nameField.setText(name);
	}
	
	private void updateWorkingDirectory(ILaunchConfiguration configuration) {
		String workingDir= "";
		try {
			workingDir= configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		workDirectoryField.setText(workingDir);
		workDirectoryField.addModifyListener(modifyListener);
		
	}
	
	private void updateLocation(ILaunchConfiguration configuration) {
		String location= "";
		try {
			location= configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		locationField.setText(location);
		locationField.addModifyListener(modifyListener);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String location= locationField.getText().trim();
		if (location.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		}
		
		String workingDirectory= workDirectoryField.getText().trim();
		if (workingDirectory.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		}
		
		String desc= descriptionField.getText().trim();
		if (desc.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_DESCRIPTION, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_DESCRIPTION, desc);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Main";
	}
	
	/**
	 * @see ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return false;
	}
}
