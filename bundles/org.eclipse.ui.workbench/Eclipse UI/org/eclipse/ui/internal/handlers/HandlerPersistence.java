/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;
import org.eclipse.ui.services.IEvaluationService;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.1
 */
public final class HandlerPersistence extends RegistryPersistence {

	/**
	 * The index of the command elements in the indexed array.
	 * 
	 * @see HandlerPersistence#read()
	 */
	private static final int INDEX_COMMAND_DEFINITIONS = 0;

	/**
	 * The index of the command elements in the indexed array.
	 * 
	 * @see HandlerPersistence#read()
	 */
	private static final int INDEX_HANDLER_DEFINITIONS = 1;

	/**
	 * The index of the handler submissions in the indexed array.
	 * 
	 * @see HandlerPersistence#read()
	 */
	private static final int INDEX_HANDLER_SUBMISSIONS = 2;

	/**
	 * The handler activations that have come from the registry. This is used to
	 * flush the activations when the registry is re-read. This value is never
	 * <code>null</code>
	 */
	private final Collection handlerActivations = new ArrayList();

	/**
	 * The handler service with which this persistence class is associated. This
	 * value must not be <code>null</code>.
	 */
	private final IHandlerService handlerService;

	private IEvaluationService evaluationService;

	/**
	 * Constructs a new instance of <code>HandlerPersistence</code>.
	 * 
	 * @param handlerService
	 *            The handler service with which the handlers should be
	 *            registered; must not be <code>null</code>.
	 * @param evaluationService
	 *            The evaluation service used by handler proxies with enabled
	 *            when expressions
	 */
	HandlerPersistence(final IHandlerService handlerService,
			IEvaluationService evaluationService) {
		this.handlerService = handlerService;
		this.evaluationService = evaluationService;
	}

	/**
	 * Deactivates all of the activations made by this class, and then clears
	 * the collection. This should be called before every read.
	 * 
	 * @param handlerService
	 *            The service handling the activations; must not be
	 *            <code>null</code>.
	 */
	private final void clearActivations(final IHandlerService handlerService) {
		handlerService.deactivateHandlers(handlerActivations);
		handlerActivations.clear();
	}

	public final void dispose() {
		super.dispose();
		clearActivations(handlerService);
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		return false;
	}

	public boolean handlersNeedUpdating(final IRegistryChangeEvent event) {
		/*
		 * Handlers will need to be re-read (i.e., re-verified) if any of the
		 * handler extensions change (i.e., handlers, commands), or if any of
		 * the command extensions change (i.e., action definitions).
		 */
		final IExtensionDelta[] handlerDeltas = event.getExtensionDeltas(
				PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_HANDLERS);
		if (handlerDeltas.length == 0) {
			final IExtensionDelta[] commandDeltas = event.getExtensionDeltas(
					PlatformUI.PLUGIN_ID,
					IWorkbenchRegistryConstants.PL_COMMANDS);
			if (commandDeltas.length == 0) {
				final IExtensionDelta[] actionDefinitionDeltas = event
						.getExtensionDeltas(
								PlatformUI.PLUGIN_ID,
								IWorkbenchRegistryConstants.PL_ACTION_DEFINITIONS);
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
		reRead();
	}

	public final void reRead() {
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
			if (TAG_HANDLER_SUBMISSION.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_HANDLER_SUBMISSIONS, handlerSubmissionCount++);
			} else if (TAG_COMMAND.equals(name)) {
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
			if (TAG_HANDLER.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_HANDLER_DEFINITIONS, handlerDefinitionCount++);
			}
		}

		clearActivations(handlerService);
		readDefaultHandlersFromRegistry(
				indexedConfigurationElements[INDEX_COMMAND_DEFINITIONS],
				commandDefinitionCount);
		readHandlerSubmissionsFromRegistry(
				indexedConfigurationElements[INDEX_HANDLER_SUBMISSIONS],
				handlerSubmissionCount);
		readHandlersFromRegistry(
				indexedConfigurationElements[INDEX_HANDLER_DEFINITIONS],
				handlerDefinitionCount);
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
	 */
	private final void readDefaultHandlersFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			/*
			 * Read out the command identifier. This was already checked by
			 * <code>CommandPersistence</code>, so we'll just ignore any
			 * problems here.
			 */
			final String commandId = readOptional(configurationElement, ATT_ID);
			if (commandId == null) {
				continue;
			}

			// Check to see if we have a default handler of any kind.
			if ((configurationElement.getAttribute(ATT_DEFAULT_HANDLER) == null)
					&& (configurationElement.getChildren(TAG_DEFAULT_HANDLER).length == 0)) {
				continue;
			}

			handlerActivations.add(handlerService
					.activateHandler(commandId, new HandlerProxy(
							configurationElement, ATT_DEFAULT_HANDLER)));
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
	private final void readHandlersFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(configurationElement,
					ATT_COMMAND_ID, warningsToLog, "Handlers need a command id"); //$NON-NLS-1$
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
					configurationElement, TAG_ACTIVE_WHEN, commandId,
					warningsToLog);
			if (activeWhenExpression == ERROR_EXPRESSION) {
				continue;
			}
			final Expression enabledWhenExpression = readWhenElement(
					configurationElement, TAG_ENABLED_WHEN, commandId,
					warningsToLog);
			if (enabledWhenExpression == ERROR_EXPRESSION) {
				continue;
			}

			final IHandler proxy = new HandlerProxy(configurationElement,
					ATT_CLASS, enabledWhenExpression, evaluationService);
			handlerActivations.add(handlerService.activateHandler(commandId,
					proxy, activeWhenExpression));

			// Read out the help context identifier.
			final String helpContextId = readOptional(configurationElement,
					ATT_HELP_CONTEXT_ID);
			handlerService.setHelpContextId(proxy, helpContextId);
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
	private final void readHandlerSubmissionsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount) {
		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = readRequired(configurationElement,
					ATT_COMMAND_ID, warningsToLog,
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
}
