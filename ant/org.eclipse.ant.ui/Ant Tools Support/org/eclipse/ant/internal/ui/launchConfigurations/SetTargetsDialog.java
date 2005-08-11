/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.ant.internal.ui.preferences.DialogSettingsHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SetTargetsDialog extends Dialog {
	
    private static String DIALOG_SETTINGS_SECTION = "SetTargetsDialogSettings"; //$NON-NLS-1$
    
	private ILaunchConfigurationWorkingCopy fConfiguration;
	private AntTargetsTab fTargetsTab;
	
	public SetTargetsDialog(Shell parentShell, ILaunchConfigurationWorkingCopy config) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | getShellStyle());
		fConfiguration= config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		getShell().setText(AntLaunchConfigurationMessages.SetTargetsDialog_0);
		Composite composite = (Composite)super.createDialogArea(parent);
		
		fTargetsTab= new AntTargetsTab();
		fTargetsTab.createControl(composite);
		fTargetsTab.initializeFrom(fConfiguration);
		applyDialogFont(composite);
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fTargetsTab.performApply(fConfiguration);
        
		super.okPressed();
	}

    protected String getTargetsSelected() {
		String defaultValue= null;
		if (!fTargetsTab.isTargetSelected()) {
			defaultValue= ""; //$NON-NLS-1$
		}
        try {
            return fConfiguration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS, defaultValue);
        } catch (CoreException e) {
            return defaultValue;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        DialogSettingsHelper.persistShellGeometry(getShell(), DIALOG_SETTINGS_SECTION);
        return super.close();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    protected Point getInitialLocation(Point initialSize) {
        Point p = DialogSettingsHelper.getInitialLocation(DIALOG_SETTINGS_SECTION);
        return p != null ? p : super.getInitialLocation(initialSize);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialSize()
     */
    protected Point getInitialSize() {
        Point p = super.getInitialSize();
        return DialogSettingsHelper.getInitialSize(DIALOG_SETTINGS_SECTION, p);
    }
}
    
