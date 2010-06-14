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
package org.eclipse.e4.ui.internal.di;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.di.internal.extensions.EventObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.EventHandler;

public class UIEventObjectSupplier extends EventObjectSupplier {

	class UIEventHandler implements EventHandler {

		final protected IRequestor requestor;

		public UIEventHandler(IRequestor requestor) {
			this.requestor = requestor;
		}

		public void handleEvent(org.osgi.service.event.Event event) {
			Object data = event.getProperty(EventUtils.DATA);
			addCurrentEvent(event.getTopic(), data);
			requestor.resolveArguments();
			removeCurrentEvent(event.getTopic());
			Display display = getDisplay();
			if (display == null || display.isDisposed()) {
				if (logger != null)
					logger.log(Level.WARNING, "No display found to process UI event " + event);
				return;
			}
			display.syncExec(new Runnable() {
				public void run() {
					requestor.execute();
				}
			});
		}
		
		private Display getDisplay() {
			if (contextDisplay != null)
				return contextDisplay;
			Display display = Display.getCurrent();
			if (display != null)
				return display;
			try {
				return Display.getDefault();
			} catch (SWTException e) {
				return null;
			}
		}
	}
	
	@Inject @Optional
	protected Display contextDisplay;
	
	@Inject @Optional
	protected Logger logger;

	protected EventHandler makeHandler(IRequestor requestor) {
		return new UIEventHandler(requestor);
	}

	protected String getTopic(IObjectDescriptor descriptor) {
		if (descriptor == null)
			return null;
		UIEventTopic qualifier = descriptor.getQualifier(UIEventTopic.class);
		return qualifier.value();
	}

}
