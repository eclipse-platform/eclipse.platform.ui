/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.text.Collator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.ObjectContributorManager;
import org.eclipse.ui.internal.misc.Sorter;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;

/**
 * Extends generic object contributor manager by loading property page
 * contributors from the registry.
 */

public class PropertyPageContributorManager extends ObjectContributorManager {
	private static PropertyPageContributorManager sharedInstance=null;
	private boolean contributorsLoaded=false;
	
	private Sorter sorter = new Sorter() {
		private Collator collator = Collator.getInstance();
		
		public boolean compare(Object o1, Object o2) {
			// Make sure the workbench info page is always at the top.
			RegistryPageContributor c1 = (RegistryPageContributor)o1;
			RegistryPageContributor c2 = (RegistryPageContributor)o2;
			if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(c1.getPageId())) {
				// c1 is the info page
				if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(c2.getPageId())) {
					// both are the info page so c2 is not greater
					return false;
				}
				// c2 is any other page so it must be greater
				return true;
			}
			if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(c2.getPageId())) {
				// c1 is any other page so it is greater
				return false;
			}

			// The other pages are sorted in alphabetical order			 
			String s1 = c1.getPageName();
			String s2 = c2.getPageName();
			//Return true if c2 is 'greater than' c1
			return collator.compare(s2, s1) > 0;
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
	
	List result = getContributors(object);	
	
	if (result == null || result.size() == 0)
		return false;
	
	// Sort the results 
	Object[] sortedResult = sorter.sort(result.toArray());

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
