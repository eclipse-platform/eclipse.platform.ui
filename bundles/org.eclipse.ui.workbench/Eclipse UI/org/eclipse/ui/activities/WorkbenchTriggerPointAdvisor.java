/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.eclipse.ui.internal.util.Util;

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
		
		if (triggerPoint.getBooleanHint(ITriggerPoint.HINT_PRE_UI)) {
			IActivityManager activityManager = PlatformUI.getWorkbench()
					.getActivitySupport().getActivityManager();
			Iterator iterator = identifier.getActivityIds().iterator();
			while (iterator.hasNext()) {
				String id = (String) iterator.next();
				IActivity activity = activityManager.getActivity(id);
				if (activity.getExpression() != null) {
					if (!activity.isEnabled())
						// if we have any disabled expression activities we
						// should disallow immediately
						return null; 
				}
			}
			// if we have no disabled expression activities just return the
			// empty set. This will allow the use of the given object but will
			// not result in any activity activation.
			return Collections.EMPTY_SET;  
		}
		
        if (!PrefUtil.getInternalPreferenceStore().getBoolean(
                IPreferenceConstants.SHOULD_PROMPT_FOR_ENABLEMENT)) {
            return identifier.getActivityIds();
        }		
		
		//If it's a non-interactive point activate all activities
		if (!triggerPoint.getBooleanHint(ITriggerPoint.HINT_INTERACTIVE)) {
			return identifier.getActivityIds();
		}
		
        EnablementDialog dialog = new EnablementDialog(Util.getShellToParentOn(), identifier
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

	/**
	 * This implementation of
	 * {@link ITriggerPointAdvisor#computeEnablement(IActivityManager, IIdentifier)}
	 * calls
	 * {@link #doComputeEnablement(IActivityManager, IIdentifier, boolean)} with
	 * a boolean argument of <code>false</code>. Subclasses that wish to
	 * disable identifiers if there is at least one disabled expression-based
	 * activity should override this method and call
	 * {@link #doComputeEnablement(IActivityManager, IIdentifier, boolean)} with
	 * a boolean argument of <code>true</code>.
	 * 
	 * Subclasses may override.
	 * 
	 * @param activityManager
	 *            the activity manager
	 * @param identifier
	 *            the identifier to update
	 * 
	 * @return <code>true</code> if this identifier should be enabled,
	 *         <code>false</code> otherwise
	 * @since 3.4
	 * 
	 * @see WorkbenchTriggerPointAdvisor#doComputeEnablement(IActivityManager,
	 *      IIdentifier, boolean)
	 */
	public boolean computeEnablement(IActivityManager activityManager, IIdentifier identifier) {
		return doComputeEnablement(activityManager, identifier, false);
	}

	/**
	 * Helper method for determining whether an identifier should be enabled.
	 * Returns <code>true</code> if there is no applicable activity for the
	 * given identifier. Otherwise, if the boolean argument <code>
	 * disabledExpressionActivitiesTakePrecedence</code> is
	 * <code>false</code>, returns true if any of the applicable activities
	 * is enabled. If the boolean argument is <code>true</code>, this method
	 * returns <code>false</code> if there is at least one disabled
	 * expression-based activity; and it returns <code>true</code> if there
	 * are no disabled expression-based activities and there is at least one
	 * applicable activity that is enabled.
	 * @param activityManager the activity manager
	 * @param identifier
	 *            the identifier to update
	 * @param disabledExpressionActivitiesTakePrecedence
	 * 
	 * @return <code>true</code> if this identifier should be enabled,
	 *         <code>false</code> otherwise
	 * @since 3.4
	 */
	protected boolean doComputeEnablement(IActivityManager activityManager,
			IIdentifier identifier, boolean disabledExpressionActivitiesTakePrecedence) {
		final Set activityIds = identifier.getActivityIds();
		if (activityIds.size() == 0) {
			return true;
		}

		boolean matchesAtLeastOneEnabled = false;
		boolean matchesDisabledExpressionActivitiesWithPrecedence = false;
		Set enabledActivityIds = activityManager.getEnabledActivityIds();
		for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {
			String activityId = (String) iterator.next();
			IActivity activity = activityManager.getActivity(activityId);
			
			if (activity.isEnabled() && enabledActivityIds.contains(activityId)) {
				if (!disabledExpressionActivitiesTakePrecedence) {
					return true;
				}
				matchesAtLeastOneEnabled = true;
			} else {
				if (disabledExpressionActivitiesTakePrecedence && activity.getExpression() != null) {
					matchesDisabledExpressionActivitiesWithPrecedence = true;
				}
			}

		}

		return !matchesDisabledExpressionActivitiesWithPrecedence && matchesAtLeastOneEnabled;
	}
}
