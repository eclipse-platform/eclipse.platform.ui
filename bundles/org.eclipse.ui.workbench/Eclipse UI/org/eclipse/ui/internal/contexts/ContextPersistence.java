/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.common.HandleObject;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.RegistryPersistence;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.1
 */
final class ContextPersistence extends RegistryPersistence {

	/**
	 * The index of the context elements in the indexed array.
	 * 
	 * @see ContextPersistence#read()
	 */
	private static final int INDEX_CONTEXT_DEFINITIONS = 0;

	/**
	 * Reads all of the command definitions from the commands extension point.
	 * 
	 * @param configurationElements
	 *            The configuration elements in the commands extension point;
	 *            must not be <code>null</code>, but may be empty.
	 * @param configurationElementCount
	 *            The number of configuration elements that are really in the
	 *            array.
	 * @param contextManager
	 *            The context manager to which the commands should be added;
	 *            must not be <code>null</code>.
	 */
	private static final void readContextsFromRegistry(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final ContextManager contextManager) {
		// Undefine all the previous handle objects.
		final HandleObject[] handleObjects = contextManager
				.getDefinedContexts();
		if (handleObjects != null) {
			for (int i = 0; i < handleObjects.length; i++) {
				handleObjects[i].undefine();
			}
		}

		final List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String contextId = readRequired(configurationElement, ATT_ID,
					warningsToLog, "Contexts need an id"); //$NON-NLS-1$
			if (contextId == null) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(configurationElement, ATT_NAME,
					warningsToLog, "Contexts need a name", //$NON-NLS-1$
					contextId);
			if (name == null) {
				continue;
			}

			// Read out the description.
			final String description = readOptional(configurationElement,
					ATT_DESCRIPTION);

			// Read out the parent id.
			String parentId = configurationElement.getAttribute(ATT_PARENT_ID);
			if ((parentId == null) || (parentId.length() == 0)) {
				parentId = configurationElement.getAttribute(ATT_PARENT);
				if ((parentId == null) || (parentId.length() == 0)) {
					parentId = configurationElement
							.getAttribute(ATT_PARENT_SCOPE);
				}
			}
			if ((parentId != null) && (parentId.length() == 0)) {
				parentId = null;
			}

			final Context context = contextManager.getContext(contextId);
			context.define(name, description, parentId);
		}

		logWarnings(
				warningsToLog,
				"Warnings while parsing the contexts from the 'org.eclipse.ui.contexts', 'org.eclipse.ui.commands' and 'org.eclipse.ui.acceleratorScopes' extension points."); //$NON-NLS-1$
	}

	/**
	 * The context manager for this instance; must not be <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * Constructs a new instance of <code>ContextPersistence</code>.
	 */
	ContextPersistence(final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"The context manager must not be null"); //$NON-NLS-1$
		}
		this.contextManager = contextManager;
	}

	protected final boolean isChangeImportant(final IRegistryChangeEvent event) {
		final IExtensionDelta[] acceleratorScopeDeltas = event
				.getExtensionDeltas(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_ACCELERATOR_SCOPES);
		if (acceleratorScopeDeltas.length == 0) {
			final IExtensionDelta[] contextDeltas = event.getExtensionDeltas(
					PlatformUI.PLUGIN_ID,
					IWorkbenchRegistryConstants.PL_CONTEXTS);
			if (contextDeltas.length == 0) {
				final IExtensionDelta[] commandDeltas = event
						.getExtensionDeltas(PlatformUI.PLUGIN_ID,
								IWorkbenchRegistryConstants.PL_COMMANDS);
				if (commandDeltas.length == 0) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Reads all of the contexts from the registry,
	 * 
	 * @param contextManager
	 *            The context manager which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	protected final void read() {
		super.read();

		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int contextDefinitionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[1][];

		/*
		 * Retrieve the accelerator scopes from the accelerator scopes extension
		 * point.
		 */
		final IConfigurationElement[] acceleratorScopesExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACCELERATOR_SCOPES);
		for (int i = 0; i < acceleratorScopesExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = acceleratorScopesExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_ACCELERATOR_SCOPE.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CONTEXT_DEFINITIONS, contextDefinitionCount++);
			}
		}

		/*
		 * Retrieve the deprecated scopes and contexts from the commands
		 * extension point.
		 */
		final IConfigurationElement[] commandsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (int i = 0; i < commandsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = commandsExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_SCOPE.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CONTEXT_DEFINITIONS, contextDefinitionCount++);
			} else if (TAG_CONTEXT.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CONTEXT_DEFINITIONS, contextDefinitionCount++);

			}
		}

		/*
		 * Retrieve the contexts from the contexts extension point.
		 */
		final IConfigurationElement[] contextsExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_CONTEXTS);
		for (int i = 0; i < contextsExtensionPoint.length; i++) {
			final IConfigurationElement configurationElement = contextsExtensionPoint[i];
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_CONTEXT.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CONTEXT_DEFINITIONS, contextDefinitionCount++);
			}
		}

		readContextsFromRegistry(
				indexedConfigurationElements[INDEX_CONTEXT_DEFINITIONS],
				contextDefinitionCount, contextManager);
	}

}
