package org.eclipse.ui.externaltools.launchConfigurations;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

public class ExternalToolsBuilderTab extends AbstractLaunchConfigurationTab {

	private Button fullBuildButton;
	private Button autoBuildButton;
	private Button incrementalBuildButton;

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createBuildScheduleComponent(mainComposite);
	}
	
	private void createBuildScheduleComponent(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText("Run this builder for:");
		fullBuildButton= createButton(parent, "&Full builds", "Runs whenever a full build is invoked. The \"Rebuild All\" action causes a full build.");
		incrementalBuildButton= createButton(parent, "&Incremental builds", "Runs whenever an incremental build is invoked. The \"Build All\" action causes an incremental build.");
		autoBuildButton= createButton(parent, "&Auto builds (Not recommended)", "Runs whenever a resource in the workspace is modified. Enabling this option will cause the builder to run very often and is not recommended.");
	}
	
	/**
	 * Creates a check button in the given composite with the given text
	 */
	private Button createButton(Composite parent, String text, String tooltipText) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(text);
		button.setToolTipText(tooltipText);
		return button;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		fullBuildButton.setSelection(true);
		incrementalBuildButton.setSelection(true);
		autoBuildButton.setSelection(false);
	}

	/**
	 * Sets the state of the widgets from the given configuration
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		String buildKindString= null;
		try {
			buildKindString= configuration.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, "");
		} catch (CoreException e) {
		}
		int buildTypes[]= ExternalToolBuilder.buildTypesToArray(buildKindString);
		for (int i = 0; i < buildTypes.length; i++) {
			switch (buildTypes[i]) {
				case IncrementalProjectBuilder.FULL_BUILD:
					fullBuildButton.setSelection(true);
					break;
				case IncrementalProjectBuilder.INCREMENTAL_BUILD:
					incrementalBuildButton.setSelection(true);
					break;
				case IncrementalProjectBuilder.AUTO_BUILD:
					autoBuildButton.setSelection(true);
					break;
			}
		}
	}

	/**
	 * Stores the settings from the dialog into the launch configuration
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		StringBuffer buffer= new StringBuffer();
		if (fullBuildButton.getSelection()) {
			buffer.append(IExternalToolConstants.BUILD_TYPE_FULL).append(',');
		} 
		if (incrementalBuildButton.getSelection()){
			buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL).append(','); 
		} 
		if (autoBuildButton.getSelection()) {
			buffer.append(IExternalToolConstants.BUILD_TYPE_AUTO).append(',');
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Build Options";
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
	}

}
