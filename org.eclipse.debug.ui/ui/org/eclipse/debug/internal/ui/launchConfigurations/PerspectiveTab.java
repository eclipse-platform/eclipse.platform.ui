package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;

public class PerspectiveTab implements ILaunchConfigurationTab {

	/**
	 * The dialog this tab is being displayed in.
	 */
	private ILaunchConfigurationDialog fDialog;
	
	/**
	 * @see ILaunchConfigurationTab#createTabControl(TabItem)
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem) {
		setLaunchDialog(dialog);
		
		Composite comp = new Composite(tabItem.getParent(), SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 3;
		comp.setLayout(topLayout);		
		GridData gd;
		
		Label label = new Label(comp, SWT.HORIZONTAL | SWT.LEFT);
		label.setText("Open/switch perspective when lanuched:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		
		Button button = new Button(comp, SWT.CHECK);
		button.setText("Run Mode:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		button.setLayoutData(gd);
		
		Combo combo = new Combo(comp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		combo.setLayoutData(gd);
		
		button = new Button(comp, SWT.CHECK);
		button.setText("Debug Mode:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		button.setLayoutData(gd);
		
		combo = new Combo(comp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		combo.setLayoutData(gd);				
				
		label = new Label(comp, SWT.HORIZONTAL | SWT.LEFT);
		label.setText("When a thread suspends:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);				
		
		button = new Button(comp, SWT.CHECK);
		button.setText("Show Debug View in perspective:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		button.setLayoutData(gd);
		
		combo = new Combo(comp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		combo.setLayoutData(gd);
				
		return comp;
	}

	/*
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
	}

	/*
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Sets the dialog this tab is being displayed in
	 * 
	 * @param dialog launch configuration dialog
	 */
	private void setLaunchDialog(ILaunchConfigurationDialog dialog) {
		fDialog = dialog;
	}
	
	/**
	 * Returns the dialog this tab is being displayed in
	 * 
	 * @return the dialog this tab is being displayed in
	 */
	protected ILaunchConfigurationDialog getLaunchDialog() {
		return fDialog;
	}	
}

