package org.eclipse.update.internal.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.ui.IStartup;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configurator.ConfiguratorUtils;
import org.eclipse.update.configurator.IPlatformConfiguration;
import org.eclipse.update.configurator.IPlatformConfiguration.ISiteEntry;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.InternalSiteManager;
import org.eclipse.update.internal.operations.OperationFactory;
import org.eclipse.update.operations.IOperation;
import org.eclipse.update.operations.OperationsManager;

public class ScheduledPluginUninstall implements IStartup {

	public void earlyStartup() {
		URL platformXML = ConfiguratorUtils.getCurrentPlatformConfiguration().getConfigurationLocation();
		
		File f = new File(platformXML.getFile());

		f = new File(f.getParentFile(), "toBeUninstalled");
		try {
			if (!f.exists()) {
				return;
			}
			FileInputStream fis = new FileInputStream(f);
			Properties toBeUninstalled = new Properties();
			toBeUninstalled.load(fis);
			fis.close();

			IFeature[] features = new IFeature[toBeUninstalled.size()];
			IConfiguredSite[] configuredSites = new IConfiguredSite[toBeUninstalled.size()];

			IConfiguredSite[] sites = SiteManager.getLocalSite().getCurrentConfiguration().getConfiguredSites();

			Enumeration featuresToBeDeleted = toBeUninstalled.elements();
			int i = 0;
			while(featuresToBeDeleted.hasMoreElements()) {
				String temp = (String)featuresToBeDeleted.nextElement();
				StringTokenizer stk = new StringTokenizer( temp, ";");
				String targetSite = stk.nextToken();
				String featureName = stk.nextToken();
				for(int j = 0; j < sites.length; j++) {
					if (sites[j].getSite().getURL().toString().equals(targetSite)) {
						configuredSites[i] = sites[j];
						IFeatureReference[] siteFeatures = configuredSites[i].getFeatureReferences();
						for(int k = 0; k < siteFeatures.length; k++) {
							if (siteFeatures[k].getVersionedIdentifier().toString().equals(featureName)) {
								features[i] = siteFeatures[k].getFeature(null);
								
								break;
							}
						}
						
					}
				}
				i++;
			}
			

			IOperation uninstallFeaturesOperation =
				((OperationFactory)OperationsManager.getOperationFactory()).createUninstallFeaturesOperation( configuredSites, features);

			uninstallFeaturesOperation.execute(null, null);
			
			f.delete();
		} catch (IOException ioe) {
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
