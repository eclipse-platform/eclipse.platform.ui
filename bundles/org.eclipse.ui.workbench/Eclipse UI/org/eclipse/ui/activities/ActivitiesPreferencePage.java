/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
 * Preference page that allows configuration of the activity set. This page may
 * be used by product developers to provide basic ability to tweak the enabled
 * activity set. You may provide the certain strings to this class via method #2
 * of {@link org.eclipse.core.runtime.IExecutableExtension}.
 * 
 * @see #ACTIVITY_NAME
 * @see #ACTIVITY_PROMPT_BUTTON
 * @see #ACTIVITY_PROMPT_BUTTON_TOOLTIP
 * @since 3.1
 */
public final class ActivitiesPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage, IExecutableExtension {

	/**
	 * The name to use for the activities.  Ie: "Capabilities".
	 */
    public static final String ACTIVITY_NAME = "activityName"; //$NON-NLS-1$
    
	/**
	 * The label to be used for the prompt button. Ie: "&Prompt when enabling capabilities".
	 */    
    public static final String ACTIVITY_PROMPT_BUTTON = "activityPromptButton"; //$NON-NLS-1$
    
	/**
	 * The tooltip to be used for the prompt button. Ie: "Prompt when a feature is first used that requires enablement of capabilities".
	 */    
    public static final String ACTIVITY_PROMPT_BUTTON_TOOLTIP = "activityPromptButtonTooltip"; //$NON-NLS-1$
    
	private Button activityPromptButton;

    private IWorkbench workbench;

    private ActivityEnabler enabler;
    
    private Properties strings = new Properties();

    private IMutableActivityManager workingCopy;
    
    /**
     * Create the prompt for activity enablement.
     * 
     * @param composite the parent
     */
    protected void createActivityPromptPref(Composite composite) {
        activityPromptButton = new Button(composite, SWT.CHECK);
        activityPromptButton.setText(strings.getProperty(ACTIVITY_PROMPT_BUTTON, ActivityMessages.activityPromptButton)); 
        activityPromptButton.setToolTipText(strings.getProperty(ACTIVITY_PROMPT_BUTTON_TOOLTIP, ActivityMessages.activityPromptToolTip));

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
    	initializeDialogUnits(parent);
    	
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);

        createActivityPromptPref(composite);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        activityPromptButton.setLayoutData(data);

        data = new GridData(GridData.FILL_BOTH);
        workingCopy = workbench.getActivitySupport().createWorkingCopy();
        enabler = new ActivityEnabler(workingCopy, strings);
        enabler.createControl(composite).setLayoutData(data);
        
        Dialog.applyDialogFont(composite);

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
        workbench.getActivitySupport().setEnabledActivityIds(workingCopy.getEnabledActivityIds());
        
        getPreferenceStore().setValue(
                IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT,
                activityPromptButton.getSelection());

        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        enabler.restoreDefaults();
        activityPromptButton.setSelection(getPreferenceStore()
                .getDefaultBoolean(
                        IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT));
        super.performDefaults();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof Hashtable) {
			strings.putAll((Hashtable)data);
		}		
	}
}
