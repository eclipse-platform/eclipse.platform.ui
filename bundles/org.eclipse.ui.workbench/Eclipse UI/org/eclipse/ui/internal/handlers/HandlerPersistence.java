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

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.1
 */
final class HandlerPersistence {

	/**
	 * The name of the id attribute, which is used on command definitions.
	 */
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the command identifier attribute, which appears on a handler
	 * submission.
	 */
	private static final String ATTRIBUTE_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the class attribute, which appears on a handler definition.
	 */
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	/**
	 * The name of the default handler attribute, which appears on a command
	 * definition.
	 */
	private static final String ATTRIBUTE_DEFAULT_HANDLER = "defaultHandler"; //$NON-NLS-1$

	/**
	 * The name of the element storing a command.
	 */
	private static final String ELEMENT_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the active when element, which appears on a handler
	 * definition.
	 */
	private static final String ELEMENT_ACTIVE_WHEN = "activeWhen"; //$NON-NLS-1$

	/**
	 * The name of the class element, which appears on a handler definition.
	 */
	private static final String ELEMENT_CLASS = ATTRIBUTE_CLASS;

	/**
	 * The name of the default handler element, which appears on a command
	 * definition.
	 */
	private static final String ELEMENT_DEFAULT_HANDLER = ATTRIBUTE_DEFAULT_HANDLER;

	/**
	 * The name of the enabled when element, which appears on a handler
	 * definition.
	 */
	private static final String ELEMENT_ENABLED_WHEN = "enabledWhen"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler.
	 */
	private static final String ELEMENT_HANDLER = "handler"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler submission.
	 */
	private static final String ELEMENT_HANDLER_SUBMISSION = "handlerSubmission"; //$NON-NLS-1$

	/**
	 * The name of the commands extension point.
	 */
	private static final String EXTENSION_COMMANDS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_COMMANDS;

	/**
	 * The name of the commands extension point.
	 */
	private static final String EXTENSION_HANDLERS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_HANDLERS;
	
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
	 * Whether the preference and registry change listeners have been attached
	 * yet.
	 */
    private static boolean listenersAttached = false;

	/**
	 * Inserts the given element into the indexed two-dimensional array in the
	 * array at the index. The array is grown as necessary.
	 * 
	 * @param elementToAdd
	 *            The element to add to the indexed array; may be
	 *            <code>null</code>
	 * @param indexedArray
	 *            The two-dimensional array that is indexed by element type;
	 *            must not be <code>null</code>.
	 * @param index
	 *            The index at which the element should be added; must be a
	 *            valid index.
	 * @param currentCount
	 *            The current number of items in the array at the index.
	 */
	private static final void addElementToIndexedArray(
			final IConfigurationElement elementToAdd,
			final IConfigurationElement[][] indexedArray, final int index,
			final int currentCount) {
		final IConfigurationElement[] elements;
		if (currentCount == 0) {
			elements = new IConfigurationElement[1];
			indexedArray[index] = elements;
		} else {
			if (currentCount >= indexedArray[index].length) {
				final IConfigurationElement[] copy = new IConfigurationElement[indexedArray[index].length * 2];
				System.arraycopy(indexedArray[index], 0, copy, 0, currentCount);
				elements = copy;
				indexedArray[index] = elements;
			} else {
				elements = indexedArray[index];
			}
		}
		elements[currentCount] = elementToAdd;
	}
	
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
	 * Reads all of the handlers from the registry
	 * 
	 * @param handlerService
	 *            The handler service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	static final void read(final IHandlerService handlerService) {
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
		readDefaultHandlersFromCommandsExtensionPoint(
				indexedConfigurationElements[INDEX_COMMAND_DEFINITIONS],
				commandDefinitionCount, handlerService);
		readHandlerSubmissionsFromCommandsExtensionPoint(
				indexedConfigurationElements[INDEX_HANDLER_SUBMISSIONS],
				handlerSubmissionCount, handlerService);
		readHandlersFromHandlersExtensionPoint(
				indexedConfigurationElements[INDEX_HANDLER_DEFINITIONS],
				handlerDefinitionCount, handlerService);
		
		/*
		 * Adds listener so that future registry changes trigger an update of
		 * the command manager automatically.
		 */
		if (!listenersAttached) {
			registry.addRegistryChangeListener(new IRegistryChangeListener() {
				public final void registryChanged(
						final IRegistryChangeEvent event) {
					/*
					 * Handlers will need to be re-read (i.e., re-verified) if
					 * any of the handler extensions change (i.e., handlers,
					 * commands), or if any of the command extensions change
					 * (i.e., action definitions).
					 */
					final IExtensionDelta[] handlerDeltas = event
							.getExtensionDeltas(PlatformUI.PLUGIN_ID,
									IWorkbenchConstants.PL_HANDLERS);
					if (handlerDeltas.length == 0) {
						final IExtensionDelta[] commandDeltas = event
								.getExtensionDeltas(PlatformUI.PLUGIN_ID,
										IWorkbenchConstants.PL_COMMANDS);
						if (commandDeltas.length == 0) {
							final IExtensionDelta[] actionDefinitionDeltas = event
									.getExtensionDeltas(
											PlatformUI.PLUGIN_ID,
											IWorkbenchConstants.PL_ACTION_DEFINITIONS);
							if (actionDefinitionDeltas.length == 0) {
								return;
							}
						}
					}

					/*
					 * At least one of the deltas is non-zero, so re-read all of
					 * the bindings.
					 */
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							read(handlerService);
						}
					});
				}
			}, PlatformUI.PLUGIN_ID);

			listenersAttached = true;
		}
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
	private static final void readDefaultHandlersFromCommandsExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IHandlerService handlerService) {
		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = configurationElement
					.getAttribute(ATTRIBUTE_ID);
			if ((commandId == null) || (commandId.length() == 0)) {
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
	private static final void readHandlersFromHandlersExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IHandlerService handlerService) {
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = null;

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = configurationElement
					.getAttribute(ATTRIBUTE_COMMAND_ID);
			if ((commandId == null) || (commandId.length() == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Handlers need a command id: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				if (warningsToLog == null) {
					warningsToLog = new ArrayList();
				}
				warningsToLog.add(status);
				continue;
			}

			// Check to see if we have a handler class.
			if ((configurationElement.getAttribute(ATTRIBUTE_CLASS) == null)
					&& (configurationElement.getChildren(ELEMENT_CLASS).length == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Handlers need a class: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "', commandId=" //$NON-NLS-1$
						+ commandId + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				if (warningsToLog == null) {
					warningsToLog = new ArrayList();
				}
				warningsToLog.add(status);
				continue;
			}

			// Check to see if we have an activeWhen expression.
			final IConfigurationElement[] activeWhenElements = configurationElement
					.getChildren(ELEMENT_ACTIVE_WHEN);
			Expression activeWhenExpression = null;
			if (activeWhenElements.length > 0) {
				// Check if we have too many active when elements.
				if (activeWhenElements.length > 1) {
					// There should only be one activeWhen element
					final String message = "Handlers should only have one activeWhen element: plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId=" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					if (warningsToLog == null) {
						warningsToLog = new ArrayList();
					}
					warningsToLog.add(status);
					continue;
				}

				// Convert the activeWhen element into an expression.
				final ElementHandler elementHandler = ElementHandler
						.getDefault();
				final ExpressionConverter converter = ExpressionConverter
						.getDefault();
				try {
					activeWhenExpression = elementHandler.create(converter,
							activeWhenElements[0]);
				} catch (final CoreException e) {
					// There activeWhen expression could not be created.
					final String message = "Problem creating activeWhen element: plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId=" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					if (warningsToLog == null) {
						warningsToLog = new ArrayList();
					}
					warningsToLog.add(status);
				}
			}

			// Check to see if we have an enabledWhen expression.
			final IConfigurationElement[] enabledWhenElements = configurationElement
					.getChildren(ELEMENT_ENABLED_WHEN);
			Expression enabledWhenExpression = null;
			if (enabledWhenElements.length > 0) {
				// Check if we have too many enabled when elements.
				if (enabledWhenElements.length > 1) {
					// There should only be one enabledWhen element
					final String message = "Handlers should only have one enabledWhen element: plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId=" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					if (warningsToLog == null) {
						warningsToLog = new ArrayList();
					}
					warningsToLog.add(status);
					continue;
				}

				// Convert the enabledWhen element into an expression.
				final ElementHandler elementHandler = ElementHandler
						.getDefault();
				final ExpressionConverter converter = ExpressionConverter
						.getDefault();
				try {
					enabledWhenExpression = elementHandler.create(converter,
							enabledWhenElements[0]);
				} catch (final CoreException e) {
					// There enabledWhen expression could not be created.
					final String message = "Problem creating enabledWhen element: plug-in='" //$NON-NLS-1$
							+ configurationElement.getNamespace()
							+ "', commandId=" //$NON-NLS-1$
							+ commandId + "'."; //$NON-NLS-1$
					final IStatus status = new Status(IStatus.WARNING,
							WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
					if (warningsToLog == null) {
						warningsToLog = new ArrayList();
					}
					warningsToLog.add(status);
				}
			}

			if (activeWhenExpression != null) {
				handlerActivations.add(handlerService.activateHandler(
						commandId, new HandlerProxy(configurationElement,
								ATTRIBUTE_CLASS, enabledWhenExpression,
								handlerService), activeWhenExpression));
			} else {
				handlerActivations.add(handlerService.activateHandler(
						commandId, new HandlerProxy(configurationElement,
								ATTRIBUTE_CLASS, enabledWhenExpression,
								handlerService)));
			}
		}

		// If there were any warnings, then log them now.
		if (warningsToLog != null) {
			final String message = "Warnings while parsing the handlers from the 'org.eclipse.ui.handlers' extension point."; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
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
	private static final void readHandlerSubmissionsFromCommandsExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final IHandlerService handlerService) {
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = null;

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String commandId = configurationElement
					.getAttribute(ATTRIBUTE_COMMAND_ID);
			if ((commandId == null) || (commandId.length() == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Handler submissions need a command id: '" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				if (warningsToLog == null) {
					warningsToLog = new ArrayList();
				}
				warningsToLog.add(status);
				continue;
			}

			handlerActivations.add(handlerService.activateHandler(commandId,
					new LegacyHandlerWrapper(new LegacyHandlerProxy(
							configurationElement))));
		}

		// If there were any warnings, then log them now.
		if (warningsToLog != null) {
			final String message = "Warnings while parsing the handler submissions from the 'org.eclipse.ui.commands' extension point."; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}
}
