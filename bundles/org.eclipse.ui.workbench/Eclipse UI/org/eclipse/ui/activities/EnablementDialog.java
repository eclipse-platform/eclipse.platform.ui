/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;


/**
 * Dialog that will prompt the user and confirm that they wish to activate a set
 * of activities.
 * @since 3.0
 */
class EnablementDialog extends Dialog {

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(EnablementDialog.class.getName());
    
    private IIdentifier identifier;

    private Button dontAskButton;
    
    /**
     * @param parentShell
     */
    protected EnablementDialog(Shell parentShell, IIdentifier identifier) {
        super(parentShell);
        this.identifier = identifier;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {            
		Composite composite = (Composite)super.createDialogArea(parent);
		Text text = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
		text.setText(RESOURCE_BUNDLE.getString("requires")); //$NON-NLS-1$
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Set activityIds = identifier.getActivityIds();
		IActivityManager manager = PlatformUI.getWorkbench().getActivitySupport().getActivityManager();
		for (Iterator i = activityIds.iterator(); i.hasNext();) {
            String id = (String) i.next();                
            IActivity activity = manager.getActivity(id);
            if (!activity.isEnabled()) {
                Label label = new Label(composite, SWT.READ_ONLY);
                try {
                    label.setText("\t* " + activity.getName()); //$NON-NLS-1$                   
                } catch (NotDefinedException e) {
                    label.setText("\t* " + id); //$NON-NLS-1$
                }
                label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));                
            }
        }
		text = new Text(composite, SWT.READ_ONLY);
		text.setText(RESOURCE_BUNDLE.getString("proceed")); //$NON-NLS-1$
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		dontAskButton = new Button(composite, SWT.CHECK);
        dontAskButton.setSelection(false);
        dontAskButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dontAskButton.setText(RESOURCE_BUNDLE.getString("dontAsk")); //$NON-NLS-1$
		
		return composite;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(RESOURCE_BUNDLE.getString("title")); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        if (dontAskButton.getSelection()) {
            PlatformUI
	    	.getWorkbench()
	    	.getPreferenceStore()
	    	.setValue(
	    	    IWorkbenchPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT, false);            
        }
        super.okPressed();
    }
}