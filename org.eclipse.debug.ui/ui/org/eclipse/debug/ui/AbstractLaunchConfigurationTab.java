/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Common function for launch configuration tabs.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @see ILaunchConfigurationTab
 * @since 2.0
 */
public abstract class AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {
	
	/**
	 * The control for this page, or <code>null</code>
	 */
	private Control fControl;

	/**
	 * The launch configuration dialog this tab is
	 * contained in.
	 */
	private ILaunchConfigurationDialog fLaunchConfigurationDialog;
	
	/**
	 * Current error message, or <code>null</code>
	 */
	private String fErrorMessage;
	
	/**
	 * Current message, or <code>null</code>
	 */
	private String fMessage;
	
	/**
	 * Whether this tab needs to apply changes. This attribute is initialized to
	 * <code>true</code> to be backwards compatible. If clients want to take advantage
	 * of such a feature, they should set the flag to false, and check it before
	 * applying changes to the launch configuration working copy.
	 * 
	 * @since 2.1
	 */
	private boolean fDirty = true;	
		
	/**
	 * Returns the dialog this tab is contained in, or
	 * <code>null</code> if not yet set.
	 * 
	 * @return launch configuration dialog, or <code>null</code>
	 */
	protected ILaunchConfigurationDialog getLaunchConfigurationDialog() {
		return fLaunchConfigurationDialog;
	}	
		
	/**
	 * Updates the buttons and message in this page's launch
	 * configuration dialog.
	 */
	protected void updateLaunchConfigurationDialog() {
		if (getLaunchConfigurationDialog() != null) {
			//order is important here due to the call to 
			//refresh the tab viewer in updateButtons()
			//which ensures that the messages are up to date
			getLaunchConfigurationDialog().updateButtons();
			getLaunchConfigurationDialog().updateMessage();
		}
	}
				
	/**
	 * @see ILaunchConfigurationTab#getControl()
	 */
	public Control getControl() {
		return fControl;
	}

	/**
	 * Sets the control to be displayed in this tab.
	 * 
	 * @param control the control for this tab
	 */
	protected void setControl(Control control) {
		fControl = control;
	}

	/**
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}

	/**
	 * @see ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}

	/**
	 * By default, do nothing.
	 * 
	 * @see ILaunchConfigurationTab#launched(ILaunch)
	 * @deprecated
	 */
	public void launched(ILaunch launch) {
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		fLaunchConfigurationDialog = dialog;
	}
	
	/**
	 * Sets this page's error message, possibly <code>null</code>.
	 * 
	 * @param errorMessage the error message or <code>null</code>
	 */
	protected void setErrorMessage(String errorMessage) {
		fErrorMessage = errorMessage;
	}

	/**
	 * Sets this page's message, possibly <code>null</code>.
	 * 
	 * @param message the message or <code>null</code>
	 */
	protected void setMessage(String message) {
		fMessage = message;
	}
	
	/**
	 * Convenience method to return the launch manager.
	 * 
	 * @return the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}	
	
	/**
	 * By default, do nothing.
	 * 
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Returns the shell this tab is contained in, or <code>null</code>.
	 * 
	 * @return the shell this tab is contained in, or <code>null</code>
	 */
	protected Shell getShell() {
		Control control = getControl();
		if (control != null) {
			return control.getShell();
		}
		return null;
	}
	
	/**
	 * Creates and returns a new push button with the given
	 * label and/or image.
	 * 
	 * @param parent parent control
	 * @param label button label or <code>null</code>
	 * @param image image of <code>null</code>
	 * 
	 * @return a new push button
	 */
	protected Button createPushButton(Composite parent, String label, Image image) {
		return SWTUtil.createPushButton(parent, label, image);	
	}
	
	/**
	 * Creates and returns a new radio button with the given
	 * label and/or image.
	 * 
	 * @param parent parent control
	 * @param label button label or <code>null</code>
	 * 
	 * @return a new radio button
	 */
	protected Button createRadioButton(Composite parent, String label) {
		return SWTUtil.createRadioButton(parent, label);	
	}	
	
	/**
	 * Creates and returns a new check button with the given
	 * label.
	 * 
	 * @param parent the parent composite
	 * @param label the button label
	 * @return a new check button
	 * @since 3.0
	 */
	protected Button createCheckButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		button.setFont(parent.getFont());
		SWTUtil.setButtonDimensionHint(button);
		return button;
	}
	
	/**
	 * @see ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return true;
	}
	
	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return true;
	}

	/**
	 * Create some empty space.
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
		label.setFont(comp.getFont());
	}	
	
	/**
	 * Create a horizontal separator.
	 * 
	 * @param comp parent widget
	 * @param colSpan number of columns to span
	 * @since 3.0
	 */
	protected void createSeparator(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}	
		
	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * Convenience method to set a boolean attribute of on a launch
	 * configuration. If the value being set is the default, the attribute's
	 * value is set to <code>null</code>.
	 * 
	 * @param attribute attribute identifier
	 * @param configuration the configuration on which to set the attribute
	 * @param value the value of the attribute
	 * @param defaultValue the default value of the attribute
	 * @since 2.1
	 */
	protected void setAttribute(String attribute, ILaunchConfigurationWorkingCopy configuration, boolean value, boolean defaultValue) {
		if (value == defaultValue) {
			configuration.setAttribute(attribute, (String)null);
		} else {
			configuration.setAttribute(attribute, value);
		}
	}



	/**
	 * Returns whether this tab is dirty. It is up to clients to set/reset and consult
	 * this attribute as required. By default, a tab is initialized to dirty.
	 * 
	 * @return whether this tab is dirty
	 * @since 2.1
	 */
	protected boolean isDirty() {
		return fDirty;
	}

	/**
	 * Returns whether this tab is dirty. It is up to clients to set/reset and consult
	 * this attribute as required. By default, a tab is initialized to dirty.
	 * 
	 * @param dirty whether this tab is dirty
	 * @since 2.1
	 */
	protected void setDirty(boolean dirty) {
		fDirty = dirty;
	}
	
	/**
	 * This method was added to the <code>ILaunchConfigurationTab</code> interface
	 * in the 3.0 release to allow tabs to distinguish between a tab being activated
	 * and a tab group be initialized for the first time, from a selected launch
	 * configuration. To maintain backwards compatible behavior, the default
	 * implementation provided, calls this tab's <code>initializeFrom</code> method.
	 * Tabs should override this method as required.
	 * <p>
	 * The launch tab framework was originially designed to take care of inter tab
	 * communication by applying attributes from the active tab to the launch configuration
	 * being edited, when a tab is exited, and by initializing a tab when activated.
	 * The addition of the methods <code>activated</code> and <code>deactivated</code>
	 * allow tabs to determine the appropriate course of action. 
	 * </p>
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 * @since 3.0
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		initializeFrom(workingCopy);
	}

	/**
	 * This method was added to the <code>ILaunchConfigurationTab</code> interface
	 * in the 3.0 release to allow tabs to distinguish between a tab being deactivated
	 * and saving its attributes to a launch configuration. To maintain backwards
	 * compatible behavior, the default implementation provided, calls this tab's
	 * <code>performApply</code> method. Tabs should override this method as required.
	 * <p>
	 * The launch tab framework was originially designed to take care of inter tab
	 * communication by applying attributes from the active tab to the launch configuration
	 * being edited, when a tab is exited, and by initializing a tab when activated.
	 * The addition of the methods <code>activated</code> and <code>deactivated</code>
	 * allow tabs to determine the appropriate course of action. 
	 * </p>
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 * @since 3.0
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		performApply(workingCopy);
	}

}

