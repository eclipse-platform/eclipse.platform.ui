/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

/**
 * A model proxy represents a model for a specific presentation context and
 * fires deltas to notify listeners of changes in the model. A model proxy
 * is created by a model proxy factory.
 * <p>
 * When an element is added to an asynchronous viewer, its associated model proxy
 * factory is queried to create a model proxy for that element. The model proxy
 * is then installed into the viewer and the viewer listens to model deltas
 * in order to update that element. Generally, a model proxy factory creates
 * model proxies for root elements in a model, and then represents all elements
 * within that model for a specific presentation context. 
 * </p>
 * <p>
 * Clients may implement this interface. Implementation of this interface
 * must subclass {@link AbstractModelProxy}.
 * </p>
 * @see IModelDelta
 * @see IModelProxyFactory
 * @see IModelChangedListener
 * @since 3.2
 */
public interface IModelProxy {

	/**
	 * Notification this model proxy has been created and is about to be installed
	 * in the following context. This is the first method called after a model proxy
	 * is created.
	 * <p>
	 * This method is called by the asynchronous viewer framework and should not
	 * be called by clients.
	 * </p>
	 * @param context presentation context in which the proxy will be installed
	 */
	public void init(IPresentationContext context);
	
	/** 
	 * Notification this model proxy has been installed in its presentation 
	 * context. This indicates that the model proxy has been created and registered
	 * model change listeners are ready to process deltas.
	 * <p>
	 * This method is called by the asynchronous viewer framework and should not
	 * be called by clients.
	 * </p>
	 */
	public void installed();
	
	/**
	 * Disposes this model proxy.
	 * <p>
	 * This method is called by the asynchronous viewer framework and should not
	 * be called by clients.
	 * </p>
	 */
	public void dispose();
	
	/**
	 * Registers the given listener for model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void addModelChangedListener(IModelChangedListener listener);
	
	/**
	 * Deregisters the given listener from model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void removeModelChangedListener(IModelChangedListener listener);
	
}
