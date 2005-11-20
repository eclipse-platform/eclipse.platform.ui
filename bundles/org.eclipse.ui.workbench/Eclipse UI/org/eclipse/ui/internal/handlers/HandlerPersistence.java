/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.1
 */
final class HandlerPersistence extends RegistryPersistence {

	/**
	 * The handler activations that have come from the registry. This is used to
	 * flush the activations when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private static final Collection handlerActivations = new ArrayList();

	/**
	 * The index of the command elements in the indexed array.
	 * 
	 * @see HandlerPersistence#read(IHandlerService)
	 */
	private static final int INDEX_COMMAND_DEFINITIONS = 0;

	/**
	 * The index of the command elements in the indexed array.
	 * 
	 * @see HandlerPersistence#read(IHandlerService)
	 */
	private static final int INDEX_HANDLER_DEFINITIONS = 1;

	/**
	 * The index of the handler submissions in the indexed array.
	 * 
	 * @see HandlerPersistence#read(IHandlerService)
	 */
	private static final int INDEX_HANDLER_SUBMISSIONS = 2;

	/**
	 * Deactivates all of the activations made by this class, and then clears
	 * the collection. This should be called before every read.
	 * 
	 * @param handlerService
	 *            The service handling the activations; must not be
	 *            <code>null</code>.
	 */
	private static final void clearActivations(
			final IHandlerService handlerService) {
		handlerService.deactivateHandlers(handlerActivations);
		handlerActivations.clear();
	}

	/**
	 * Reads the default handlers from an array of command elements from the
	 * commands extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param handlerService
	 *            The handler service to which the handlers should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readDefaultHandlersFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IHandlerService handlerService) {
		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			/*
			 * Read out the command identifier. This was already checked by
			 * <code>CommandPersistence</code>, so we'll just ignore any
			 * problems here.
			 */
			final String commandId = readOptional(
					configurationElement, ATTRIBUTE_ID);
			if (commandId == null) {
				continue;
			}

			// Check to see if we have a default handler of any kind.
			if ((configurationElement.getAttribute(ATTRIBUTE_DEFAULT_HANDLER) == null)
					&& (configurationElement
							.getChildren(ELEMENT_DEFAULT_HANDLER).length == 0)) {
				continue;
			}

			handlerActivations.add(handlerService.activateHandler(commandId,
					new HandlerProxy(configurationElement,
							ATTRIBUTE_DEFAULT_HANDLER)));
		}
	}

	/**
	 * Reads all of the handlers from the handlers extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param handlerService
	 *            The handler service to which the handlers should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readHandlersFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IHandlerService handlerService) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(
					configurationElement, ATTRIBUTE_COMMAND_ID, warningsToLog,
					"Handlers need a command id"); //$NON-NLS-1$
			if (commandId == null) {
				continue;
			}

			// Check to see if we have a handler class.
			if (!checkClass(configurationElement, warningsToLog,
					"Handlers need a class", commandId)) { //$NON-NLS-1$
				continue;
			}

			// Get the activeWhen and enabledWhen expressions.
			final Expression activeWhenExpression = readWhenElement(
					configurationElement, ELEMENT_ACTIVE_WHEN, commandId,
					warningsToLog);
			final Expression enabledWhenExpression = readWhenElement(
					configurationElement, ELEMENT_ENABLED_WHEN, commandId,
					warningsToLog);

			handlerActivations.add(handlerService.activateHandler(commandId,
					new HandlerProxy(configurationElement, ATTRIBUTE_CLASS,
							enabledWhenExpression, handlerService),
					activeWhenExpression));
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the handlers from the 'org.eclipse.ui.handlers' extension point."); //$NON-NLS-1$
	}

	/**
	 * Reads all of the handler submissions from the commands extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param handlerService
	 *            The handler service to which the handlers should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readHandlerSubmissionsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IHandlerService handlerService) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(
					configurationElement, ATTRIBUTE_COMMAND_ID, warningsToLog,
					"Handler submissions need a command id"); //$NON-NLS-1$
			if (commandId == null) {
				continue;
			}

			handlerActivations.add(handlerService.activateHandler(commandId,
					new LegacyHandlerWrapper(new LegacyHandlerProxy(
							configurationElement))));
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the handler submissions from the 'org.eclipse.ui.commands' extension point."); //$NON-NLS-1$
	}

	/**
	 * The handler service with which this persistence class is associated. This
	 * value must not be <code>null</code>.
	 */
	private final IHandlerService handlerService;

	/**
	 * Constructs a new instance of <code>HandlerPersistence</code>.
	 */
	HandlerPersistence(final IHandlerService handlerService) {
		this.handlerService = handlerService;
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		/*
		 * Handlers will need to be re-read (i.e., re-verified) if any of the
		 * handler extensions change (i.e., handlers, commands), or if any of
		 * the command extensions change (i.e., action definitions).
		 */
		final IExtensionDelta[] handlerDeltas = event.getExtensionDeltas(
				PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_HANDLERS);
		if (handlerDeltas.length == 0) {
			final IExtensionDelta[] commandDeltas = event.getExtensionDeltas(
					PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_COMMANDS);
			if (commandDeltas.length == 0) {
				final IExtensionDelta[] actionDefinitionDeltas = event
						.getExtensionDeltas(PlatformUI.PLUGIN_ID,
								IWorkbenchConstants.PL_ACTION_DEFINITIONS);
				if (actionDefinitionDeltas.length == 0) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Reads all of the handlers from the registry
	 * 
	 * @param handlerService
	 *            The handler service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	protected final void read() {
		super.read();

		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int commandDefinitionCount = 0;
		int handlerDefinitionCount = 0;
		int handlerSubmissionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[3][];

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] commandsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (int i = 0; i < commandsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = commandsExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a handler submission or a command definition.
			if (ELEMENT_HANDLER_SUBMISSION.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_HANDLER_SUBMISSIONS, handlerSubmissionCount++);
			} else if (ELEMENT_COMMAND.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_COMMAND_DEFINITIONS, commandDefinitionCount++);
			}
		}

		// Sort the handler extension point based on element name.
		final IConfigurationElement[] handlersExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_HANDLERS);
		for (int i = 0; i < handlersExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = handlersExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a handler submission or a command definition.
			if (ELEMENT_HANDLER.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_HANDLER_DEFINITIONS, handlerDefinitionCount++);
			}
		}

		clearActivations(handlerService);
		readDefaultHandlersFromRegistry(
				indexedConfigurationElements[INDEX_COMMAND_DEFINITIONS],
				commandDefinitionCount, handlerService);
		readHandlerSubmissionsFromRegistry(
				indexedConfigurationElements[INDEX_HANDLER_SUBMISSIONS],
				handlerSubmissionCount, handlerService);
		readHandlersFromRegistry(
				indexedConfigurationElements[INDEX_HANDLER_DEFINITIONS],
				handlerDefinitionCount, handlerService);
	}
}
