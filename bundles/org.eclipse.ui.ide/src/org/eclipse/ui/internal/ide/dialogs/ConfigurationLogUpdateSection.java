/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.PrintWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.update.configuration.IActivity;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.SiteManager;

/**
 * Writes information about the update configurer into the system summary.
 * 
 * @since 3.0
 */
public class ConfigurationLogUpdateSection implements ISystemSummarySection {
    public void write(PrintWriter writer) {
		ILocalSite site;
		try {
			site = SiteManager.getLocalSite();
		} catch (CoreException e) {
			e.printStackTrace(writer);
			return;
		}
		IInstallConfiguration[] configurations = site.getConfigurationHistory();
		for (int i = 0; i < configurations.length; i++) {
			writer.println();
			if (i>0)
				writer.println("----------------------------------------------------"); //$NON-NLS-1$

			writer.println(IDEWorkbenchMessages.format("SystemSummary.configuration", new Object[] {configurations[i].getLabel()})); //$NON-NLS-1$
			writer.println(IDEWorkbenchMessages.format("SystemSummary.isCurrentConfiguration", new Object[] {new Boolean(configurations[i].isCurrent())})); //$NON-NLS-1$ 
			IActivity[] activities = configurations[i].getActivities();
			for (int j = 0; j < activities.length; j++) {
				writer.println();
				writer.println(IDEWorkbenchMessages.format("SystemSummary.date", new Object[] {activities[j].getDate()})); //$NON-NLS-1$
				writer.println(IDEWorkbenchMessages.format("SystemSummary.target", new Object[] {activities[j].getLabel()})); //$NON-NLS-1$
				writer.println(IDEWorkbenchMessages.format("SystemSummary.action", new Object[] {getActionLabel(activities[j])})); //$NON-NLS-1$
				writer.println(IDEWorkbenchMessages.format("SystemSummary.status", new Object[] {getStatusLabel(activities[j])})); //$NON-NLS-1$
			}
		}
    }

	private String getActionLabel(IActivity activity) {
		int action = activity.getAction();
		switch (action) {
			case IActivity.ACTION_CONFIGURE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.enabled"); //$NON-NLS-1$
			case IActivity.ACTION_FEATURE_INSTALL:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.featureInstalled"); //$NON-NLS-1$
			case IActivity.ACTION_FEATURE_REMOVE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.featureRemoved"); //$NON-NLS-1$
			case IActivity.ACTION_SITE_INSTALL:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.siteInstalled"); //$NON-NLS-1$
			case IActivity.ACTION_SITE_REMOVE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.siteRemoved"); //$NON-NLS-1$
			case IActivity.ACTION_UNCONFIGURE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.disabled"); //$NON-NLS-1$
			case IActivity.ACTION_REVERT:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.revert"); //$NON-NLS-1$
			case IActivity.ACTION_RECONCILIATION:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.reconcile"); //$NON-NLS-1$
			case IActivity.ACTION_ADD_PRESERVED:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.preserved"); //$NON-NLS-1$
			default:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.unknown"); //$NON-NLS-1$
		}
	}

	private String getStatusLabel(IActivity activity) {
		switch (activity.getStatus()) {
			case IActivity.STATUS_OK:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.status.success"); //$NON-NLS-1$
			case IActivity.STATUS_NOK:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.status.failure"); //$NON-NLS-1$
		}
		return IDEWorkbenchMessages.getString("SystemSummary.activity.status.unknown"); //$NON-NLS-1$
	}
}
