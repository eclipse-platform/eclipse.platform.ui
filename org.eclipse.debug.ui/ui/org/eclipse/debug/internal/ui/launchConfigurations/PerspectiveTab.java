package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;

public class PerspectiveTab implements ILaunchConfigurationTab {

	/**
	 * The dialog this tab is being displayed in.
	 */
	private ILaunchConfigurationDialog fDialog;
	
	/**
	 * The current launch configuration being displayed
	 */
	private ILaunchConfigurationWorkingCopy fLaunchConfiguration;
	
	/**
	 * The combo box specifying the run perspective
	 */
	private Combo fRunCombo;
	
	/**
	 * The combo box specifying the debug perspective
	 */
	private Combo fDebugCombo;
	
	/**
	 * The check box specifying whether to use the run perspective
	 */
	private Button fRunPerspectiveButton;
	
	/**
	 * The check box specifying whether to use the debug perspective
	 */
	private Button fDebugPerspectiveButton;	
	
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
		
		createVerticalSpacer(comp, 3);

		Label label = new Label(comp, SWT.HORIZONTAL | SWT.LEFT);
		label.setText("Open/switch perspective when launched:");
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
		setRunPerspectiveButton(button);
		
		Combo combo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		combo.setLayoutData(gd);
		fillWithPerspectives(combo);
		setRunCombo(combo);
		
		button = new Button(comp, SWT.CHECK);
		button.setText("Debug Mode:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		button.setLayoutData(gd);
		setDebugPerspectiveButton(button);
		
		combo = new Combo(comp, SWT.DROP_DOWN |SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		combo.setLayoutData(gd);
		fillWithPerspectives(combo);				
		setDebugCombo(combo);
				
		initializeEventHandling();
		
		return comp;
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
		
		try {
			// initialize the run perspective setting
			initializeButtonAndCombo(getRunPerspectiveButton(), getRunCombo(),
				launchConfiguration.getAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, null));
				
			// initialize the debug perspective setting
			initializeButtonAndCombo(getDebugPerspectiveButton(), getDebugCombo(),
				launchConfiguration.getAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, null));
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(getShell(), "Error", "Unable to initialize Perspective tab.", e.getStatus());
		}
	}
	
	/**
	 * Based in the given perspective identifier, initialize the settings
	 * of the button and associated combo box. The check box is selected
	 * when there is a valid perspective, and the combo box is set to
	 * display the label of the associated perspective. The check box is
	 * deselected, and the combo box is set to the default value (debug
	 * perspective) when the identfier is <code>null</code>.
	 * 
	 * @param button check box button
	 * @param combo combo box with perspective labels
	 * @param id perspective identifier or <code>null</code>
	 */
	protected void initializeButtonAndCombo(Button button, Combo combo, String id) {
		if (id == null) {
			button.setSelection(false);
			combo.setText(getPerspectiveWithId(IDebugUIConstants.ID_DEBUG_PERSPECTIVE).getLabel());
		} else {
			button.setSelection(true);
			IPerspectiveDescriptor pd = getPerspectiveWithId(id);
			if (pd == null) {
				// perpective does not exist - reset
				initializeButtonAndCombo(button, combo, null);
			} else {
				combo.setText(pd.getLabel());
			}
		}
	}

	/**
	 * Returns the launch configuration currently being displayed.
	 */
	protected ILaunchConfigurationWorkingCopy getLaunchConfiguration() {
		return fLaunchConfiguration;
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
	
	/**
	 * Fills the given combo box with the labels of all existing
	 * perspectives.
	 * 
	 * @param combo combo box
	 */
	protected void fillWithPerspectives(Combo combo) {
		IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] persps = reg.getPerspectives();
		for (int i = 0; i < persps.length; i++) {
			combo.add(persps[i].getLabel());
		}
	}
	
	/**
	 * Returns the perspective with the given label, or
	 * <code>null</code> if none is found.
	 * 
	 * @param label perspective label
	 * @return perspective descriptor
	 */
	protected IPerspectiveDescriptor getPerspectiveWithLabel(String label) {		
		return PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithLabel(label);
	}
	
	/**
	 * Returns the perspective with the given id, or
	 * <code>null</code> if none is found.
	 * 
	 * @param id perspective identifier
	 * @return perspective descriptor
	 */
	protected IPerspectiveDescriptor getPerspectiveWithId(String id) {		
		return PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id);
	}	
	/**
	 * Returns the perspective combo assoicated with the
	 * debug perspective button.
	 * 
	 * @return a combo box
	 */
	protected Combo getDebugCombo() {
		return fDebugCombo;
	}

	/**
	 * Sets the perspective combo assoicated with the
	 * debug perspective button.
	 * 
	 * @param combo a combo box
	 */
	private void setDebugCombo(Combo combo) {
		fDebugCombo = combo;
	}

	/**
	 * Returns the check box indicating whether the perspective should
	 * be changed when the configuration is launched in debug mode.
	 * 
	 * @return a check box button
	 */
	protected Button getDebugPerspectiveButton() {
		return fDebugPerspectiveButton;
	}

	/**
	 * Sets the check box indicating whether the perspective should
	 * be changed when the configuration is launched in debug mode.
	 * 
	 * @param button a check box button
	 */
	private void setDebugPerspectiveButton(Button button) {
		fDebugPerspectiveButton = button;
	}

	/**
	 * Returns the perspective combo assoicated with the
	 * run perspective button.
	 * 
	 * @return a combo box
	 */
	protected Combo getRunCombo() {
		return fRunCombo;
	}

	/**
	 * Sets the perspective combo assoicated with the
	 * run perspective button.
	 * 
	 * @param combo a combo box
	 */
	private void setRunCombo(Combo combo) {
		fRunCombo = combo;
	}

	/**
	 * Returns the check box indicating whether the perspective should
	 * be changed when the configuration is launched in run mode.
	 * 
	 * @return a check box button
	 */
	protected Button getRunPerspectiveButton() {
		return fRunPerspectiveButton;
	}

	/**
	 * Sets the check box indicating whether the perspective should
	 * be changed when the configuration is launched in run mode.
	 * 
	 * @param button a check box button
	 */
	private void setRunPerspectiveButton(Button button) {
		fRunPerspectiveButton = button;
	}

	/**
	 * Returns the shell this tab is contained in
	 * 
	 * @return shell
	 */
	protected Shell getShell() {
		return getDebugCombo().getShell();
	}
	
	/**
	 * Sets up event handlers for the widgets in this tab.
	 */
	protected void initializeEventHandling() {
		getRunPerspectiveButton().addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateRunPerspective();
				}
			}
		);
		
		getRunCombo().addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateRunPerspective();
				}
			}
		);
		
		getDebugPerspectiveButton().addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateDebugPerspective();
				}
			}
		);
		
		getDebugCombo().addSelectionListener(
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateDebugPerspective();
				}
			}
		);		
		
	}
	
	/**
	 * Update the run perspective attribute based on current
	 * UI settings.
	 */
	protected void updateRunPerspective() {
		if (getRunPerspectiveButton().getSelection()) {
			getLaunchConfiguration().setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE,
				getPerspectiveWithLabel(getRunCombo().getText()).getId());
		} else {
			getLaunchConfiguration().setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, null);
		}
		getLaunchDialog().refreshStatus();	
	}
	
	/**
	 * Update the debug perspective attribute based on current
	 * UI settings.
	 */
	protected void updateDebugPerspective() {
		if (getDebugPerspectiveButton().getSelection()) {
			getLaunchConfiguration().setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE,
				getPerspectiveWithLabel(getDebugCombo().getText()).getId());
		} else {
			getLaunchConfiguration().setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, null);
		}
		getLaunchDialog().refreshStatus();
		
	}	

	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp, int columnWidth) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnWidth;
		label.setLayoutData(gd);
	}
		
}
	

