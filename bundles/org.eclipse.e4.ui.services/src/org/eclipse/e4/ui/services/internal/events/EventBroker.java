/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services.internal.events;

import org.eclipse.e4.core.services.events.IEventBroker;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.ui.internal.services.Activator;
import org.eclipse.e4.ui.internal.services.ServiceMessages;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class EventBroker implements IEventBroker {
	
	// TBD synchronization
	private Map<EventHandler, ServiceRegistration> registrations = new HashMap<EventHandler, ServiceRegistration>();

	@Inject
	Logger logger;
	
	// This is a temporary code to ensure that bundle containing
	// EventAdmin implementation is started. This code it to be removed once
	// the proper method to start EventAdmin is added.
	static {
		EventAdmin eventAdmin = Activator.getDefault().getEventAdmin();
		if (eventAdmin == null) {
			Bundle[] bundles = Activator.getDefault().getBundleContext().getBundles();
			for (Bundle bundle : bundles) {
				if (!"org.eclipse.equinox.event".equals(bundle.getSymbolicName()))
					continue;
				try {
					bundle.start(Bundle.START_TRANSIENT);
				} catch (BundleException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	public EventBroker() {
		// placeholder
	}

	public boolean send(String topic, Object data) {
		Event event = constructEvent(topic, data);
		EventAdmin eventAdmin = Activator.getDefault().getEventAdmin();
		if (eventAdmin == null) {
			logger.error(NLS.bind(ServiceMessages.NO_EVENT_ADMIN, event.toString()));
			return false;
		}
		eventAdmin.sendEvent(event);
		return true;
	}

	public boolean post(String topic, Object data) {
		Event event = constructEvent(topic, data);
		EventAdmin eventAdmin = Activator.getDefault().getEventAdmin();
		if (eventAdmin == null) {
			logger.error(NLS.bind(ServiceMessages.NO_EVENT_ADMIN, event.toString()));
			return false;
		}
		eventAdmin.postEvent(event);
		return true;
	}

	private Event constructEvent(String topic, Object data) {
		Event event;
		if (data instanceof Dictionary<?,?>) {
			event = new Event(topic, (Dictionary<?,?>)data);
		} else if (data instanceof Map<?,?>) {
			event = new Event(topic, (Map<?,?>)data);
		} else {
			Dictionary<String, Object> d = new Hashtable<String, Object>(2);
			d.put(EventConstants.EVENT_TOPIC, topic);
			if (data != null)
				d.put(IEventBroker.DATA, data);
			event = new Event(topic, d);
		}
		return event;
	}

	public boolean subscribe(String topic, EventHandler eventHandler) {
		return subscribe(topic, null, eventHandler, false);
	}
	
	public boolean subscribe(String topic, String filter, EventHandler eventHandler, boolean headless) {
		BundleContext bundleContext = Activator.getDefault().getBundleContext();
		if (bundleContext == null) {
			logger.error(NLS.bind(ServiceMessages.NO_BUNDLE_CONTEXT, topic));
			return false;
		}
		String[] topics = new String[] {topic};
		Dictionary<String, Object> d = new Hashtable<String, Object>();
		d.put(EventConstants.EVENT_TOPIC, topics);
		if (filter != null)
			d.put(EventConstants.EVENT_FILTER, filter);
		EventHandler wrappedHandler = new UIEventHandler(eventHandler, headless);
		ServiceRegistration registration = bundleContext.registerService(EventHandler.class.getName(), wrappedHandler, d);
		registrations.put(eventHandler, registration);
		return true;
	}

	public boolean unsubscribe(EventHandler eventHandler) {
		ServiceRegistration registration = (ServiceRegistration) registrations.remove(eventHandler);
		if (registration == null)
			return false;
		registration.unregister();
		return true;
	}
	
	@PreDestroy
	void dispose() {
		Collection<ServiceRegistration> values = registrations.values();
		ServiceRegistration[] array = values.toArray(new ServiceRegistration[values.size()]);
		registrations.clear();
		for (int i = 0; i < array.length; i++) {
			//System.out.println("EventBroker dispose:" + array[i] + ")");
			array[i].unregister();
		}
	}
}
