/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Addon supporting standard drag and drop management
 */
public class DnDAddon {

	@Inject
	IEventBroker eventBroker;

	private EventHandler installHook = new EventHandler() {
		public void handleEvent(Event event) {
			MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MWindow))
				return;

			Widget widget = (Widget) event.getProperty(EventTags.NEW_VALUE);
			if (widget instanceof Shell && !widget.isDisposed()) {
				DnDManager theManager = (DnDManager) widget.getData("DnDManager"); //$NON-NLS-1$
				if (theManager == null) {
					theManager = new DnDManager((MWindow) changedElement);
					widget.setData("DnDManager", theManager); //$NON-NLS-1$
				}
			}
		}
	};

	@PostConstruct
	void hookListeners() {
		String topic = UIEvents.UIElement.TOPIC_WIDGET;
		eventBroker.subscribe(topic, null, installHook, false);
	}

	@PreDestroy
	void unhookListeners() {
		eventBroker.unsubscribe(installHook);
	}
}
