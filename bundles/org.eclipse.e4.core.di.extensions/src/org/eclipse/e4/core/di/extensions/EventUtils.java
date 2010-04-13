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
package org.eclipse.e4.core.di.extensions;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;

/**
 * DOC: no instantiate, no extend
 * 
 */
final public class EventUtils {

	// Same as IEventBroker.DATA
	final static public String DATA = "org.eclipse.e4.data"; //$NON-NLS-1$

	private EventUtils() {
		// prevents instantiation
	}

	static public boolean send(EventAdmin eventAdmin, String topic, Object data) {
		Event event = constructEvent(topic, data);
		eventAdmin.sendEvent(event);
		return true;
	}

	static public boolean post(EventAdmin eventAdmin, String topic, Object data) {
		Event event = constructEvent(topic, data);
		eventAdmin.postEvent(event);
		return true;
	}

	static public Event constructEvent(String topic, Object data) {
		Event event;
		if (data instanceof Dictionary<?, ?>) {
			event = new Event(topic, (Dictionary<?, ?>) data);
		} else if (data instanceof Map<?, ?>) {
			event = new Event(topic, (Map<?, ?>) data);
		} else {
			Dictionary<String, Object> d = new Hashtable<String, Object>(2);
			d.put(EventConstants.EVENT_TOPIC, topic);
			if (data != null)
				d.put(DATA, data);
			event = new Event(topic, d);
		}
		return event;
	}

}
