/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services.events;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.internal.events.EventBroker;



/**
 * Use this class to obtain an instance of {@link IEventBroker}.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be sub-classed by clients.
 */

public class EventBrokerFactory extends ContextFunction {
	@Override
	public Object compute(IEclipseContext context) {
        EventBroker broker = context.getLocal(EventBroker.class);
		if (broker == null) {
            broker = ContextInjectionFactory.make(EventBroker.class, context);
            context.set(EventBroker.class, broker);
		}
		return broker;
	}
}

