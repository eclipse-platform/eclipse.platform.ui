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
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.ws.HandlerProxy;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
final class HandlerPersistence {

	/**
	 * The name of the command identifier attribute, which appears on a handler
	 * submission.
	 */
	private static final String ATTRIBUTE_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler submission.
	 */
	private static final String ELEMENT_HANDLER_SUBMISSION = "handlerSubmission"; //$NON-NLS-1$

	/**
	 * The name of the commands extension point.
	 */
	private static final String EXTENSION_COMMANDS = "org.eclipse.ui.commands"; //$NON-NLS-1$

	/**
	 * The index of the handler submissions in the indexed array.
	 * 
	 * @see HandlerPersistence#read(IHandlerService)
	 */
	private static final int INDEX_HANDLER_SUBMISSIONS = 0;

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
	 * Reads all of the handlers from the registry
	 * 
	 * @param handlerService
	 *            The handler service which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	static final void read(final IHandlerService handlerService) {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int handlerSubmissionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[1][];

		// Sort the commands extension point based on element name.
		final IConfigurationElement[] commandsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (int i = 0; i < commandsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = commandsExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (ELEMENT_HANDLER_SUBMISSION.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_HANDLER_SUBMISSIONS, handlerSubmissionCount++);
			}
		}

		readHandlersFromCommandsExtensionPoint(
				indexedConfigurationElements[INDEX_HANDLER_SUBMISSIONS],
				handlerSubmissionCount, handlerService);
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
	private static final void readHandlersFromCommandsExtensionPoint(
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

			handlerService.activateHandler(commandId, new LegacyHandlerWrapper(
					new HandlerProxy(configurationElement)));
		}

		// If there were any warnings, then log them now.
		if (warningsToLog != null) {
			final String message = "Warnings while parsing the handler submissions from the 'org.eclipse.ui.commands' extension point."; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(message, status);
		}
	}
}
