/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.ITriggerPointManager;

/**
 * @since 3.1
 */
public class TriggerPointManager implements ITriggerPointManager {

	private HashMap triggerMap = new HashMap();

	/**
	 * 
	 */
	public TriggerPointManager() {
		super();
		triggerMap.put(ITriggerPointManager.UNKNOWN_TRIGGER_POINT_ID,
				new AbstractTriggerPoint() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.activities.ITriggerPoint#getId()
					 */
					public String getId() {
						return ITriggerPointManager.UNKNOWN_TRIGGER_POINT_ID;
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.activities.ITriggerPoint#getName()
					 */
					public String getName() {
						return ""; //$NON-NLS-1$
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.activities.ITriggerPoint#getDescription()
					 */
					public String getDescription() {
						return ""; //$NON-NLS-1$
					}
					
					/* (non-Javadoc)
					 * @see org.eclipse.ui.activities.ITriggerPoint#getStringHint(java.lang.String)
					 */
					public String getStringHint(String key) {
						if (ITriggerPoint.HINT_INTERACTIVE.equals(key)) {
							// TODO: change to false when we have mapped our trigger points
							return Boolean.TRUE.toString();
						}
						return null;
					}
					
					/* (non-Javadoc)
					 * @see org.eclipse.ui.activities.ITriggerPoint#getBooleanHint(java.lang.String)
					 */
					public boolean getBooleanHint(String key) {
						if (ITriggerPoint.HINT_INTERACTIVE.equals(key)) {
							// TODO: change to false when we have mapped our trigger points
							return true;
						}
						return false;
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.ITriggerPointManager#getTriggerPoint(java.lang.String)
	 */
	public ITriggerPoint getTriggerPoint(String id) {
		return (ITriggerPoint) triggerMap.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.ITriggerPointManager#getDefinedTriggerPointIds()
	 */
	public Set getDefinedTriggerPointIds() {
		return triggerMap.entrySet();
	}
}
