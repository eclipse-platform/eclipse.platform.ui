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
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.IRequestor;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.di.extensions.UIEventTopic;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.EventHandler;

public class UIEventObjectSupplier extends EventObjectSupplier {

	class UIEventHandler implements EventHandler {

		final private IRequestor requestor;

		public UIEventHandler(IRequestor requestor) {
			this.requestor = requestor;
		}

		public void handleEvent(org.osgi.service.event.Event event) {
			IInjector requestorInjector = requestor.getInjector();
			if (requestorInjector != null) {
				Object data = event.getProperty(EventUtils.DATA);
				addCurrentEvent(event.getTopic(), data);
				boolean resolved = requestorInjector.resolveArguments(requestor, requestor
						.getPrimarySupplier());
				removeCurrentEvent(event.getTopic());
				if (resolved) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							try {
								requestor.execute();
							} catch (InvocationTargetException e) {
								logError("Injection failed for the object \""
										+ requestor.getRequestingObject().toString()
										+ "\". Unable to execute \"" + requestor.toString() + "\"",
										e);
							} catch (InstantiationException e) {
								logError("Injection failed for the object \""
										+ requestor.getRequestingObject().toString()
										+ "\". Unable to execute \"" + requestor.toString() + "\"",
										e);
							}
						}
					});
				}
			}
		}
	}

	protected EventHandler makeHandler(IRequestor requestor) {
		return new UIEventHandler(requestor);
	}

	protected String getTopic(IObjectDescriptor descriptor) {
		if (descriptor == null)
			return null;
		Object qualifier = descriptor.getQualifier(UIEventTopic.class);
		return ((UIEventTopic) qualifier).value();
	}

}
