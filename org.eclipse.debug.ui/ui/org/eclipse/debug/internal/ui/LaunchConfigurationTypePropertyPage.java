package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page used to set a default launch configuration type on IResources.
 */
public class LaunchConfigurationTypePropertyPage extends PropertyPage {

	/**
	 * Arrays for the launch configuration types and their names.  
	 * The first entries are <code>null</code> and <code>0</code> respectively.
	 */
	private ILaunchConfigurationType[] fConfigTypes;
	private String[] fConfigTypeNames;

	private Label fSelectLabel;
	private Combo fConfigTypeCombo;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite topComp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();		
		topComp.setLayout(topLayout);
		
		setSelectLabel(new Label(topComp, SWT.NONE));
		getSelectLabel().setText("Select default launch configuration type");
		
		setConfigTypeCombo(new Combo(topComp, SWT.NONE));
		initializeConfigComboItems();
		
		return topComp;
	}

	/**
	 * Populate the internal data structures that store all launch configuration types 
	 * and their names.  The launch configuration type data structure has a <code>null</code>
	 * entry that corresponds to the name <code>none</code> in the name data structure.
	 * This allows the user to choose no default launch configuration type for the resource.
	 */
	protected void initializeConfigComboItems() {
		ILaunchConfigurationType[] realConfigTypes = getLaunchManager().getLaunchConfigurationTypes();
		ILaunchConfigurationType[] configTypes = new ILaunchConfigurationType[realConfigTypes.length + 1];
		configTypes[0] = null;
		System.arraycopy(realConfigTypes, 0, configTypes, 1, realConfigTypes.length);		
		setConfigTypes(configTypes);
		
		String[] configTypeNames = new String[configTypes.length];
		configTypeNames[0] = "none";
		for (int i = 1; i < configTypes.length; i++) {
			configTypeNames[i] = configTypes[i].getName();			
		}
		setConfigTypeNames(configTypeNames);
		getConfigTypeCombo().setItems(configTypeNames);
		
		ILaunchConfigurationType defaultConfigType = getLaunchManager().getDefaultLaunchConfigurationType(getResource(), true);
		int selectionIndex = getLaunchConfigurationTypeIndex(defaultConfigType);
		
		getConfigTypeCombo().select(selectionIndex);
	}
	
	/**
	 * Return the index in the array of config types of the specified config type.
	 * Return 0 if the specified config type is null.
	 */
	protected int getLaunchConfigurationTypeIndex(ILaunchConfigurationType configType) {
		if (configType == null) {
			return 0;
		}
		ILaunchConfigurationType[] configTypes = getConfigTypes();		
		for (int i = 1; i < configTypes.length; i++) {
			if (configType.equals(configTypes[i])) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		int selectedIndex = getConfigTypeCombo().getSelectionIndex();
		ILaunchConfigurationType configType = null;
		if (selectedIndex > -1) {
			configType = getConfigTypes()[selectedIndex];
			String configTypeID = null;
			if (configType != null) {
				configTypeID = configType.getIdentifier();
			}
			getLaunchManager().setDefaultLaunchConfigurationType(getResource(), configTypeID);
		}
		return true;
	}
	
	/**
	 * Get the IResource this page works on.
	 */
	protected IResource getResource() {
		IAdaptable adaptable = getElement();
		return (IResource)adaptable.getAdapter(IResource.class);
	}
	
	private void setConfigTypes(ILaunchConfigurationType[] configTypes) {
		fConfigTypes = configTypes;
	}

	private ILaunchConfigurationType[] getConfigTypes() {
		return fConfigTypes;
	}
	
	private void setConfigTypeNames(String[] configTypeNames) {
		fConfigTypeNames = configTypeNames;
	}

	private String[] getConfigTypeNames() {
		return fConfigTypeNames;
	}

	private void setSelectLabel(Label selectLabel) {
		fSelectLabel = selectLabel;
	}

	private Label getSelectLabel() {
		return fSelectLabel;
	}

	private void setConfigTypeCombo(Combo configTypeCombo) {
		fConfigTypeCombo = configTypeCombo;
	}

	private Combo getConfigTypeCombo() {
		return fConfigTypeCombo;
	}
	
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();		
	}

}
