/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.team.ui.mapping.*;

/**
 * A synchronize operation context that supports caching of
 * properties relevant to the operation and the registering of
 * dispose listeners.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.team.ui.mapping.ISynchronizationCache
 * @since 3.2
 */
public class SynchronizationCache implements ISynchronizationCache {

	Map properties;
	ListenerList listeners;
	private final ISynchronizationContext context;
	
	/**
	 * CCreate an empty cache
	 */
	public SynchronizationCache(ISynchronizationContext context) {
		this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizeOperationContext#addProperty(java.lang.String, java.lang.Object)
	 */
	public synchronized void addProperty(String name, Object value) {
		if (properties == null) {
			properties = new HashMap();
		}
		properties.put(name, value);
	}

	public synchronized Object getProperty(String name) {
		if (properties == null)
			return null;
		return properties.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizeOperationContext#removeProperty(java.lang.String)
	 */
	public synchronized void removeProperty(String name) {
		if (properties != null)
			properties.remove(name);
		if (properties.isEmpty()) {
			properties = null;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizeOperationContext#addDisposeListener(org.eclipse.team.ui.mapping.IDisposeListener)
	 */
	public synchronized void addDisposeListener(IDisposeListener listener) {
		if (listeners == null)
			listeners = new ListenerList(ListenerList.IDENTITY);
		listeners.add(listener);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizeOperationContext#removeDisposeListener(org.eclipse.team.ui.mapping.IDisposeListener)
	 */
	public synchronized void removeDisposeListener(IDisposeListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#dispose()
	 */
	public void dispose() {
		if (listeners != null) {
			Object[] allListeners = listeners.getListeners();
			for (int i = 0; i < allListeners.length; i++) {
				final Object listener = allListeners[i];
				Platform.run(new SafeRunnable(){
					public void run() throws Exception {
						((IDisposeListener)listener).contextDisposed(context);
					}
				});
			}
		}
		properties = null;
	}
	
}
