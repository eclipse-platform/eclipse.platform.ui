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

import org.eclipse.e4.ui.services.internal.events.EventBroker;



/**
 * Use this class to obtain an instance of {@link IEventBroker}.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be sub-classed by clients.
 */

public class EventBrokerFactory {
	public static IEventBroker newEventBroker() {
		return new EventBroker();
	}
}