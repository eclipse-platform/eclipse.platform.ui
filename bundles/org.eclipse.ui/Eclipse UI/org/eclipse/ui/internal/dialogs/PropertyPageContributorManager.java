package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.misc.Sorter;
import java.util.*;

/**
 * Extends generic object contributor manager by loading property page
 * contributors from the registry.
 */

public class PropertyPageContributorManager extends ObjectContributorManager {
	private static PropertyPageContributorManager sharedInstance=null;
	private boolean contributorsLoaded=false;
	
	private Sorter sorter = new Sorter() {
		public boolean compare(Object o1, Object o2) {
			String s1 = ((RegistryPageContributor)o1).getPageName().toUpperCase();
			String s2 = ((RegistryPageContributor)o2).getPageName().toUpperCase();
			//Return true if elementTwo is 'greater than' elementOne
			return s2.compareTo(s1) > 0;
		}
	};
/**
 * The constructor.
 */
public PropertyPageContributorManager() {
	super();
}
/**
 * Given the object class, this method will find all the registered
 * matching contributors and sequentially invoke them to contribute
 * to the property page manager. Matching algorithm
 * will also check subclasses and implemented interfaces.
 * 
 * @return true if contribution took place, false otherwise.
 */
public boolean contribute(PropertyPageManager manager, IAdaptable object) {
	// Lazy initialize
	if (!contributorsLoaded)
		loadContributors();

	// Get all the property page contributors registered for the object
	List result = super.getContributors(object.getClass());
	if (result == null || result.size() == 0)
		return false;

	// Sort the results in alphabetical order
	Object[] sortedResult = sorter.sort(result.toArray());

	// Make sure the workbench info page is always at the top.
	for (int i = 0; i < sortedResult.length; i++) {
		RegistryPageContributor ppcont = (RegistryPageContributor) sortedResult[i];
		if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(ppcont.getPageId())) {
			sortedResult[i] = sortedResult[0];
			sortedResult[0] = ppcont;
			break;
		}
	}

	// Allow each contributor to add its page to the manager.
	boolean actualContributions = false;
	for (int i = 0; i < sortedResult.length; i++) {
		IPropertyPageContributor ppcont = (IPropertyPageContributor) sortedResult[i];
		if (!ppcont.isApplicableTo(object)) continue;
		if (ppcont.contributePropertyPages(manager, object))
			actualContributions = true;
	}
	return actualContributions;
}
/**
 * Ideally, shared instance should not be used
 * and manager should be located in the workbench class.
 */
public static PropertyPageContributorManager getManager() {
	if (sharedInstance==null)
	   sharedInstance = new PropertyPageContributorManager();
	return sharedInstance;
}
/**
 * Returns true if contributors exist in the manager for
 * this object. If contributors wer not loaded from
 * the registry, load them first.
 */
public boolean hasContributorsFor(Object object) {
   if (!contributorsLoaded) loadContributors();
   return super.hasContributorsFor(object);
}
/**
 * Loads property page contributors from the registry.
 */
private void loadContributors() {
	PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(this);
	reader.registerPropertyPages(Platform.getPluginRegistry());
	contributorsLoaded=true;
	
}
}
