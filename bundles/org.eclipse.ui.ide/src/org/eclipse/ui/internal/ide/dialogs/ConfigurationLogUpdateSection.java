/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.PrintWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
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
            if (i > 0)
                writer
                        .println("----------------------------------------------------"); //$NON-NLS-1$

            writer.println(NLS.bind(IDEWorkbenchMessages.SystemSummary_configuration, configurations[i].getLabel()));
            writer.println(NLS.bind(IDEWorkbenchMessages.SystemSummary_isCurrentConfiguration, new Boolean(configurations[i].isCurrent())));
            IActivity[] activities = configurations[i].getActivities();
            for (int j = 0; j < activities.length; j++) {
                writer.println();
                writer.println(NLS.bind(IDEWorkbenchMessages.SystemSummary_date, activities[j].getDate()));
                writer.println(NLS.bind(IDEWorkbenchMessages.SystemSummary_target, activities[j].getLabel()));
                writer.println(NLS.bind(IDEWorkbenchMessages.SystemSummary_action, getActionLabel(activities[j])));
                writer.println(NLS.bind(IDEWorkbenchMessages.SystemSummary_status, getStatusLabel(activities[j])));
            }
        }
    }

    private String getActionLabel(IActivity activity) {
        int action = activity.getAction();
        switch (action) {
        case IActivity.ACTION_CONFIGURE:
            return IDEWorkbenchMessages.SystemSummary_activity_enabled;
        case IActivity.ACTION_FEATURE_INSTALL:
            return IDEWorkbenchMessages.SystemSummary_activity_featureInstalled;
        case IActivity.ACTION_FEATURE_REMOVE:
            return IDEWorkbenchMessages.SystemSummary_activity_featureRemoved;
        case IActivity.ACTION_SITE_INSTALL:
            return IDEWorkbenchMessages.SystemSummary_activity_siteInstalled;
        case IActivity.ACTION_SITE_REMOVE:
            return IDEWorkbenchMessages.SystemSummary_activity_siteRemoved;
        case IActivity.ACTION_UNCONFIGURE:
            return IDEWorkbenchMessages.SystemSummary_activity_disabled;
        case IActivity.ACTION_REVERT:
            return IDEWorkbenchMessages.SystemSummary_activity_revert;
        case IActivity.ACTION_RECONCILIATION:
            return IDEWorkbenchMessages.SystemSummary_activity_reconcile;
        case IActivity.ACTION_ADD_PRESERVED:
            return IDEWorkbenchMessages.SystemSummary_activity_preserved;
        default:
            return IDEWorkbenchMessages.SystemSummary_activity_unknown;
        }
    }

    private String getStatusLabel(IActivity activity) {
        switch (activity.getStatus()) {
        case IActivity.STATUS_OK:
            return IDEWorkbenchMessages.SystemSummary_activity_status_success;
        case IActivity.STATUS_NOK:
            return IDEWorkbenchMessages.SystemSummary_activity_status_failure;
        }
        return IDEWorkbenchMessages.SystemSummary_activity_status_unknown;
    }
}
