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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
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
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A static class for accessing the registry.
 * </p>
 * 
 * @since 3.1
 */
final class ContextPersistence {

	/**
	 * The name of the description attribute, which appears on a context
	 * definition.
	 */
	private static final String ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The name of the id attribute, which is used on context definitions.
	 */
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the name attribute, which appears on contexts definitions.
	 */
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent attribute, which appears on contexts
	 * definitions.
	 */
	private static final String ATTRIBUTE_PARENT = "parent"; //$NON-NLS-1$

	/**
	 * The name of the parent id attribute, which appears on contexts
	 * definitions.
	 */
	private static final String ATTRIBUTE_PARENT_ID = "parentId"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent scope attribute, which appears on
	 * contexts definitions.
	 */
	private static final String ATTRIBUTE_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$

	/**
	 * The name of the element storing a deprecated accelerator scope.
	 */
	private static final String ELEMENT_ACCELERATOR_SCOPE = "acceleratorScope"; //$NON-NLS-1$

	/**
	 * The name of the element storing a context.
	 */
	private static final String ELEMENT_CONTEXT = "context"; //$NON-NLS-1$

	/**
	 * The name of the element storing a deprecated scope.
	 */
	private static final String ELEMENT_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * The name of the accelerator scopes extension point.
	 */
	private static final String EXTENSION_ACCELERATOR_SCOPES = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchConstants.PL_ACCELERATOR_SCOPES;

	/**
	 * The name of the commands extension point.
	 */
	private static final String EXTENSION_COMMANDS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_COMMANDS;

	/**
	 * The name of the contexts extension point.
	 */
	private static final String EXTENSION_CONTEXTS = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchConstants.PL_CONTEXTS;

	/**
	 * The index of the context elements in the indexed array.
	 * 
	 * @see ContextPersistence#read(ContextManager)
	 */
	private static final int INDEX_CONTEXT_DEFINITIONS = 0;

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
	 * Reads all of the contexts from the registry,
	 * 
	 * @param contextManager
	 *            The context manager which should be populated with the values
	 *            from the registry; must not be <code>null</code>.
	 */
	static final void read(final ContextManager contextManager) {
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
			if (ELEMENT_ACCELERATOR_SCOPE.equals(name)) {
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
			if (ELEMENT_SCOPE.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CONTEXT_DEFINITIONS, contextDefinitionCount++);
			} else if (ELEMENT_CONTEXT.equals(name)) {
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
			if (ELEMENT_CONTEXT.equals(name)) {
				addElementToIndexedArray(configurationElement,
						indexedConfigurationElements,
						INDEX_CONTEXT_DEFINITIONS, contextDefinitionCount++);
			}
		}

		readContextsFromExtensionPoint(
				indexedConfigurationElements[INDEX_CONTEXT_DEFINITIONS],
				contextDefinitionCount, contextManager);
		
        /*
		 * Adds listener so that future registry changes trigger an update of
		 * the command manager automatically.
		 */
		if (!listenersAttached) {
			registry.addRegistryChangeListener(new IRegistryChangeListener() {
				public final void registryChanged(
						final IRegistryChangeEvent event) {
					final IExtensionDelta[] acceleratorScopeDeltas = event
							.getExtensionDeltas(
									PlatformUI.PLUGIN_ID,
									IWorkbenchConstants.PL_ACCELERATOR_SCOPES);
					if (acceleratorScopeDeltas.length == 0) {
						final IExtensionDelta[] contextDeltas = event
								.getExtensionDeltas(PlatformUI.PLUGIN_ID,
										IWorkbenchConstants.PL_CONTEXTS);
						if (contextDeltas.length == 0) {
							final IExtensionDelta[] commandDeltas = event
									.getExtensionDeltas(PlatformUI.PLUGIN_ID,
											IWorkbenchConstants.PL_COMMANDS);
							if (commandDeltas.length == 0) {
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
							read(contextManager);
						}
					});
				}
			}, PlatformUI.PLUGIN_ID);

			listenersAttached = true;
		}
	}

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
	private static final void readContextsFromExtensionPoint(
			final IConfigurationElement[] configurationElements,
			final int configurationElementCount,
			final ContextManager contextManager) {
		// Undefine all the previous handle objects.
		final Set contextIds = contextManager.getDefinedContextIds();
		if (contextIds != null) {
			final Iterator contextIdItr = contextIds.iterator();
			final Context[] contexts = new Context[contextIds.size()];
			int i = 0;
			
			while (contextIdItr.hasNext()) {
				contexts[i++] = contextManager.getContext((String) contextIdItr.next());
			}

			for (int j = 0; j < contexts.length; j++) {
				contexts[j].undefine();
			}
		}
		
		/*
		 * If necessary, this list of status items will be constructed. It will
		 * only contains instances of <code>IStatus</code>.
		 */
		List warningsToLog = new ArrayList(1);

		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];

			// Read out the command identifier.
			final String contextId = configurationElement
					.getAttribute(ATTRIBUTE_ID);
			if ((contextId == null) || (contextId.length() == 0)) {
				// The id should never be null. This is invalid.
				final String message = "Contexts need an id: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace() + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the name.
			final String name = configurationElement
					.getAttribute(ATTRIBUTE_NAME);
			if ((name == null) || (name.length() == 0)) {
				// The name should never be null. This is invalid.
				final String message = "Contexts need a name: plug-in='" //$NON-NLS-1$
						+ configurationElement.getNamespace()
						+ "', contextId='" //$NON-NLS-1$
						+ contextId + "'."; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.WARNING,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, null);
				warningsToLog.add(status);
				continue;
			}

			// Read out the description.
			String description = configurationElement
					.getAttribute(ATTRIBUTE_DESCRIPTION);
			if ((description != null) && (description.length() == 0)) {
				description = null;
			}

			// Read out the category id.
			String parentId = configurationElement
					.getAttribute(ATTRIBUTE_PARENT_ID);
			if ((parentId == null) || (parentId.length() == 0)) {
				parentId = configurationElement.getAttribute(ATTRIBUTE_PARENT);
				if ((parentId == null) || (parentId.length() == 0)) {
					parentId = configurationElement
							.getAttribute(ATTRIBUTE_PARENT_SCOPE);
				}
			}
			if ((parentId != null) && (parentId.length() == 0)) {
				parentId = null;
			}

			final Context context = contextManager.getContext(contextId);
			context.define(name, description, parentId);
		}

		// If there were any warnings, then log them now.
		if (!warningsToLog.isEmpty()) {
			final String message = "Warnings while parsing the contexts from the 'org.eclipse.ui.contexts', 'org.eclipse.ui.commands' and 'org.eclipse.ui.acceleratorScopes' extension points."; //$NON-NLS-1$
			final IStatus status = new MultiStatus(
					WorkbenchPlugin.PI_WORKBENCH, 0, (IStatus[]) warningsToLog
							.toArray(new IStatus[warningsToLog.size()]),
					message, null);
			WorkbenchPlugin.log(status);
		}
	}

	/**
	 * This class should not be constructed.
	 */
	private ContextPersistence() {
		// Should not be called.
	}

}
