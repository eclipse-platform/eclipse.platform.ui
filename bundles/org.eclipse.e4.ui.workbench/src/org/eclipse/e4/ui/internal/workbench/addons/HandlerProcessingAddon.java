/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sopot Cela - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.addons;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Process the additions and removals of handlers on the model
 */
public class HandlerProcessingAddon {

	private EventHandler eventHandler;
	private EventHandler contextHandler;

	@Inject
	private IEventBroker eventBroker;

	/**
	 * Registers the listeners
	 * 
	 * @param application
	 * @param modelService
	 */
	@PostConstruct
	public void postConstruct(MApplication application, EModelService modelService) {
		List<MHandlerContainer> findElements = modelService.findElements(application, null,
				MHandlerContainer.class, null);
		for (MHandlerContainer mHandlerContainer : findElements) {
			if (mHandlerContainer instanceof MContext) {
				MContext mContext = (MContext) mHandlerContainer;
				IEclipseContext context = mContext.getContext();
				if (context != null) {
					for (MHandler mHandler : mHandlerContainer.getHandlers()) {
						processActiveHandler(mHandler, context);
					}
				}
			}
		}

		registerModelListeners();
	}

	private void registerModelListeners() {
		eventHandler = new EventHandler() {
			public void handleEvent(Event event) {
				if ((event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MHandlerContainer)
						&& (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MContext)) {
					MHandlerContainer handlerContainer = (MHandlerContainer) event
							.getProperty(UIEvents.EventTags.ELEMENT);
					if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
						if (event.getProperty(UIEvents.EventTags.NEW_VALUE) instanceof MHandler) {
							MHandler handler = (MHandler) event
									.getProperty(UIEvents.EventTags.NEW_VALUE);
							MContext mContext = (MContext) handlerContainer;
							IEclipseContext context = mContext.getContext();
							if (context != null) {
								processActiveHandler(handler, context);
							}
						}
					} else if (UIEvents.EventTypes.REMOVE.equals(event
							.getProperty(UIEvents.EventTags.TYPE))) {
						if (event.getProperty(UIEvents.EventTags.OLD_VALUE) instanceof MHandler) {
							MHandler handler = (MHandler) event
									.getProperty(UIEvents.EventTags.OLD_VALUE);
							MContext mContext = (MContext) handlerContainer;
							IEclipseContext context = mContext.getContext();
							if (context != null) {
								MCommand command = handler.getCommand();
								if (command != null) {
									String commandId = command.getElementId();
									EHandlerService handlerService = (EHandlerService) context
											.get(EHandlerService.class.getName());
									handlerService
											.deactivateHandler(commandId, handler.getObject());
								}
							}
						}

					}

				}

			}
		};

		contextHandler = new EventHandler() {

			public void handleEvent(Event event) {
				Object origin = event.getProperty(UIEvents.EventTags.ELEMENT);
				Object context = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if ((origin instanceof MHandlerContainer)
						&& (UIEvents.EventTypes.SET.equals(event
								.getProperty(UIEvents.EventTags.TYPE)) && context instanceof IEclipseContext)) {
					MHandlerContainer handlerContainer = (MHandlerContainer) origin;
					IEclipseContext castedContext = (IEclipseContext) context;
					for (MHandler mHandler : handlerContainer.getHandlers()) {
						processActiveHandler(mHandler, castedContext);
					}

				}

			}
		};
		eventBroker.subscribe(UIEvents.HandlerContainer.TOPIC_HANDLERS, eventHandler);
		eventBroker.subscribe(UIEvents.Context.TOPIC_CONTEXT, contextHandler);
	}

	/**
	 * Clean up
	 */
	@PreDestroy
	public void preDestroy() {
		unregisterModelListeners();
	}

	/**
	 * Unregisters the listeners
	 */
	private void unregisterModelListeners() {
		eventBroker.unsubscribe(eventHandler);
		eventBroker.unsubscribe(contextHandler);
	}

	/**
	 * @param handler
	 * @param context
	 */
	private void processActiveHandler(MHandler handler, IEclipseContext context) {
		MCommand command = handler.getCommand();
		if (command != null) {
			String commandId = command.getElementId();
			if (handler.getObject() == null) {
				IContributionFactory contributionFactory = (IContributionFactory) context
						.get(IContributionFactory.class.getName());
				handler.setObject(contributionFactory.create(handler.getContributionURI(), context));
			}
			EHandlerService handlerService = (EHandlerService) context.get(EHandlerService.class
					.getName());
			handlerService.activateHandler(commandId, handler.getObject());
		}
	}

}