package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;import java.io.InputStream;import java.net.URL;import java.util.ArrayList;import java.util.PropertyResourceBundle;import java.util.StringTokenizer;import java.util.Vector;import org.eclipse.core.internal.boot.LaunchInfo;import org.eclipse.core.internal.boot.Policy;import org.eclipse.core.internal.boot.LaunchInfo.Status;import org.eclipse.core.internal.boot.LaunchInfo.VersionedIdentifier;

public class BootUpdateManager {
/**
 * 
 * @return java.lang.String
 * @param manifest org.eclipse.core.internal.boot.update.IManifestDescriptor
 * @param manifest2 org.eclipse.core.internal.boot.update.IManifestDescriptor
 */
private static String createErrorString(IManifestDescriptor manifest, IManifestDescriptor[] manifestsConflicting) {

	StringBuffer strbMessage = new StringBuffer();
	
	// at present we are limited to 1 line message
	if (manifestsConflicting.length==0)
		strbMessage.append(Policy.bind("update.conflicts",manifest.getUniqueIdentifier(),manifest.getVersionIdentifier().toString()));
	else
		strbMessage.append(Policy.bind("update.conflictsWith",
										new String[] {
											manifest.getUniqueIdentifier(),
											manifest.getVersionIdentifier().toString(),
											manifestsConflicting[0].getUniqueIdentifier(),
											manifestsConflicting[0].getVersionIdentifier().toString()
										} ));
		
//	strbMessage.append("\n");	
//	for (int j = 0; j < manifestsConflicting.length; ++j) {
//		strbMessage.append(Policy.bind("update.conflictsId",manifestsConflicting[j].getUniqueIdentifier(),manifestsConflicting[j].getVersionIdentifier().toString()));
//		strbMessage.append("\n");
//	}
	
	return strbMessage.toString();
}
/*
 * Filters out the older versions of the identifiers.
 */
private static LaunchInfo.VersionedIdentifier[] filterNewestVersions( LaunchInfo.VersionedIdentifier[] vids ){
	
	if( vids == null )
		return null;
		
	if( vids.length <= 1 )
		return vids;
	
	VersionComparator comparator = new VersionComparator();	
	Vector vectorLatest = new Vector();
	LaunchInfo.VersionedIdentifier vid = null;
	boolean bFound = false;

	// Add latest versions of each identfier to another list
	//------------------------------------------------------
	for( int i=0; i<vids.length; ++i){
		
		bFound = false;
		
		// Determine if the identifier has already been seen
		//--------------------------------------------------
		for( int j=0; j<vectorLatest.size(); ++j ){
			
			// Retrieve an identifier from the encountered list
			//-------------------------------------------------
			vid = (LaunchInfo.VersionedIdentifier)vectorLatest.elementAt(j);
			
			// Determine if the identifiers are equal
			//---------------------------------------
			if( vids[i].getIdentifier().equals( vid.getIdentifier() ) == true ){
				bFound = true;
				
				// Compare versions and replace if newer
				//--------------------------------------
				if( comparator.compare( vids[i].getVersion(), vid.getVersion() ) > 0 ){
					vectorLatest.setElementAt( vids[i], j);
					break;
				}
			}
		}
		
		// Add to the list if this is the first encounter
		//-----------------------------------------------
		if( bFound == false ){
			vectorLatest.add( vids[i] );
		}
	}
	
	// Create an array from the list of latest versions
	//-------------------------------------------------
	LaunchInfo.VersionedIdentifier[] vidsLatest = new LaunchInfo.VersionedIdentifier[vectorLatest.size()];
	vectorLatest.copyInto(vidsLatest);
	
	return vidsLatest;
}
/*
 * Registers newly found configurations and components with the current
 * launch info.
 */
public static LaunchInfo.Status[] install(LaunchInfo.VersionedIdentifier[] vidConfigurations, LaunchInfo.VersionedIdentifier[] vidComponents) {

	LaunchInfo launchInfo = LaunchInfo.getCurrent();

	// NOTE: Ingore any entries that do not contain valid install.xml
	//---------------------------------------------------------------

	// Create list for error messages
	//-------------------------------
	Vector vectorMessages = new Vector();
	
	// Create lists to keep track of any items that need to be inactivated as a result of conflicts
	// --------------------------------------------------------------------------------------------
	ArrayList inactConfigs = new ArrayList();
	ArrayList inactComps = new ArrayList();
	ArrayList inactPlugins = new ArrayList();
	ArrayList inactFragments = new ArrayList();
	
	// Obtain the registry of the installed tree
	//------------------------------------------
	URL urlBase = UMEclipseTree.getBaseInstallURL();
	String strUrlBase = urlBase.toExternalForm();
	UMRegistryManager registryManagerInstalled = new UMRegistryManager(urlBase);
	IUMRegistry registryInstalled = registryManagerInstalled.getLocalRegistry();
	IUMRegistry registryCurrent = registryManagerInstalled.getCurrentRegistry();

	// Register configurations
	//------------------------
	if (vidConfigurations.length > 0) {
		
		// Obtain only newest versions
		//----------------------------
		vidConfigurations = filterNewestVersions( vidConfigurations );

		for (int i = 0; i < vidConfigurations.length; i++) {			

			IProductDescriptor productDescriptor = registryInstalled.getProductDescriptor(vidConfigurations[i].getIdentifier(), vidConfigurations[i].getVersion());
			if (productDescriptor!=null) {

				// Validate existence
				//-------------------
				IManifestDescriptor[] manifestsConflicting = registryCurrent.getConflictingManifests(productDescriptor);

				// Add to launch info
				//-------------------
				if (manifestsConflicting.length == 0) {
					launchInfo.setConfiguration(vidConfigurations[i], productDescriptor.getApplication());
				}

				// Create error message string and mark for inactivation
				//------------------------------------------------------
				else {
					String strError = createErrorString(productDescriptor, manifestsConflicting);
					vectorMessages.add(new LaunchInfo.Status(strError));
					inactConfigs.add(vidConfigurations[i]);
				}
			}
		}
	}

	// Register components  
	//--------------------
	if (vidComponents.length > 0) {
		
		vidComponents = filterNewestVersions( vidComponents );

		for (int i = 0; i < vidComponents.length; i++) {

			IComponentDescriptor componentDescriptor = registryInstalled.getComponentDescriptor(vidComponents[i].getIdentifier(), vidComponents[i].getVersion());
			if (componentDescriptor!=null) {

				// Validate existence
				//-------------------
				IManifestDescriptor[] manifestsConflicting = registryCurrent.getConflictingManifests(componentDescriptor);

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

				// Obtain error message string and mark for inactivation
				//------------------------------------------------------
				else {
					String strError = createErrorString(componentDescriptor, manifestsConflicting);
					vectorMessages.add(new LaunchInfo.Status(strError));
					inactComps.add(vidComponents[i]);	

					IPluginEntryDescriptor[] plugList = componentDescriptor.getPluginEntries();
					for (int ix = 0; plugList!=null && ix < plugList.length; ++ix) {
						inactPlugins.add(new LaunchInfo.VersionedIdentifier(plugList[ix].getUniqueIdentifier(),plugList[ix].getVersionStr()));
					}
					IFragmentEntryDescriptor[] fragList = componentDescriptor.getFragmentEntries();
					for (int ix = 0; fragList != null && ix < fragList.length; ++ix) {
						inactFragments.add(new LaunchInfo.VersionedIdentifier(fragList[ix].getUniqueIdentifier(),fragList[ix].getVersionStr()));
					}
				}
			}
		}
	}
	
	// Inactivate any conflicting items
	// --------------------------------
	if ((inactConfigs.size() + inactComps.size() + inactPlugins.size() + inactFragments.size()) > 0) {
		LaunchInfo.VersionedIdentifier[] inactConfigsList = new LaunchInfo.VersionedIdentifier[inactConfigs.size()];
		LaunchInfo.VersionedIdentifier[] inactCompsList = new LaunchInfo.VersionedIdentifier[inactComps.size()];
		LaunchInfo.VersionedIdentifier[] inactPluginsList = new LaunchInfo.VersionedIdentifier[inactPlugins.size()];	
		LaunchInfo.VersionedIdentifier[] inactFragmentsList = new LaunchInfo.VersionedIdentifier[inactFragments.size()];
		inactConfigs.toArray(inactConfigsList);
		inactComps.toArray(inactCompsList);
		inactPlugins.toArray(inactPluginsList);
		inactFragments.toArray(inactFragmentsList);	
		launchInfo.setInactive(inactConfigsList, inactCompsList, inactPluginsList, inactFragmentsList);
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


/**
 *
 */
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

		if( registryInstalled != null && launchInfo != null ){			
			uninstallProducts(vectorProducts, registryInstalled, launchInfo);
			uninstallComponents(vectorComponents, registryInstalled, launchInfo);
		}

		// Do garbage collection of inactive items
		//----------------------------------------
		launchInfo.uninstall();
	}

	return;
}

/**
 * Makes the component inactive, and all of its child plugins / fragments inactive,
 * only if the component is removable.
 */
private static void uninstallComponent(IComponentDescriptor componentInstalled, IProductDescriptor productParent, IUMRegistry registry, LaunchInfo launchInfo) {

	if (componentInstalled == null || registry == null || launchInfo == null)
		return;

	// Determine if the component is removeable
	//-----------------------------------------
	if (componentInstalled.isRemovable( productParent ) == true) {

		// Component
		//----------
		LaunchInfo.VersionedIdentifier[] vidaComponents = new LaunchInfo.VersionedIdentifier[1];
		vidaComponents[0] = new LaunchInfo.VersionedIdentifier( componentInstalled.getUniqueIdentifier(), componentInstalled.getVersionStr() );

		// Plugins
		//--------
		IPluginEntryDescriptor[] plugins = componentInstalled.getPluginEntries();
		LaunchInfo.VersionedIdentifier[] vidaPlugins = new LaunchInfo.VersionedIdentifier[plugins.length];
		
		for (int i = 0; i < plugins.length; ++i) {
			vidaPlugins[i] = new LaunchInfo.VersionedIdentifier(plugins[i].getUniqueIdentifier(), plugins[i].getVersionStr());
		}

		// Fragments
		//----------
		IFragmentEntryDescriptor[] fragments = componentInstalled.getFragmentEntries();
		LaunchInfo.VersionedIdentifier[] vidaFragments = new LaunchInfo.VersionedIdentifier[fragments.length];
		
		for (int i = 0; i < fragments.length; ++i) {
			vidaFragments[i] = new LaunchInfo.VersionedIdentifier(fragments[i].getUniqueIdentifier(), fragments[i].getVersionStr());
		}

		// Remove the component, plugins, and fragments
		//---------------------------------------------
		launchInfo.setInactive(null, vidaComponents, vidaPlugins, vidaFragments);
	}

	return;
}
/**
 * Attempts to make the component and its child plugins / fragments inactive.
 */
private static void uninstallComponents(Vector vectorComponents, IUMRegistry registry, LaunchInfo launchInfo) {

	if (vectorComponents == null || vectorComponents.size() == 0 || registry == null || launchInfo == null)
		return;

	IdVersionPair        pair               = null;
	IComponentDescriptor componentInstalled = null;

	for (int i = 0; i < vectorComponents.size(); ++i) {

		pair = new IdVersionPair((String) vectorComponents.elementAt(i));

		// Obtain the descriptor from the installed registry
		//--------------------------------------------------
		componentInstalled = registry.getComponentDescriptor(pair.getUniqueIdentifier(), pair.getVersionStr());

		// Uninstall the component assuming no parent
		//-------------------------------------------
		uninstallComponent( componentInstalled, null, registry, launchInfo );
	}

	return;
}
/**
 * Makes the configuration inactive, and all of its child components inactive
 * only if the configuration / component is removable.
 */
private static void uninstallProducts(Vector vectorProducts, IUMRegistry registry, LaunchInfo launchInfo) {

	if (vectorProducts == null || vectorProducts.size() == 0 || registry == null || launchInfo == null)
		return;

	// Determine which products may be removed
	//----------------------------------------
	IdVersionPair      pair                   = null;
	IProductDescriptor productInstalled       = null;
	Vector             vectorProductsRemoving = new Vector();


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

	// Remove the configurations
	//--------------------------
	if (vectorProductsRemoving.size() > 0) {

		// Create a string array of product identifiers
		//---------------------------------------------	    
		LaunchInfo.VersionedIdentifier[] vidaProducts = new LaunchInfo.VersionedIdentifier[vectorProductsRemoving.size()];

		Vector vectorComponentsRemoving = new Vector();

		for (int i = 0; i < vectorProductsRemoving.size(); ++i) {
			
			productInstalled = (IProductDescriptor) vectorProductsRemoving.elementAt(i);
			
			// Uninstall each child component
			//-------------------------------
			IComponentEntryDescriptor[] componentEntries = productInstalled.getComponentEntries();
			
			if( componentEntries != null ){
				for( int j=0; j<componentEntries.length; ++j ){
					uninstallComponent( componentEntries[j].getComponentDescriptor(), productInstalled, registry, launchInfo );
				}
			}
	
			// Add to the list of inactive products
			//-------------------------------------
			vidaProducts[i] = new LaunchInfo.VersionedIdentifier(productInstalled.getUniqueIdentifier(), productInstalled.getVersionStr());
		}

		// Remove the products
		//--------------------
		launchInfo.setInactive(vidaProducts, null, null, null);
	}

	return;
}
}
