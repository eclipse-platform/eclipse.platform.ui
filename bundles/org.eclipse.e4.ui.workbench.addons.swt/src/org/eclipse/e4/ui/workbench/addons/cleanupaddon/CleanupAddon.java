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

package org.eclipse.e4.ui.workbench.addons.cleanupaddon;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.PostConstruct;
import org.eclipse.e4.core.di.annotations.PreDestroy;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class CleanupAddon {
	@Inject
	protected IEventBroker eventBroker;

	private EventHandler childrenHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);
			if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				final MElementContainer<?> container = (MElementContainer<?>) changedObj;
				if (container instanceof MApplication) {
					// the application should not be unrendered
					return;
				}

				Display display = Display.getCurrent();

				// Stall the removal to handle cases where the container is only transiently empty
				if (display != null) {
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							// Don't mess with editor stacks (for now)
							if (container.getTags().contains("EditorStack")) //$NON-NLS-1$
								return;

							// Remove it from the model if it's empty
							if (container.getChildren().size() == 0) {
								container.setToBeRendered(false);
								container.getParent().getChildren().remove(container);
							}
						}
					});
				}
			}
		}
	};

	private EventHandler renderingChangeHandler = new EventHandler() {
		public void handleEvent(Event event) {
			MUIElement changedObj = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
			MElementContainer<MUIElement> container = changedObj.getParent();

			// Don't mess with editor stacks (for now)
			if (container.getTags().contains("EditorStack")) //$NON-NLS-1$
				return;

			Boolean toBeRendered = (Boolean) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (toBeRendered) {
				// Bring the container back if one of its children goes visible
				if (!container.isToBeRendered())
					container.setToBeRendered(true);
			} else {
				int visCount = 0;
				for (MUIElement element : container.getChildren()) {
					if (element.isToBeRendered())
						visCount++;
				}

				// Remove stacks with no visible children from the display (but not the
				// model)
				if (visCount == 0) {
					container.setToBeRendered(false);
				}
			}
		}
	};

	@PostConstruct
	void init(IEclipseContext context) {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), childrenHandler);
		eventBroker.subscribe(
				UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.TOBERENDERED),
				renderingChangeHandler);
	}

	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(renderingChangeHandler);
	}
}
