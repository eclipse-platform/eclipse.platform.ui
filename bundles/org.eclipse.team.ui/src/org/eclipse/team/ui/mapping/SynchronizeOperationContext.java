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
package org.eclipse.team.ui.mapping;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;

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
 * @see org.eclipse.team.ui.mapping.ISynchronizeOperationContext
 * @since 3.2
 */
public abstract class SynchronizeOperationContext extends TeamViewerContext implements ISynchronizeOperationContext {

	Map properties;
	ListenerList listeners;
	
	/**
	 * Create an operation context for the given input.
	 * @param input the input of the context
	 */
	public SynchronizeOperationContext(IResourceMappingOperationInput input) {
		super(input);
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
			listeners = new ListenerList();
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
						((IDisposeListener)listener).contextDisposed(SynchronizeOperationContext.this);
					}
				});
			}
		}
		properties = null;
	}
	
}
