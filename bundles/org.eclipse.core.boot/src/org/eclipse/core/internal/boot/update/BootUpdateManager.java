package org.eclipse.core.internal.boot.update;

import java.util.StringTokenizer;
import java.util.PropertyResourceBundle;
import java.io.*;
import org.eclipse.core.internal.boot.*;
import java.net.URL;
import java.util.*;

public class BootUpdateManager {
/**
 * 
 * @return java.lang.String
 * @param manifest org.eclipse.core.internal.boot.update.IManifestDescriptor
 * @param manifest2 org.eclipse.core.internal.boot.update.IManifestDescriptor
 */
private static String createErrorString(IManifestDescriptor manifest, IManifestDescriptor[] manifestsConflicting) {

   StringBuffer strbMessage = new StringBuffer();
	
	strbMessage.append(manifest.getUniqueIdentifier());
	strbMessage.append(" ");
	strbMessage.append(manifest.getVersionIdentifier());
	strbMessage.append(": ");
	strbMessage.append("Cannot be installed because of one or more conflicts");
	strbMessage.append("\n");
	
	for (int j = 0; j < manifestsConflicting.length; ++j) {
		strbMessage.append(manifestsConflicting[j].getUniqueIdentifier());
		strbMessage.append(" ");
		strbMessage.append(manifestsConflicting[j].getVersionIdentifier());
		strbMessage.append("\n");
	}
	
	return strbMessage.toString();
}
/*
 * Registers newly found configurations and components with the current
 * launch info.F
 */
public static LaunchInfo.Status[] install(LaunchInfo.VersionedIdentifier[] vidConfigurations, LaunchInfo.VersionedIdentifier[] vidComponents) {

	LaunchInfo launchInfo = LaunchInfo.getCurrent();

	// NOTE: Ingore any entries that do not contain valid install.xml
	//---------------------------------------------------------------

	// Create list for error messages
	//-------------------------------
	Vector vectorMessages = new Vector();
	
	// Obtain the registry of the installed tree
	//------------------------------------------
	URL urlBase = UMEclipseTree.getBaseInstallURL();
	String strUrlBase = urlBase.toExternalForm();
	UMRegistryManager registryManagerInstalled = new UMRegistryManager(urlBase);
	IUMRegistry registryInstalled = registryManagerInstalled.getLocalRegistry();

	// Register configurations
	//------------------------
	if (vidConfigurations.length > 0) {

		for (int i = 0; i < vidConfigurations.length; i++) {			

			IManifestDescriptor manifestDescriptor = registryInstalled.getProductDescriptor(vidConfigurations[i].getIdentifier(), vidConfigurations[i].getVersion());

			// Verify existence
			//-----------------
			IManifestDescriptor[] manifestsConflicting = registryInstalled.getConflictingManifests(manifestDescriptor);

			// Add to launch info
			//-------------------
			if (manifestsConflicting.length == 0) {
				launchInfo.setConfiguration(vidConfigurations[i]);
			}

			// Create error message string
			//----------------------------
			else {
				String strError = createErrorString(manifestDescriptor, manifestsConflicting);

				vectorMessages.add(new LaunchInfo.Status(strError));
			}
		}
	}

	// Register components
	//--------------------
	if (vidComponents.length > 0) {

		for (int i = 0; i < vidComponents.length; i++) {

			IComponentDescriptor componentDescriptor = registryInstalled.getComponentDescriptor(vidComponents[i].getIdentifier(), vidComponents[i].getVersion());

			// Validate existence
			//-------------------
			IManifestDescriptor[] manifestsConflicting = registryInstalled.getConflictingManifests(componentDescriptor);

			// Add to launch info
			//-------------------
			if (manifestsConflicting.length == 0) {
				launchInfo.setComponent(vidComponents[i]);

				// Register plugins
				//-----------------
				IPluginEntryDescriptor[] pluginDescriptors = componentDescriptor.getPluginEntries();

				for (int j = 0; j < pluginDescriptors.length; ++j) {

					String strId = pluginDescriptors[j].getUniqueIdentifier();

					launchInfo.setPlugin(new LaunchInfo.VersionedIdentifier(strId,pluginDescriptors[j].getVersionStr()));

					// Register platform component's boot plugin
					// This allows the platform to start with the plugin's boot.jar
					//-------------------------------------------------------------
					if (launchInfo.isPlatformComponent(vidComponents[i].getIdentifier())) {
						if (launchInfo.isRuntimePlugin(strId)) {
							launchInfo.setRuntime(new LaunchInfo.VersionedIdentifier(strId,pluginDescriptors[j].getVersionStr()));
						}
					}
				}
				
				// Register fragments
				//-------------------
				IFragmentEntryDescriptor[] fragmentDescriptors = componentDescriptor.getFragmentEntries();

				for (int j = 0; j < fragmentDescriptors.length; ++j) {

					launchInfo.setFragment(new LaunchInfo.VersionedIdentifier(fragmentDescriptors[j].getUniqueIdentifier(),fragmentDescriptors[j].getVersionStr()));
				}
			}

			// Obtain error message string
			//----------------------------
			else {
				String strError = createErrorString(componentDescriptor, manifestsConflicting);

				vectorMessages.add(new LaunchInfo.Status(strError));
			}
		}
	}

	// Return status array of localized error messages
	//------------------------------------------------
	LaunchInfo.Status[] straErrorMessages = new LaunchInfo.Status[vectorMessages.size()];
	vectorMessages.copyInto(straErrorMessages);
	return straErrorMessages;
}
/*
 * Parses a string into separate strings.
 *
 */
private static Vector parseStrings(String strLine) {

	Vector vectorStrings = new Vector();

	// Create a tokenizer with the default tokens, plus comma
	//-------------------------------------------------------
	StringTokenizer tokenizer = new StringTokenizer(strLine, " \t\n\r\f,");

	while (tokenizer.hasMoreTokens() == true) {
		vectorStrings.add(tokenizer.nextToken());
	}

	return vectorStrings;
}
/*
 * The URL is the location of a properties file that contains a list of
 * configurations and components.
 *
 * configurations = a,b,c
 * components = d,e,f
 *
 */
public static void uninstall(URL url) {

	// The url points to a property file
	//----------------------------------
	InputStream inputStream = null;

	try {
		inputStream = BaseURLHandler.open(url).getInputStream();
	}
	catch (IOException ex) {
	}

	// Read and parse the file
	//------------------------
	Vector vectorProducts = null;
	Vector vectorComponents = null;

	if (inputStream != null) {

		PropertyResourceBundle bundle = null;

		try {
			bundle = new PropertyResourceBundle(inputStream);

			String strProducts = bundle.getString("configurations");
			String strComponents = bundle.getString("components");

			vectorProducts = parseStrings(strProducts);
			vectorComponents = parseStrings(strComponents);
		}
		catch (IOException ex) {
		}
	}
	
	uninstall(vectorProducts, vectorComponents);
	return;
}

public static void uninstall(LaunchInfo.VersionedIdentifier[] configList, LaunchInfo.VersionedIdentifier[] compList) {
	Vector cfg = new Vector();
	Vector cmp = new Vector();
	for (int i=0; i<configList.length; i++)
		cfg.add(configList[i].toString());
	for (int i=0; i<compList.length; i++)
		cfg.add(compList[i].toString());
	uninstall(cfg, cmp);
	return;	
}


private static void uninstall(Vector vectorProducts, Vector vectorComponents) {
	
	// Process configurations and components
	//--------------------------------------
	if ((vectorProducts != null && vectorProducts.size() > 0) || (vectorComponents != null && vectorComponents.size() > 0)) {
		
		// Installed registry
		//-------------------
		URL urlBase = UMEclipseTree.getBaseInstallURL();
		UMRegistryManager registryManagerInstalled = new UMRegistryManager(urlBase);
		IUMRegistry registryInstalled = registryManagerInstalled.getLocalRegistry();

		LaunchInfo launchInfo = LaunchInfo.getCurrent();

		uninstallProducts(vectorProducts, registryInstalled, launchInfo);
		uninstallComponents(vectorComponents, registryInstalled, launchInfo);

		// Do garbage collection of inactive items
		//----------------------------------------
		launchInfo.uninstall();
	}

	return;
}

/**
 */
private static void uninstallComponents(Vector vectorComponents, IUMRegistry registry, LaunchInfo launchInfo) {

	if (vectorComponents == null || vectorComponents.size() == 0)
		return;

	Vector vectorComponentsRemoving = new Vector();
	Vector vectorPluginsRemoving = new Vector();
	Vector vectorFragmentsRemoving = new Vector();

	IdVersionPair pair = null;
	IComponentDescriptor componentInstalled = null;

	for (int i = 0; i < vectorComponents.size(); ++i) {

		pair = new IdVersionPair((String) vectorComponents.elementAt(i));

		// Obtain the descriptor from the installed registry
		//--------------------------------------------------
		componentInstalled = registry.getComponentDescriptor(pair.getUniqueIdentifier(), pair.getVersionStr());

		// Determine if the descriptor is removeable
		//------------------------------------------
		if (componentInstalled != null && componentInstalled.isRemovable() == true) {
			vectorComponentsRemoving.add(componentInstalled);

			// Plugins
			//--------
			IPluginEntryDescriptor[] plugins = componentInstalled.getPluginEntries();
			for (int j = 0; j < plugins.length; ++j) {
				vectorPluginsRemoving.addElement(plugins[j]);
			}

			// Fragments
			//----------
			IFragmentEntryDescriptor[] fragments = componentInstalled.getFragmentEntries();
			for (int j = 0; j < fragments.length; ++j) {
				vectorFragmentsRemoving.addElement(fragments[j]);
			}
		}
	}

	// Remove the components
	//----------------------
	if (vectorComponentsRemoving.size() > 0) {

		// Create a string array of component identifiers
		//-----------------------------------------------
		LaunchInfo.VersionedIdentifier[] vidaComponents = new LaunchInfo.VersionedIdentifier[vectorComponentsRemoving.size()];

		for (int i = 0; i < vectorComponentsRemoving.size(); ++i) {
			componentInstalled = (IComponentDescriptor) vectorComponentsRemoving.elementAt(i);
			vidaComponents[i] = new LaunchInfo.VersionedIdentifier(componentInstalled.getUniqueIdentifier(), componentInstalled.getVersionStr());
		}

		// Create a string array of plugin identifiers
		//--------------------------------------------
		LaunchInfo.VersionedIdentifier[] vidaPlugins = new LaunchInfo.VersionedIdentifier[vectorPluginsRemoving.size()];
		IPluginEntryDescriptor pluginInstalled = null;

		for (int i = 0; i < vectorPluginsRemoving.size(); ++i) {
			pluginInstalled = (IPluginEntryDescriptor) vectorPluginsRemoving.elementAt(i);
			vidaPlugins[i] = new LaunchInfo.VersionedIdentifier(pluginInstalled.getUniqueIdentifier(), pluginInstalled.getVersionStr());
		}

		// Create a string array of fragment identifiers
		//----------------------------------------------
		LaunchInfo.VersionedIdentifier[] vidaFragments = new LaunchInfo.VersionedIdentifier[vectorFragmentsRemoving.size()];
		IFragmentEntryDescriptor fragmentInstalled = null;

		for (int i = 0; i < vectorFragmentsRemoving.size(); ++i) {
			fragmentInstalled = (IFragmentEntryDescriptor) vectorFragmentsRemoving.elementAt(i);
			vidaFragments[i] = new LaunchInfo.VersionedIdentifier(fragmentInstalled.getUniqueIdentifier(), fragmentInstalled.getVersionStr());
		}

		// Remove the components, plugins, and fragments
		//----------------------------------------------
		launchInfo.setInactive(null, vidaComponents, vidaPlugins, vidaFragments);
	}

	return;
}
/**
 */
private static void uninstallProducts(Vector vectorProducts, IUMRegistry registry, LaunchInfo launchInfo) {

	if (vectorProducts == null || vectorProducts.size() == 0)
		return;

	// Determine which products may be removed
	//----------------------------------------
	Vector vectorProductsRemoving = new Vector();
	IdVersionPair pair = null;
	IProductDescriptor productInstalled = null;

	for (int i = 0; i < vectorProducts.size(); ++i) {

		pair = new IdVersionPair((String) vectorProducts.elementAt(i));

		// Obtain the descriptor from the installed registry
		//--------------------------------------------------
		productInstalled = registry.getProductDescriptor(pair.getUniqueIdentifier(), pair.getVersionStr());

		// Determine if the descriptor is removeable
		//------------------------------------------
		if (productInstalled != null && productInstalled.isRemovable() == true) {
			vectorProductsRemoving.add(productInstalled);
		}
	}

	// Remove the products
	//--------------------
	if (vectorProductsRemoving.size() > 0) {

		// Create a string array of product identifiers
		//---------------------------------------------	    
		LaunchInfo.VersionedIdentifier[] vidaProducts = new LaunchInfo.VersionedIdentifier[vectorProductsRemoving.size()];

		for (int i = 0; i < vectorProductsRemoving.size(); ++i) {
			productInstalled = (IProductDescriptor) vectorProductsRemoving.elementAt(i);
			vidaProducts[i] = new LaunchInfo.VersionedIdentifier(productInstalled.getUniqueIdentifier(), productInstalled.getVersionStr());
		}

		// Remove the products
		//--------------------
		launchInfo.setInactive(vidaProducts, null, null, null);
	}

	return;
}
}
