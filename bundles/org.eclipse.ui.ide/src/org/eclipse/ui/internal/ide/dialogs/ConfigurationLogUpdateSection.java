/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.update.configurator.ConfiguratorUtils;
import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.configurator.IPlatformConfiguration.IFeatureEntry;
import org.eclipse.update.configurator.IPlatformConfiguration.ISiteEntry;

/**
 * Writes information about the update configurer into the system summary.
 * 
 * @since 3.0
 */
public class ConfigurationLogUpdateSection implements ISystemSummarySection {
    public void write(PrintWriter writer) {
    	
    	IPlatformConfiguration platformConf = ConfiguratorUtils.getCurrentPlatformConfiguration();
    	writer.println(IDEWorkbenchMessages.ConfigurationLogUpdateSection_installConfiguration); 
    	writer.println(" " + NLS.bind( IDEWorkbenchMessages.ConfigurationLogUpdateSection_lastChangedOn, DateFormat.getDateInstance().format(new Date(platformConf.getChangeStamp())))); //$NON-NLS-1$
       	writer.println(" " + NLS.bind( IDEWorkbenchMessages.ConfigurationLogUpdateSection_location, platformConf.getConfigurationLocation()));  //$NON-NLS-1$
       	
       	ISiteEntry[] sites = platformConf.getConfiguredSites();
       	writer.println();
       	writer.println(" " + IDEWorkbenchMessages.ConfigurationLogUpdateSection_configurationSites);   //$NON-NLS-1$
       	for(int i = 0; i < sites.length; i++){
       		writer.println("  " + sites[i].getURL().toExternalForm()); //$NON-NLS-1$
       	}
       	
       	writer.println();
       	writer.println(" " + IDEWorkbenchMessages.ConfigurationLogUpdateSection_configurationFeatures);  //$NON-NLS-1$
       	IFeatureEntry[] features = platformConf.getConfiguredFeatureEntries();     
       	for(int i = 0; i < features.length; i++){
       		writer.println("  " + NLS.bind( IDEWorkbenchMessages.ConfigurationLogUpdateSection_featureIdAndVersion, features[i].getFeaturePluginIdentifier(), features[i].getFeaturePluginVersion())); //$NON-NLS-1$ 
       	}
       	
       	writer.println();
  		URL[] urls = platformConf.getPluginPath();
  		writer.println(" " + IDEWorkbenchMessages.ConfigurationLogUpdateSection_plugins); //$NON-NLS-1$
   		for(int j = 0; j < urls.length; j++){
   			writer.println("  " + urls[j].toExternalForm()); //$NON-NLS-1$
   		}
    }
}
