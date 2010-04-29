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

import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.di.extensions.UIEventTopic;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.EventHandler;

public class UIEventObjectSupplier extends EventObjectSupplier {

	class UIEventHandler implements EventHandler {

		final protected IRequestor requestor;

		public UIEventHandler(IRequestor requestor) {
			this.requestor = requestor;
		}

		public void handleEvent(org.osgi.service.event.Event event) {
			IInjector requestorInjector = requestor.getInjector();
			if (requestorInjector != null) {
				Object data = event.getProperty(EventUtils.DATA);
				addCurrentEvent(event.getTopic(), data);
				requestorInjector.resolveArguments(requestor, requestor.getPrimarySupplier());
				removeCurrentEvent(event.getTopic());
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						requestor.execute();
					}
				});
			}
		}
	}

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
