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

package org.eclipse.ant.ui.internal.launchConfigurations;

import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class AntJRETab extends JavaJRETab {
	
	private Button useSeparateVM;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		super.createControl(parent);
		
		useSeparateVM = new Button((Composite)getControl(), SWT.CHECK);
		useSeparateVM.setFont(parent.getFont());
		useSeparateVM.setText(AntLaunchConfigurationMessages.getString("AntJRETab.Run_&Ant_in_a_separate_Java_virtual_machine_1")); //$NON-NLS-1$
		useSeparateVM.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				toggleUseSeparateVM();
				updateLaunchConfigurationDialog();
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		useSeparateVM.setLayoutData(gd);
		createVerticalSpacer((Composite)getControl(), 2);
		
		//mechanism to specify the working directory.
	}
	
	private void toggleUseSeparateVM() {
		boolean enable = useSeparateVM.getSelection();
		fJREAddButton.setEnabled(enable);
		fJRECombo.setEnabled(enable);
		fJRELabel.setEnabled(enable);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
	
		String vmTypeID= null;
		try {
			vmTypeID = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
		} catch (CoreException ce) {
			AntUIPlugin.log(ce);			
		}
		
		useSeparateVM.setSelection(vmTypeID != null);
		toggleUseSeparateVM();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (useSeparateVM.getSelection()) {
			super.performApply(configuration);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.apache.tools.ant.Main"); //$NON-NLS-1$
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider"); //$NON-NLS-1$
		} else {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		}
	}
}
