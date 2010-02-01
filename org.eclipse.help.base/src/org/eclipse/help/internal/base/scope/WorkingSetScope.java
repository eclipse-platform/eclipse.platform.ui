/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.base.scope;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IIndexSee;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.workingset.AdaptableHelpResource;
import org.eclipse.help.internal.workingset.IHelpWorkingSetManager;
import org.eclipse.help.internal.workingset.WorkingSet;

public class WorkingSetScope extends AbstractHelpScope {
	
	IHelpWorkingSetManager wSetManager;
	private WorkingSet workingSet;
	AdaptableHelpResource[] elements;
	
	public WorkingSetScope(String scope, IHelpWorkingSetManager manager) {
		wSetManager = manager;
		workingSet = wSetManager.getWorkingSet(scope); 
		elements = workingSet.getElements();
		wSetManager = manager;
	}

	public boolean inScope(IToc toc) {
		for (int i = 0; i < elements.length; i++) {	
			for (AdaptableHelpResource adaptable = elements[i]; adaptable != null; ) {
				Object itoc = adaptable.getAdapter(IToc.class); 
				if (toc == itoc) {
					return true;	
				}
				IAdaptable parent= adaptable.getParent();
				if (parent instanceof AdaptableHelpResource) {
					adaptable = (AdaptableHelpResource) parent;
				} else {
				    adaptable = null;
				}
			}		
		}
		return false;
	}

	public boolean inScope(ITopic topic) {
		Set topics = new HashSet();
		IToc toc = null;
		topics.add(topic);
		if (topic instanceof UAElement) {
			for (UAElement uae = (UAElement) topic; uae != null; ) {
				if (uae instanceof IToc)  {
					toc = (IToc) uae;
					uae = null;
				} else if (uae instanceof IIndexEntry) {
					for (int i = 0; i < elements.length; i++) {
						AdaptableHelpResource adaptable = elements[i];
						if (adaptable.getTopic(topic.getHref()) != null) {
							return true;
						}
					}
					return false;
				} else {					
					if (uae instanceof ITopic) {
					    topics.add(uae);
					}
					uae = uae.getParentElement();
				}
			}
		}
		for (int i = 0; i < elements.length; i++) {
			AdaptableHelpResource adaptable = elements[i];
			if (toc != null) {
				Object itoc = adaptable.getAdapter(IToc.class);
				if (toc == itoc) {
					return true;
				}
			}
			Object itopic = adaptable.getAdapter(ITopic.class);
			if (topic != null && topics.contains(itopic)) {
				return true;
			}
			IAdaptable parent = adaptable.getParent();
			if (parent instanceof AdaptableHelpResource) {
				adaptable = (AdaptableHelpResource) parent;
			} else {
				adaptable = null;
			}
		}
		return false;
	}

	public boolean inScope(IIndexEntry entry) {
		return hasInScopeChildren(entry);
	}

	public boolean inScope(IIndexSee see) {
		return hasInScopeChildren(see);
	}

	public String getName(Locale locale) {
		return null;
	}

}
