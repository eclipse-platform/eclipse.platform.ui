/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;


import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchConfigurationMessages;
import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor;
import org.eclipse.jdt.internal.debug.ui.jres.JREsComboBlock;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page for setting global Ant JRE settings.
 */
public class AntJREPage {
	
	private JREsComboBlock fJREBlock;
	private AntRuntimePreferencePage preferencePage;
	
	//	Selection changed listener (checked JRE)
	 private ISelectionChangedListener fListener = new ISelectionChangedListener() {
		 public void selectionChanged(SelectionChangedEvent event) {
			 handleSelectedJREChanged();
		 }
	 };
	
	/**
	 * Creates an instance.
	 */
	public AntJREPage(AntRuntimePreferencePage preferencePage) {
		this.preferencePage= preferencePage;
	}
	
	/**
	 * Creates the tab item that contains this sub-page.
	 */
	protected TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("JR&E");
		//item.setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY));
		item.setData(this);
		item.setControl(createContents(folder));
		return item;
	}
	
	protected Composite createContents(Composite parent) {
		Font font = parent.getFont();
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setFont(font);
		WorkbenchHelp.setHelp(top, IAntUIHelpContextIds.ANT_JRE_PAGE);
		GridLayout layout = new GridLayout();
		layout.numColumns= 2;
		top.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		top.setLayoutData(gridData); 
				
		fJREBlock = new JREsComboBlock();
		fJREBlock.setDefaultJREDescriptor(getDefaultJREDescriptor());
		fJREBlock.createControl(top);
		Control control = fJREBlock.getControl();
		fJREBlock.addSelectionChangedListener(fListener);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		control.setLayoutData(gridData);

		Composite dynTabComp = new Composite(top, SWT.NONE);
		dynTabComp.setFont(font);

//		setDynamicTabHolder(dynTabComp);
//		GridLayout tabHolderLayout = new GridLayout();
//		tabHolderLayout.marginHeight= 0;
//		tabHolderLayout.marginWidth= 0;
//		tabHolderLayout.numColumns = 1;
//		getDynamicTabHolder().setLayout(tabHolderLayout);
//		gd = new GridData(GridData.FILL_BOTH);
//		getDynamicTabHolder().setLayoutData(gd);
		
		return top;
	}
	
	protected String getJRE() {
		if (!fJREBlock.isDefaultJRE()) {
			IVMInstall vmInstall= fJREBlock.getJRE();
			StringBuffer vmInfo = new StringBuffer();
			if (vmInstall != null) {
				vmInfo.append(vmInstall.getName());
				vmInfo.append(AntUtil.ATTRIBUTE_SEPARATOR);
				vmInfo.append(vmInstall.getVMInstallType().getId());
			}
			if (vmInfo.length() > 0) {
				return vmInfo.toString();
			}
		}
		return null;
	}
	
	protected void updateJREFromPrefs() {
		String vmName = null;
		String vmTypeID = null;
		
		IPreferenceStore store= preferencePage.getPreferenceStore();
		String vmInfo = store.getString(IAntUIPreferenceConstants.ANT_VM_INFORMATION);
		if (vmInfo != null) {
			String[] infos= AntUtil.parseString(vmInfo, AntUtil.ATTRIBUTE_SEPARATOR);
			vmName= infos[0];
			vmTypeID= infos[1];
		}	
		
		selectJRE(vmTypeID, vmName);
	}
	
	protected void selectJRE(String typeID, String vmName) {
		if (typeID == null) {
			fJREBlock.setUseDefaultJRE();
		} else {
			IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
			for (int i = 0; i < types.length; i++) {
				IVMInstallType type = types[i];
				if (type.getId().equals(typeID)) {
					IVMInstall[] installs = type.getVMInstalls();
					for (int j = 0; j < installs.length; j++) {
						IVMInstall install = installs[j];
						if (install.getName().equals(vmName)) {
							fJREBlock.setJRE(install);
							return;
						}
					
					}
					break;
				}
			}
			//fUnknownVMName = vmName;
			fJREBlock.setJRE(null);
		}
	}
	
	/**
	 * Notification that the user changed the selection in the JRE combo box.
	 */
	protected void handleSelectedJREChanged() {
//		if (fOkToClearUnknownVM) {
//			fUnknownVMName = null;
//			fUnknownVMType = null;
//		}
//		
//		loadDynamicJREArea();
//		
//		// always set the newly created area with defaults
//		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
//		if (getDynamicTab() == null) {
//			// remove any VM specfic args from the config
//			if (wc == null) {
//				if (getLaunchConfiguration().isWorkingCopy()) {
//					wc = (ILaunchConfigurationWorkingCopy)getLaunchConfiguration();
//				}
//			}
//			if (!fIsInitializing) {
//				if (wc != null) {
//					wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (Map)null);
//				}
//			}
//		} else {
//			if (wc == null) {
//				try {
//					if (getLaunchConfiguration().isWorkingCopy()) {
//						// get a fresh copy to work on
//						wc = ((ILaunchConfigurationWorkingCopy)getLaunchConfiguration()).getOriginal().getWorkingCopy();
//					} else {
//							wc = getLaunchConfiguration().getWorkingCopy();
//					}
//				} catch (CoreException e) {
//					JDIDebugUIPlugin.errorDialog(LauncherMessages.getString("JavaJRETab.Unable_to_initialize_defaults_for_selected_JRE_1"), e); //$NON-NLS-1$
//					return;
//				}
//			}
//			if (!fIsInitializing) {
//				getDynamicTab().setDefaults(wc);
//				getDynamicTab().initializeFrom(wc);
//			}
//		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab#getDefaultJREDescriptor()
	 */
	protected DefaultJREDescriptor getDefaultJREDescriptor() {
		return new DefaultJREDescriptor() {
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDefaultJRE()
			 */
			public IVMInstall getDefaultJRE() {
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDescription()
			 */
			public String getDescription() {
			
				return AntLaunchConfigurationMessages.getString("AntJRETab.2"); //$NON-NLS-1$
			}
		};
	}
}