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

package org.eclipse.ui.internal.csm.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractActivityRegistry implements IActivityRegistry {

	private IActivityRegistryEvent activityRegistryEvent;
	private List activityRegistryListeners;
	
	protected List activityDefinitions = Collections.EMPTY_LIST;
	
	protected AbstractActivityRegistry() {
	}

	public void addActivityRegistryListener(IActivityRegistryListener activityRegistryListener) {
		if (activityRegistryListener == null)
			throw new NullPointerException();
			
		if (activityRegistryListeners == null)
			activityRegistryListeners = new ArrayList();
		
		if (!activityRegistryListeners.contains(activityRegistryListener))
			activityRegistryListeners.add(activityRegistryListener);
	}

	public List getActivityDefinitions() {
		return activityDefinitions;
	}
	
	public void removeActivityRegistryListener(IActivityRegistryListener activityRegistryListener) {
		if (activityRegistryListener == null)
			throw new NullPointerException();
			
		if (activityRegistryListeners != null)
			activityRegistryListeners.remove(activityRegistryListener);
	}

	protected void fireActivityRegistryChanged() {
		if (activityRegistryListeners != null) {
			for (int i = 0; i < activityRegistryListeners.size(); i++) {
				if (activityRegistryEvent == null)
					activityRegistryEvent = new ActivityRegistryEvent(this);
							
				((IActivityRegistryListener) activityRegistryListeners.get(i)).activityRegistryChanged(activityRegistryEvent);
			}				
		}	
	}
}	
