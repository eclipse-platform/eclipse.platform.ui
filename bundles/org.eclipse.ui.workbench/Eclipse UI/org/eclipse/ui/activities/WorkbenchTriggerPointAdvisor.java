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
package org.eclipse.ui.activities;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.EnablementDialog;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * 
 * Workbench implementation prompts the user with a dialog unless they've said
 * that they don't want to be prompted. You may provide the certain strings to
 * this class via method #2 of
 * {@link org.eclipse.core.runtime.IExecutableExtension}. This is provided as
 * API so that non-SDK Eclipse applications can reuse and augment the default
 * SDK trigger point behaviour.
 * 
 * @see #PROCEED_MULTI
 * @see #PROCEED_SINGLE
 * @see #DONT_ASK
 * @see #NO_DETAILS
 * @since 3.1
 */
public class WorkbenchTriggerPointAdvisor implements ITriggerPointAdvisor,
        IExecutableExtension {

	/**
	 * The string to be used when prompting to proceed with multiple activities.
	 * Ie: "Enable the selected activities?"
	 */
	public static String PROCEED_MULTI = "proceedMulti"; //$NON-NLS-1$
	
	/**
	 * The string to be used when prompting to proceed with a single activity.
	 * Ie: "Enable the required activity?"
	 */
	public static String PROCEED_SINGLE = "proceedSingle"; //$NON-NLS-1$
	
	/**
	 * The string to be used to label the "don't ask" button.
	 * Ie: "&Always enable activities and don't ask me again"
	 */
	public static String DONT_ASK = "dontAsk"; //$NON-NLS-1$
	
	/**
	 * The string to be used when no activities are selected and Details are shown.
	 * Ie: "Select an activity to view its description."
	 */
	public static String NO_DETAILS = "noDetails"; //$NON-NLS-1$

	
	private Properties strings = new Properties();
	
	/**
	 * Create a new instance of this advisor.
	 */
	public WorkbenchTriggerPointAdvisor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.ITriggerPointAdvisor#allow(org.eclipse.ui.activities.ITriggerPoint, org.eclipse.ui.activities.IIdentifier)
	 */
	public Set allow(ITriggerPoint triggerPoint, IIdentifier identifier) {
        if (!PrefUtil.getInternalPreferenceStore().getBoolean(
                IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT)) {
            return identifier.getActivityIds();
        }		
		
		//If it's a non-interactive point activate all activities
		if (!triggerPoint.getBooleanHint(ITriggerPoint.HINT_INTERACTIVE))
			return identifier.getActivityIds();
		
        EnablementDialog dialog = new EnablementDialog(PlatformUI
                .getWorkbench().getDisplay().getActiveShell(), identifier
                .getActivityIds(), strings);
        if (dialog.open() == Window.OK) {
            Set activities = dialog.getActivitiesToEnable();
            if (dialog.getDontAsk()) {
				PrefUtil.getInternalPreferenceStore().setValue(
						IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT,
						false);
				WorkbenchPlugin.getDefault().savePluginPreferences();
			}

            return activities;
        }
		
		return null;
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
