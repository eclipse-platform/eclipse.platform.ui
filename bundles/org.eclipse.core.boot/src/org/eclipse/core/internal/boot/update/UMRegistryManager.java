package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
import org.eclipse.core.internal.boot.LaunchInfo;
import java.io.*;
import java.net.*;
import java.util.*;


public class UMRegistryManager extends UMRegistryManagerModel {
	private UMFactory fFactory;

	// Current is the Local one that has been filtered with LaunchInfo to
	// reflect the active configs and components
	//---------------------------------------------------------------------
	private UMRegistry fCurrentRegistry, fLocalRegistry, fDiscoveryRegistry = null;
	private URL fEclipseBaseURL = null;
	
	// used for performance timings
	private long startTick = (new java.util.Date()).getTime(); 
	private long lastTick = startTick;


public UMRegistryManager(URL baseURL) {
	super();
	fEclipseBaseURL = UMEclipseTree.appendTrailingSlash(baseURL);
	fFactory = new UMFactory();
	
}
// add the descriptor to the current and local registries (after successful apply)

public void addComponentDescriptorToLocal(IComponentDescriptor comp) {

	// need to reload the manifest so it'll reflect the state of things as the component
	// exists relative to the current registry.  Cannot just add the component descriptor
	// to the current registry
	addComponentDescriptorToLocal(comp, false);
}
/*
 * add the descriptor to the current and local registries (after successful apply)
 * isDangling() is set after a download (in here), and during registry load
 * The important assumption used here is that component entries cannot be downloaded
 * as a dangling component.
 */ 
public void addComponentDescriptorToLocal(IComponentDescriptor comp, boolean dangling) {

	// need to reload the manifest so it'll reflect the state of things as the component
	// exists relative to the current registry.  Cannot just add the component descriptor
	// to the current registry
	fCurrentRegistry._loadComponentManifest(UMEclipseTree.getComponentURL().toString(),comp.getDirName(),fFactory);
	fLocalRegistry._loadComponentManifest(UMEclipseTree.getComponentURL().toString(),comp.getDirName(),fFactory);

	LaunchInfo.VersionedIdentifier vid = new LaunchInfo.VersionedIdentifier(comp.getUniqueIdentifier(), comp.getVersionStr());
	if (dangling) {
		fCurrentRegistry._addToDanglingComponentIVPsRel(vid);
		fLocalRegistry._addToDanglingComponentIVPsRel(vid);
	} 	
	
	// look for freshly added comp descriptor and sync up the product
	// and compEntry sides
	//---------------------------------------------------------------

	// Current Registry 
	ComponentDescriptorModel newComp = fCurrentRegistry._lookupComponentDescriptor(vid.getIdentifier(), vid.getVersion());
	if (newComp != null) {
		Vector prod_list = fCurrentRegistry._getAllProducts();
		Enumeration list = prod_list.elements();
		while ( list.hasMoreElements()) {
			ProductDescriptorModel prod = (ProductDescriptorModel) list.nextElement();
			ComponentEntryDescriptorModel compEntry = prod._lookupComponentEntry(vid.getIdentifier());
			if (compEntry != null) {
				compEntry._isInstalled(true);
				newComp._addToContainingProductsRel(prod);
			}
		}
	}

	// Repeat for local registry
	newComp = fLocalRegistry._lookupComponentDescriptor(vid.getIdentifier(), vid.getVersion());
	if (newComp != null) {
		Vector prod_list = fLocalRegistry._getAllProducts();
		Enumeration list = prod_list.elements();
		while ( list.hasMoreElements()) {
			ProductDescriptorModel prod = (ProductDescriptorModel) list.nextElement();
			ComponentEntryDescriptorModel compEntry = prod._lookupComponentEntry(vid.getIdentifier());
			if (compEntry != null) {
				compEntry._isInstalled(true);
				newComp._addToContainingProductsRel(prod);
			}
		}
	}	
			
	
}
// add the descriptor to the current and local registries (after successful apply)
// LINDA - can this be simplified?

public void addProductDescriptorToLocal(IProductDescriptor prod) {

	fCurrentRegistry._loadProductManifest(UMEclipseTree.getProductURL().toString(),prod.getDirName(),fFactory);
	fLocalRegistry._loadProductManifest(UMEclipseTree.getProductURL().toString(),prod.getDirName(),fFactory);
	
}
private UMRegistry createNewRegistry() {
	UMRegistry registry = (UMRegistry) fFactory.createUMRegistry();

	return registry;
}
/*
 * Given current list of products and components, search for all available updates using
 * the updateURLs specified by each.
 * Builds an array of products to download and one of components to download
 */
public void discover() {

	rehydrateRegistries();
	
	// for each dangling component at the latest version, check all updateURLs
	IComponentDescriptor[] comp_list = fCurrentRegistry.getDanglingComponents();
	
	for (int i = 0; i < comp_list.length; i++) {
		IComponentDescriptor comp = (IComponentDescriptor) comp_list[i];
		IURLNamePair[] urlNP_list = comp.getUpdateURLs();
	
		// connect to each updateURL, look for latest version of the component there
		for (int j=0; j < urlNP_list.length; j++) {
			IUMRegistry remote_reg = getRegistryAt(urlNP_list[j].getURL());
			IComponentDescriptor remote_comp = 	(IComponentDescriptor) 
						remote_reg.getComponentDescriptor(comp.getUniqueIdentifier());
			
			if (remote_comp.compare(comp) > 0) {
				try {
				 	_addToComponentsToDownloadRel((IComponentDescriptor)remote_comp.clone());
				} catch (java.lang.CloneNotSupportedException e) {
				}
			}
		}		
	}

	// for each product at the latest version, check all updateURLs
	IProductDescriptor[] prod_list = fCurrentRegistry.getProductDescriptors();
	
	for (int i = 0; i < prod_list.length; i++) {
		IProductDescriptor prod = (IProductDescriptor) prod_list[i];
		IURLNamePair[] urlNP_list = prod.getUpdateURLs();
	
		// connect to each updateURL, look for newer version of the product
		for (int j=0; j < urlNP_list.length; j++) {
			IUMRegistry remote_reg = getRegistryAt(urlNP_list[j].getURL());
			IProductDescriptor remote_prod = 	(IProductDescriptor) 
						remote_reg.getProductDescriptor(prod.getUniqueIdentifier());
			
			if (remote_prod.compare(prod) > 0) {
				try {
				 	_addToProductsToDownloadRel((IProductDescriptor)remote_prod.clone());
				} catch (java.lang.CloneNotSupportedException e) {
				}
			}
		}		
	}
	
}
/**
 * convert a list of comma-separated tokens into an array
 */
public static String[] getArrayFromList(String prop) {
	if (prop == null || prop.trim().equals(""))
		return new String[0];
	Vector list = new Vector();
	StringTokenizer tokens = new StringTokenizer(prop, ",");
	while (tokens.hasMoreTokens()) {
		String token = tokens.nextToken().trim();
		if (!token.equals(""))
			list.addElement(token);
	}
	return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[0]);
}
// return a list of components that needs downloading

public IComponentDescriptor[] getComponentDownloadList() {
	int size = _getSizeOfComponentsToDownloadRel();	
	if(size == 0) return new IComponentDescriptor[0];
	
	IComponentDescriptor[] list = new IComponentDescriptor[size];
	_copyComponentsToDownloadRelInto(list);
	return list;
	
}
// return the current registry - containing only the active pieces

public IUMRegistry getCurrentRegistry() {
	rehydrateRegistries();

	return fCurrentRegistry;
	
}
// return the local registry - a full snapshot of the local tree

public IUMRegistry getLocalRegistry() {
	rehydrateRegistries();
	return fLocalRegistry;
	
}
// return a list of products that needs downloading

public IProductDescriptor[] getProductDownloadList() {
	int size = _getSizeOfProductsToDownloadRel();		
	if(size == 0) return new IProductDescriptor[0];
	
	IProductDescriptor[] list = new IProductDescriptor[size];
	_copyProductsToDownloadRelInto(list);
	return list;
	
}
// return the local (current) registry

public IUMRegistry getRegistryAt(URL url) {
	UMRegistry reg = createNewRegistry();
	reg._loadManifests(url, fFactory);
	fDiscoveryRegistry = reg;
	return (IUMRegistry) fDiscoveryRegistry;
	
}
/* if new, create new registry
 * otherwise, load again
 */
private void rehydrateRegistries() {
	if (fLocalRegistry == null) {
		fLocalRegistry = createNewRegistry();
//		fLocalRegistry._loadSettings(fFactory);
		fLocalRegistry._loadManifests(fEclipseBaseURL, fFactory); 
	}
	if (fCurrentRegistry == null) {
		fCurrentRegistry = createNewRegistry();
//		fCurrentRegistry._loadSettings(fFactory);
		fCurrentRegistry._loadManifests(fEclipseBaseURL, fFactory, true); // filtered for LaunchInfo
	}
}
}
