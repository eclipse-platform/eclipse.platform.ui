/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IStatus;
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
public final class ContextPersistence extends RegistryPersistence {

	/**
	 * The index of the context elements in the indexed array.
	 *
	 * @see ContextPersistence#read()
	 */
	private static final int INDEX_CONTEXT_DEFINITIONS = 0;

	/**
	 * Reads all of the command definitions from the commands extension point.
	 *
	 * @param configurationElements     The configuration elements in the commands
	 *                                  extension point; must not be
	 *                                  <code>null</code>, but may be empty.
	 * @param configurationElementCount The number of configuration elements that
	 *                                  are really in the array.
	 * @param contextManager            The context manager to which the commands
	 *                                  should be added; must not be
	 *                                  <code>null</code>.
	 */
	private static void readContextsFromRegistry(final IConfigurationElement[] configurationElements,
			final int configurationElementCount, final ContextManager contextManager) {
		final List<IStatus> warningsToLog = new ArrayList<>(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String contextId = readRequired(configurationElement, ATT_ID, warningsToLog, "Contexts need an id"); //$NON-NLS-1$
			if (contextId == null) {
				continue;
			}

			// Read out the name.
			final String name = readRequired(configurationElement, ATT_NAME, warningsToLog, "Contexts need a name", //$NON-NLS-1$
					contextId);
			if (name == null) {
				continue;
			}

			// Read out the description.
			final String description = readOptional(configurationElement, ATT_DESCRIPTION);

			// Read out the parent id.
			String parentId = configurationElement.getAttribute(ATT_PARENT_ID);
			if ((parentId == null) || (parentId.isEmpty())) {
				parentId = configurationElement.getAttribute(ATT_PARENT);
				if ((parentId == null) || (parentId.isEmpty())) {
					parentId = configurationElement.getAttribute(ATT_PARENT_SCOPE);
				}
			}
			if ((parentId != null) && (parentId.isEmpty())) {
				parentId = null;
			}

			final Context context = contextManager.getContext(contextId);
			if (!context.isDefined()) {
				context.define(name, description, parentId);
			}
		}

		logWarnings(warningsToLog,
				"Warnings while parsing the contexts from the 'org.eclipse.ui.contexts', 'org.eclipse.ui.commands' and 'org.eclipse.ui.acceleratorScopes' extension points."); //$NON-NLS-1$
	}

	/**
	 * The context manager for this instance; must not be <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * Constructs a new instance of <code>ContextPersistence</code>.
	 */
	public ContextPersistence(final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException("The context manager must not be null"); //$NON-NLS-1$
		}
		this.contextManager = contextManager;
	}

	@Override
	protected boolean isChangeImportant(final IRegistryChangeEvent event) {
		final IExtensionDelta[] acceleratorScopeDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_ACCELERATOR_SCOPES);
		if (acceleratorScopeDeltas.length == 0) {
			final IExtensionDelta[] contextDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
					IWorkbenchRegistryConstants.PL_CONTEXTS);
			if (contextDeltas.length == 0) {
				final IExtensionDelta[] commandDeltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
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
	 */
	@Override
	protected void read() {
		super.read();
		reRead();
	}

	public void reRead() {

		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		int contextDefinitionCount = 0;
		final IConfigurationElement[][] indexedConfigurationElements = new IConfigurationElement[1][];

		/*
		 * Retrieve the accelerator scopes from the accelerator scopes extension point.
		 */
		final IConfigurationElement[] acceleratorScopesExtensionPoint = registry
				.getConfigurationElementsFor(EXTENSION_ACCELERATOR_SCOPES);
		for (final IConfigurationElement configurationElement : acceleratorScopesExtensionPoint) {
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_ACCELERATOR_SCOPE.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_CONTEXT_DEFINITIONS,
						contextDefinitionCount++);
			}
		}

		/*
		 * Retrieve the deprecated scopes and contexts from the commands extension
		 * point.
		 */
		final IConfigurationElement[] commandsExtensionPoint = registry.getConfigurationElementsFor(EXTENSION_COMMANDS);
		for (final IConfigurationElement configurationElement : commandsExtensionPoint) {
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_SCOPE.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_CONTEXT_DEFINITIONS,
						contextDefinitionCount++);
			} else if (TAG_CONTEXT.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_CONTEXT_DEFINITIONS,
						contextDefinitionCount++);

			}
		}

		/*
		 * Retrieve the contexts from the contexts extension point.
		 */
		final IConfigurationElement[] contextsExtensionPoint = registry.getConfigurationElementsFor(EXTENSION_CONTEXTS);
		for (final IConfigurationElement configurationElement : contextsExtensionPoint) {
			final String name = configurationElement.getName();

			// Check if it is a binding definition.
			if (TAG_CONTEXT.equals(name)) {
				addElementToIndexedArray(configurationElement, indexedConfigurationElements, INDEX_CONTEXT_DEFINITIONS,
						contextDefinitionCount++);
			}
		}

		readContextsFromRegistry(indexedConfigurationElements[INDEX_CONTEXT_DEFINITIONS], contextDefinitionCount,
				contextManager);
	}

}
