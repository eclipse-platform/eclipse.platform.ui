/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sopot Cela - initial API, implementation and fixes
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.addons;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;

/**
 * Process the additions and removals of handlers on the model
 */
public class HandlerProcessingAddon {

	/**
	 * Do initial check of handlers and their context upon creation
	 *
	 * @param application
	 * @param modelService
	 */
	@PostConstruct
	public void postConstruct(MApplication application, EModelService modelService) {
		List<MHandlerContainer> findElements = modelService.findElements(application, null, MHandlerContainer.class);
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
	}

	/**
	 * Responds to the coming and goings of handlers in the application model by activating and
	 * deactivating them accordingly.
	 *
	 * @param event
	 *            The event thrown in the event bus
	 */
	@Inject
	@Optional
	public void handleHandlerEvent(@EventTopic(UIEvents.HandlerContainer.TOPIC_HANDLERS) Event event) {
		if ((event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MHandlerContainer)
				&& (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MContext)) {
			MHandlerContainer handlerContainer = (MHandlerContainer) event.getProperty(UIEvents.EventTags.ELEMENT);
			if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
				if (event.getProperty(UIEvents.EventTags.NEW_VALUE) instanceof MHandler) {
					MHandler handler = (MHandler) event.getProperty(UIEvents.EventTags.NEW_VALUE);
					MContext mContext = (MContext) handlerContainer;
					IEclipseContext context = mContext.getContext();
					if (context != null) {
						processActiveHandler(handler, context);
					}
				}
			} else if (UIEvents.EventTypes.REMOVE.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
				if (event.getProperty(UIEvents.EventTags.OLD_VALUE) instanceof MHandler) {
					MHandler handler = (MHandler) event.getProperty(UIEvents.EventTags.OLD_VALUE);
					MContext mContext = (MContext) handlerContainer;
					IEclipseContext context = mContext.getContext();
					if (context != null) {
						MCommand command = handler.getCommand();
						if (command != null) {
							String commandId = command.getElementId();
							EHandlerService handlerService = context.get(EHandlerService.class);
							handlerService.deactivateHandler(commandId, handler.getObject());
						}
					}
				}

			}

		}

	}

	/**
	 * Responds to the setting of contexts of handlers in the application model and reacts
	 * accordingly.
	 *
	 * @param event
	 *            The event which signals the setting of the context.
	 */

	@Inject
	@Optional
	public void handleContextEvent(@EventTopic(UIEvents.Context.TOPIC_CONTEXT) Event event) {
		Object origin = event.getProperty(UIEvents.EventTags.ELEMENT);
		Object context = event.getProperty(UIEvents.EventTags.NEW_VALUE);
		if ((origin instanceof MHandlerContainer)
				&& (UIEvents.EventTypes.SET.equals(event.getProperty(UIEvents.EventTags.TYPE)) && context instanceof IEclipseContext)) {
			MHandlerContainer handlerContainer = (MHandlerContainer) origin;
			IEclipseContext castedContext = (IEclipseContext) context;
			for (MHandler mHandler : handlerContainer.getHandlers()) {
				processActiveHandler(mHandler, castedContext);
			}

		}
	}

	/**
	 * @param handler
	 * @param context
	 */
	private void processActiveHandler(MHandler handler, IEclipseContext context) {
		MCommand command = handler.getCommand();
		if (command == null) {
			return;
		}
		String commandId = command.getElementId();
		if (handler.getObject() == null) {
			IContributionFactory contributionFactory = context.get(IContributionFactory.class);
			handler.setObject(contributionFactory.create(handler.getContributionURI(), context));
		}
		EHandlerService handlerService = context.get(EHandlerService.class);
		handlerService.activateHandler(commandId, handler.getObject());
	}

}