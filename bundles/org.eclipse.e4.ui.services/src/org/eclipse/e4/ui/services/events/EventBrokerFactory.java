/*******************************************************************************
 * Copyright (c) 2009, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services.events;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.internal.events.EventBroker;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;

/**
 * Use this class to obtain an instance of {@link IEventBroker}.
 */
@Component(service = IContextFunction.class)
@IContextFunction.ServiceContextKey(IEventBroker.class)
public class EventBrokerFactory extends ContextFunction {

	// mandatory static reference to EventAdmin to ensure it is available before
	// the factory is activated

	@Reference
	void setEventAdmin(EventAdmin admin) {
		// don't need to do anything with the EventAdmin here
		// we only need to ensure it is available before starting this factory
	}

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		EventBroker broker = context.getLocal(EventBroker.class);
		if (broker == null) {
			broker = ContextInjectionFactory.make(EventBroker.class, context);
			context.set(EventBroker.class, broker);
		}
		return broker;
	}
}
