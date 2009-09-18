/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services.events;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.osgi.service.event.EventHandler;

/**
 * To obtain an instance of the event broker service from the @link {@link IEclipseContext} 
 * context, use 
 * <pre>
 * 	(IEventBroker) context.get(IContextConstants.EVENT)
 * </pre>
 * <p>
 * To create a new instance of the event broker service, use @link {@link EventBrokerFactory}.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEventBroker {
	
	/**
	 * The name of the event attribute used by default to pass event data.
	 */
	public String DATA = "data";

	/**
	 * Publish event synchronously (the method does not return until the event is processed).
	 * @param topic topic of the event to be published
	 * @param data data to be published with the event
	 * @return <code> true if this operation was performed successfully; <code>false</code> otherwise
	 */
	public boolean send(String topic, Object data);

	/**
	 * Publish event asynchronously (this method returns immediately).
	 * @param topic topic of the event to be published
	 * @param data data to be published with the event
	 * @return <code> true if this operation was performed successfully; <code>false</code> otherwise
	 */
	public boolean post(String topic, Object data);

	/**
	 * Subscribe for events on the given topic.
	 * @param topic topic of interest 
	 * @param filter filter for events, might be <code>null</code>
	 * @param eventHandler object to call when an event of interest arrives  
	 * @return <code> true if this operation was performed successfully; <code>false</code> otherwise
	 */
	public boolean subscribe(String topic, String filter, EventHandler eventReceiver);

	/**
	 * Unsubscribe handler previously registered using {@link #subscribe(String, String, EventHandler)}.
	 * @param eventReceiver
	 * @return <code> true if this operation was performed successfully; <code>false</code> otherwise
	 */
	public boolean unsubscribe(EventHandler eventReceiver);
}
