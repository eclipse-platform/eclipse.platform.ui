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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.ActivityEnabler;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;


/**
 * Preference page that allows configuration of the activity set.

 * @since 3.0
 */
public class ActivitiesPreferencePage 
	extends PreferencePage 
	implements IWorkbenchPreferencePage {


    private Button activityPromptButton;
    private IWorkbench workbench;
    private ActivityEnabler enabler; 
    
    /**
     * Create the prompt for activity enablement.
     * 
     * @param composite the parent
     */
    protected void createActivityPromptPref(Composite composite) {
        activityPromptButton = new Button(composite, SWT.CHECK);
        activityPromptButton.setText(ActivityMessages
                .getString("activityPromptButton")); //$NON-NLS-1$
        activityPromptButton.setToolTipText(ActivityMessages
                .getString("activityPromptToolTip")); //$NON-NLS-1$
        
        activityPromptButton.setFont(composite.getFont());
        setActivityButtonState();
    }    

    
    /**
	 * Sets the state of the activity prompt button from preferences.
	 */
	private void setActivityButtonState() {
		activityPromptButton.setSelection(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT));
	}


	/* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
		composite.setFont(parent.getFont());
		
		createActivityPromptPref(composite);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		activityPromptButton.setLayoutData(data);
		
		data = new GridData(GridData.FILL_BOTH);
		enabler = new ActivityEnabler(workbench.getActivitySupport());
		enabler.createControl(composite).setLayoutData(data);

		return composite;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench aWorkbench) {
        this.workbench = aWorkbench;    
        setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        enabler.updateActivityStates();

        getPreferenceStore().setValue(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT, activityPromptButton
                .getSelection());
        
        return true;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        enabler.restoreDefaults();     
        activityPromptButton.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT));
        super.performDefaults();
    }
}
