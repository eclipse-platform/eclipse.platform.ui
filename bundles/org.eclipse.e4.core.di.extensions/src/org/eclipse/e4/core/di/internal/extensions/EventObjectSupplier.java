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
package org.eclipse.e4.core.di.internal.extensions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.IRequestor;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.PreDestroy;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.internal.di.shared.CoreLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class EventObjectSupplier extends AbstractObjectSupplier {

	// This is a temporary code to ensure that bundle containing
	// EventAdmin implementation is started. This code it to be removed once
	// the proper method to start EventAdmin is added.
	static {
		if (getEventAdmin() == null) {
			Bundle[] bundles = DIEActivator.getDefault().getBundleContext().getBundles();
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

	protected Map<String, Object> currentEvents = new HashMap<String, Object>();

	class DIEventHandler implements EventHandler {

		final private IRequestor requestor;

		public DIEventHandler(IRequestor requestor) {
			this.requestor = requestor;
		}

		public void handleEvent(org.osgi.service.event.Event event) {
			if (requestor.getRequestingObject() == null) {
				unsubscribe(requestor);
				return;
			}

			IInjector requestorInjector = requestor.getInjector();
			if (requestorInjector != null) {
				Object data = event.getProperty(EventUtils.DATA);
				addCurrentEvent(event.getTopic(), data);
				boolean resolved = requestorInjector.resolveArguments(requestor, requestor
						.getPrimarySupplier());
				removeCurrentEvent(event.getTopic());
				if (resolved) {
					try {
						requestor.execute();
					} catch (InvocationTargetException e) {
						CoreLogger.logError("Injection failed for the object \""
								+ requestor.getRequestingObject().toString()
								+ "\". Unable to execute \"" + requestor.toString() + "\"", e);
						return;
					} catch (InstantiationException e) {
						CoreLogger.logError("Injection failed for the object \""
								+ requestor.getRequestingObject().toString()
								+ "\". Unable to execute \"" + requestor.toString() + "\"", e);
						return;
					}
				}
			}
		}
	}

	// A combo of { IRequestor + topic } used in Map lookups
	static private class Subscriber {
		private IRequestor requestor;
		private String topic;

		public Subscriber(IRequestor requestor, String topic) {
			super();
			this.requestor = requestor;
			this.topic = topic;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((requestor == null) ? 0 : requestor.hashCode());
			result = prime * result + ((topic == null) ? 0 : topic.hashCode());
			return result;
		}

		public IRequestor getRequestor() {
			return requestor;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Subscriber other = (Subscriber) obj;
			if (requestor == null) {
				if (other.requestor != null)
					return false;
			} else if (!requestor.equals(other.requestor))
				return false;
			if (topic == null) {
				if (other.topic != null)
					return false;
			} else if (!topic.equals(other.topic))
				return false;
			return true;
		}

	}

	private Map<Subscriber, ServiceRegistration> registrations = new HashMap<Subscriber, ServiceRegistration>();

	protected void addCurrentEvent(String topic, Object data) {
		synchronized (currentEvents) {
			currentEvents.put(topic, data);
		}
	}

	protected void removeCurrentEvent(String topic) {
		synchronized (currentEvents) {
			currentEvents.remove(topic);
		}
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor) {
		if (descriptor == null)
			return null;
		String topic = getTopic(descriptor);
		EventAdmin eventAdmin = getEventAdmin();
		if (topic == null || eventAdmin == null || topic.length() == 0)
			return IInjector.NOT_A_VALUE;

		subscribe(topic, eventAdmin, requestor);

		if (currentEvents.containsKey(topic))
			return currentEvents.get(topic);
		return IInjector.NOT_A_VALUE;
	}

	private void subscribe(String topic, EventAdmin eventAdmin, IRequestor requestor) {
		Subscriber subscriber = new Subscriber(requestor, topic);
		synchronized (registrations) {
			if (registrations.containsKey(subscriber))
				return;
		}
		BundleContext bundleContext = DIEActivator.getDefault().getBundleContext();
		if (bundleContext == null) {
			CoreLogger.logError(
					"Unable to subscribe to events: DI extension bundle is not activated",
					new InjectionException());
			return;
		}
		String[] topics = new String[] { topic };
		Dictionary<String, Object> d = new Hashtable<String, Object>();
		d.put(EventConstants.EVENT_TOPIC, topics);
		EventHandler wrappedHandler = makeHandler(requestor);
		ServiceRegistration registration = bundleContext.registerService(EventHandler.class
				.getName(), wrappedHandler, d);
		// due to the way requestors are constructed this limited synch should be OK
		synchronized (registrations) {
			registrations.put(subscriber, registration);
		}
	}

	protected EventHandler makeHandler(IRequestor requestor) {
		return new DIEventHandler(requestor);
	}

	protected String getTopic(IObjectDescriptor descriptor) {
		if (descriptor == null)
			return null;
		Object qualifier = descriptor.getQualifier(EventTopic.class);
		return ((EventTopic) qualifier).value();
	}

	@Override
	public Object[] get(IObjectDescriptor[] descriptors, IRequestor requestor) {
		Object[] result = new Object[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			result[i] = get(descriptors[i], requestor);
		}
		return result;
	}

	static private EventAdmin getEventAdmin() {
		return DIEActivator.getDefault().getEventAdmin();
	}

	protected void unsubscribe(IRequestor requestor) {
		synchronized (registrations) {
			Iterator<Entry<Subscriber, ServiceRegistration>> i = registrations.entrySet()
					.iterator();
			while (i.hasNext()) {
				Entry<Subscriber, ServiceRegistration> entry = i.next();
				Subscriber key = entry.getKey();
				if (key.getRequestor() != requestor)
					continue;
				ServiceRegistration registration = entry.getValue();
				registration.unregister();
				i.remove();
			}
		}
	}

	@PreDestroy
	public void dispose() {
		ServiceRegistration[] array;
		synchronized (registrations) {
			Collection<ServiceRegistration> values = registrations.values();
			array = values.toArray(new ServiceRegistration[values.size()]);
			registrations.clear();
		}
		for (int i = 0; i < array.length; i++) {
			array[i].unregister();
		}
	}
}
