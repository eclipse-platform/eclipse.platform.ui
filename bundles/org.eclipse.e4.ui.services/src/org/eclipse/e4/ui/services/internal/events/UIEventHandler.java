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
package org.eclipse.e4.ui.services.internal.events;

import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * The helper will properly place UI-aware consumers on the main thread.
 */
public class UIEventHandler implements EventHandler {
	
	final private EventHandler eventHandler;
	final private boolean headless;
	
	public UIEventHandler(EventHandler eventHandler, boolean headless) {
		this.eventHandler = eventHandler;
		this.headless = headless;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	public void handleEvent(final Event event) {
		if (headless)
			eventHandler.handleEvent(event);
		else {
			// This is very close to a no-op if run on the main thread.
			// Effectively, after some sanity checks, if the display thread
			// is the same as the current thread, the SWT does runnable.run().
			Display.getDefault().syncExec(new Runnable() { 
				public void run() {
					eventHandler.handleEvent(event);
				}
			});
		}
	}
}
