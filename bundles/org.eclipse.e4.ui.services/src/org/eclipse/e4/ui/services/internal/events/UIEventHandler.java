/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services.internal.events;

import org.eclipse.e4.ui.di.UISynchronize;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * The helper will properly place UI-aware consumers on the main thread.
 */
public class UIEventHandler implements EventHandler {

	final private EventHandler eventHandler;
	final private UISynchronize uiSync;

	public UIEventHandler(EventHandler eventHandler, UISynchronize uiSync) {
		this.eventHandler = eventHandler;
		this.uiSync = uiSync;
	}

	@Override
	public void handleEvent(final Event event) {
		if (uiSync == null)
			eventHandler.handleEvent(event);
		else {
			uiSync.syncExec(new Runnable() {

				@Override
				public void run() {
					eventHandler.handleEvent(event);
				}
			});
		}
	}
}
