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
package org.eclipse.ui.internal.about;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.osgi.framework.Bundle;

/**
 * This class puts basic platform information into the system summary log.  This
 * includes sections for the java properties, the ids of all installed features
 * and plugins, as well as a the current contents of the preferences service. 
 * 
 * @since 3.0
 */
public class ConfigurationLogDefaultSection implements ISystemSummarySection {

    /* (non-Javadoc)
     * @see org.eclipse.ui.about.ISystemSummarySection#write(java.io.PrintWriter)
     */
    public void write(PrintWriter writer) {
        appendProperties(writer);
        appendFeatures(writer);
        appendRegistry(writer);
        appendUserPreferences(writer);
    }

    /**
     * Appends the <code>System</code> properties.
     */
    private void appendProperties(PrintWriter writer) {
        writer.println();
        writer.println(WorkbenchMessages
                .getString("SystemSummary.systemProperties")); //$NON-NLS-1$
        Properties properties = System.getProperties();
        SortedSet set = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                String s1 = (String) o1;
                String s2 = (String) o2;
                return s1.compareTo(s2);
            }
        });
        set.addAll(properties.keySet());
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Object key = i.next();
            writer.print(key);
            writer.print('=');
            writer.println(properties.get(key));
        }
    }

    /**
     * Appends the installed and configured features.
     */
    private void appendFeatures(PrintWriter writer) {
        writer.println();
        writer.println(WorkbenchMessages.getString("SystemSummary.features")); //$NON-NLS-1$

        IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
        LinkedList groups = new LinkedList();
        if (providers != null)
            for (int i = 0; i < providers.length; ++i) {
                IBundleGroup[] bundleGroups = providers[i].getBundleGroups();
                for (int j = 0; j < bundleGroups.length; ++j)
                    groups.add(new AboutBundleGroupData(bundleGroups[j]));
            }
        AboutBundleGroupData[] bundleGroupInfos = (AboutBundleGroupData[]) groups
                .toArray(new AboutBundleGroupData[0]);

        AboutData.sortById(false, bundleGroupInfos);

        for (int i = 0; i < bundleGroupInfos.length; ++i) {
            AboutBundleGroupData info = bundleGroupInfos[i];
            String[] args = new String[] { info.getId(), info.getVersion(),
                    info.getName() };
            writer.println(WorkbenchMessages.format(
                    "SystemSummary.featureVersion", args)); //$NON-NLS-1$
        }
    }

    /**
     * Appends the contents of the Plugin Registry.
     */
    private void appendRegistry(PrintWriter writer) {
        writer.println();
        writer.println(WorkbenchMessages
                .getString("SystemSummary.pluginRegistry")); //$NON-NLS-1$

        Bundle[] bundles = WorkbenchPlugin.getDefault().getBundles();
        AboutBundleData[] bundleInfos = new AboutBundleData[bundles.length];

        for (int i = 0; i < bundles.length; ++i)
            bundleInfos[i] = new AboutBundleData(bundles[i]);

        AboutData.sortById(false, bundleInfos);

        for (int i = 0; i < bundleInfos.length; ++i) {
            AboutBundleData info = bundleInfos[i];
            String[] args = new String[] { info.getId(), info.getVersion(),
                    info.getName() };
            writer.println(WorkbenchMessages.format(
                    "SystemSummary.descriptorIdVersion", args)); //$NON-NLS-1$
        }
    }

    /**
     * Appends the preferences
     */
    private void appendUserPreferences(PrintWriter writer) {
        // write the prefs to a byte array
        IPreferencesService service = Platform.getPreferencesService();
        IEclipsePreferences node = service.getRootNode();
        ByteArrayOutputStream stm = new ByteArrayOutputStream();
        try {
            service.exportPreferences(node, stm, null);
        } catch (CoreException e) {
            writer.println("Error reading preferences " + e.toString());//$NON-NLS-1$		
        }

        // copy the prefs from the byte array to the writer
        writer.println();
        writer.println(WorkbenchMessages
                .getString("SystemSummary.userPreferences")); //$NON-NLS-1$

        BufferedReader reader = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(stm
                    .toByteArray());
            reader = new BufferedReader(new InputStreamReader(in, "8859_1")); //$NON-NLS-1$
            char[] chars = new char[8192];

            while (true) {
                int read = reader.read(chars);
                if (read <= 0)
                    break;
                writer.write(chars, 0, read);
            }
        } catch (IOException e) {
            writer.println("Error reading preferences " + e.toString());//$NON-NLS-1$		
        }

        // ByteArray streams don't need to be closed
    }
}