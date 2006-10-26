/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;

/**
 * @since 3.2
 */
public class SuspendTriggerAdapterFactory implements IAdapterFactory {
	
	private Map fSuspendTriggers = new HashMap(); 

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public synchronized Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(ISuspendTrigger.class)) {
			if (adaptableObject instanceof ILaunch) {
				Object trigger = fSuspendTriggers.get(adaptableObject);
				if (trigger == null) {
					trigger = new LaunchSuspendTrigger((ILaunch) adaptableObject, this);
					fSuspendTriggers.put(adaptableObject, trigger);
				}
				return trigger;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{ISuspendTrigger.class};
	}
	
	public synchronized void dispose(LaunchSuspendTrigger trigger) {
		fSuspendTriggers.remove(trigger.getLaunch());
	}

}
