/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.ObjectContributorManager;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;

/**
 * Extends generic object contributor manager by loading property page
 * contributors from the registry.
 */

public class PropertyPageContributorManager extends ObjectContributorManager {
	private static PropertyPageContributorManager sharedInstance=null;
	
	private static final Comparator comparer = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object arg0, Object arg1) {
			// Make sure the workbench info page is always at the top.
			RegistryPageContributor c1 = (RegistryPageContributor)arg0;
			RegistryPageContributor c2 = (RegistryPageContributor)arg1;
			if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(c1.getPageId())) {
				// c1 is the info page
				if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(c2.getPageId())) {
					// both are the info page so c2 is not greater
					return 0;
				}
				// c2 is any other page so it must be greater
				return -1;
			}
			if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(c2.getPageId())) {
				// c1 is any other page so it is greater
				return 1;
			}

			// The other pages are sorted in alphabetical order			 
			String s1 = c1.getPageName();
			String s2 = c2.getPageName();
			return collator.compare(s1, s2);
		}
	}; 

/**
 * The constructor.
 */
public PropertyPageContributorManager() {
	super();
    // load contributions on startup so that getContributors() returns the
    // proper content
    loadContributors();    
}
/**
 * Given the object class, this method will find all the registered
 * matching contributors and sequentially invoke them to contribute to the
 * property page manager. Matching algorithm will also check subclasses and
 * implemented interfaces.  
 * @return true if contribution took place, false otherwise.
 */
public boolean contribute(PropertyPageManager manager, IAdaptable object) {

	List result = getContributors(object);	
	
	if (result == null || result.size() == 0)
		return false;
	
	// Sort the results 
	Object[] sortedResult = result.toArray();
	Collections.sort(Arrays.asList(sortedResult), comparer);

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
 * this object. 
 */
public boolean hasContributorsFor(Object object) {
   return super.hasContributorsFor(object);
}
/**
 * Loads property page contributors from the registry.
 */
private void loadContributors() {
	PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(this);
	reader.registerPropertyPages(Platform.getExtensionRegistry());
}	
}
