/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.keys;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.keys.IBindingService;

/**
 * <p>
 * A static class for accessing the registry and the preference store.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * <p>
 * TODO Add methods for reading the extension registry and the preference store.
 * </p>
 * 
 * @since 3.1
 */
public final class BindingPersistence {

	/**
	 * The name of the attribute storing the command id for a binding.
	 */
	private static final String ATTRIBUTE_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the context id for a binding.
	 */
	private static final String ATTRIBUTE_CONTEXT_ID = "contextId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active key
	 * configuration identifier. This provides legacy support for the
	 * <code>activeKeyConfiguration</code> element in the commands extension
	 * point.
	 */
	private static final String ATTRIBUTE_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the trigger sequence for a binding.
	 * This is called a 'keySequence' for legacy reasons.
	 */
	private static final String ATTRIBUTE_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the locale for a binding.
	 */
	private static final String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the platform for a binding.
	 */
	private static final String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active scheme.
	 * This is called a 'keyConfigurationId' for legacy reasons.
	 */
	private static final String ATTRIBUTE_SCHEME_ID = ATTRIBUTE_KEY_CONFIGURATION_ID; //$NON-NLS-1$

	/**
	 * The name of the deprecated attribute of the deprecated
	 * <code>activeKeyConfiguration</code> element in the commands extension
	 * point.
	 */
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Whether this class should print out debugging information when it reads
	 * in data, or writes to the preference store.
	 */
	private static final boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	/**
	 * The name of the element storing the active key configuration from the
	 * commands extension point.
	 */
	private static final String ELEMENT_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing the active scheme. This is called a
	 * 'keyConfiguration' for legacy reasons.
	 */
	private static final String ELEMENT_ACTIVE_SCHEME = ELEMENT_ACTIVE_KEY_CONFIGURATION; //$NON-NLS-1$

	/**
	 * The name of the element storing the binding. This is called a
	 * 'keyBinding' for legacy reasons.
	 */
	private static final String ELEMENT_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The preference key for the workbench preference store. This keys is
	 * called 'commands' for legacy reasons.
	 */
	private static final String KEY = "org.eclipse.ui.commands"; //$NON-NLS-1$

	/**
	 * Returns the default scheme identifier for the currently running
	 * application.
	 * 
	 * @return The default scheme identifier (<code>String</code>); never
	 *         <code>null</code>, but may be empty or point to an undefined
	 *         scheme.
	 */
	public static final String getDefaultSchemeId() {
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		return store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
	}

	/**
	 * Writes the given active scheme and bindings to the preference store. Only
	 * bindings that are of the <code>Binding.USER</code> type will be
	 * written; the others will be ignored.
	 * 
	 * @param activeScheme
	 *            The scheme which should be persisted; may be <code>null</code>.
	 * @param bindings
	 *            The bindings which should be persisted; may be
	 *            <code>null</code>
	 * @throws IOException
	 *             If something happens while trying to write to the workbench
	 *             preference store.
	 */
	public static final void persist(final Scheme activeScheme,
			final Binding[] bindings) throws IOException {
		// Print out debugging information, if requested.
		if (DEBUG) {
			System.out.println("BINDINGS >> Persisting active scheme '" //$NON-NLS-1$
					+ activeScheme.getId() + "'"); //$NON-NLS-1$
			System.out.println("BINDINGS >> Persisting bindings"); //$NON-NLS-1$
		}

		// Write the simple preference key to the UI preference store.
		writeActiveScheme(activeScheme);

		// Build the XML block for writing the bindings and active scheme.
		final XMLMemento xmlMemento = XMLMemento.createWriteRoot(KEY);
		if (activeScheme != null) {
			writeActiveScheme(xmlMemento, activeScheme);
		}
		if (bindings != null) {
			final int bindingsLength = bindings.length;
			for (int i = 0; i < bindingsLength; i++) {
				final Binding binding = bindings[i];
				if (binding.getType() == Binding.USER) {
					writeBinding(xmlMemento, binding);
				}
			}
		}

		// Write the XML block to the workbench preference store.
		final IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		final Writer writer = new StringWriter();
		try {
			xmlMemento.save(writer);
			preferenceStore.setValue(KEY, writer.toString());
		} finally {
			writer.close();
		}
	}

	/**
	 * Reads all of the binding information from the registry and from the
	 * preference store.
	 * 
	 * @return The active scheme identifier; this value is never
	 *         <code>null</code>, but it may point to undefined scheme.
	 */
	public static final String read() {
		// Create the extension registry mementos.
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] configurationElements = registry
				.getConfigurationElementsFor(KEY);

		// Create the preference memento.
		final IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		final String preferenceString = store.getString(KEY);
		IMemento preferenceMemento = null;
		if ((preferenceString != null) && (preferenceString.length() > 0)) {
			final Reader reader = new StringReader(preferenceString);
			try {
				preferenceMemento = XMLMemento.createReadRoot(reader);
			} catch (final WorkbenchException e) {
				// Could not initialize the preference memento.
			}
		}

		return readActiveScheme(configurationElements, preferenceMemento);
	}

	/**
	 * <p>
	 * Reads the registry and the preference store, and determines the
	 * identifier for the scheme that should be active. There is a complicated
	 * order of priorities for this. The registry will only be read if there is
	 * no user preference, and the default active scheme id is different than
	 * the default default active scheme id.
	 * </p>
	 * <ol>
	 * <li>A non-default preference.</li>
	 * <li>The legacy preference XML memento.</li>
	 * <li>A default preference value that is different than the default
	 * default active scheme id.</li>
	 * <li>The registry.</li>
	 * <li>The default default active scheme id.</li>
	 * </ol>
	 * 
	 * @param configurationElements
	 *            The configuration elements from the commands extension point;
	 *            must not be <code>null</code>.
	 * @param preferences
	 *            The memento wrapping the commands preference key; may be
	 *            <code>null</code>.
	 * @return The active scheme identifier; this value is never
	 *         <code>null</code>.
	 */
	private static final String readActiveScheme(
			final IConfigurationElement[] configurationElements,
			final IMemento preferences) {
		// A non-default preference.
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String defaultActiveSchemeId = store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		final String preferenceActiveSchemeId = store
				.getString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((preferenceActiveSchemeId != null)
				&& (!preferenceActiveSchemeId.equals(defaultActiveSchemeId))) {
			return preferenceActiveSchemeId;
		}

		// A legacy preference XML memento.
		if (preferences != null) {
			final IMemento[] preferenceMementos = preferences
					.getChildren(ELEMENT_ACTIVE_KEY_CONFIGURATION);
			int preferenceMementoCount = preferenceMementos.length;
			for (int i = preferenceMementoCount - 1; i >= 0; i--) {
				final IMemento memento = preferenceMementos[i];
				String id = memento.getString(ATTRIBUTE_KEY_CONFIGURATION_ID);
				if (id != null) {
					return id;
				}
			}
		}

		// A default preference value that is different than the default.
		if ((defaultActiveSchemeId != null)
				&& (!defaultActiveSchemeId
						.equals(IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID))) {
			return defaultActiveSchemeId;
		}

		// The registry.
		final int configurationElementCount = configurationElements.length;
		for (int i = 0; i < configurationElementCount; i++) {
			final IConfigurationElement configurationElement = configurationElements[i];
			if (!ELEMENT_ACTIVE_KEY_CONFIGURATION.equals(configurationElement
					.getName())) {
				continue;
			}

			String id = configurationElement
					.getAttribute(ATTRIBUTE_KEY_CONFIGURATION_ID);
			if (id != null) {
				return id;
			}

			id = configurationElement.getAttribute(ATTRIBUTE_VALUE);
			if (id != null) {
				return id;
			}
		}

		// The default default active scheme id.
		return IBindingService.DEFAULT_DEFAULT_ACTIVE_SCHEME_ID;
	}

	/**
	 * Writes the active scheme to the memento. If the scheme is
	 * <code>null</code>, then all schemes in the memento are removed.
	 * 
	 * @param memento
	 *            The memento to which the scheme should be written; must not be
	 *            <code>null</code>.
	 * @param scheme
	 *            The scheme that should be written; must not be
	 *            <code>null</code>.
	 */
	private static final void writeActiveScheme(final IMemento memento,
			final Scheme scheme) {
		// Add this active scheme, if it is not the default.
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String schemeId = scheme.getId();
		final String defaultSchemeId = store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((defaultSchemeId == null) ? (schemeId != null) : (!defaultSchemeId
				.equals(schemeId))) {
			final IMemento child = memento.createChild(ELEMENT_ACTIVE_SCHEME);
			child.putString(ATTRIBUTE_SCHEME_ID, schemeId);
		}
	}

	/**
	 * Writes the active scheme to its own preference key. This key is used by
	 * RCP applications as part of their plug-in customization.
	 * 
	 * @param scheme
	 *            The scheme to write to the preference store. If the scheme is
	 *            <code>null</code>, then it is removed.
	 */
	private static final void writeActiveScheme(final Scheme scheme) {
		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final String schemeId = (scheme == null) ? null : scheme.getId();
		final String defaultSchemeId = store
				.getDefaultString(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		if ((defaultSchemeId == null) ? (scheme != null) : (!defaultSchemeId
				.equals(schemeId))) {
			store.setValue(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID,
					scheme.getId());
		} else {
			store
					.setToDefault(IWorkbenchPreferenceConstants.KEY_CONFIGURATION_ID);
		}
	}

	/**
	 * Writes the binding to the memento. This creates a new child element on
	 * the memento, and places the properties of the binding as its attributes.
	 * 
	 * @param parent
	 *            The parent memento for the binding element; must not be
	 *            <code>null</code>.
	 * @param binding
	 *            The binding to write; must not be <code>null</code>.
	 */
	private static final void writeBinding(final IMemento parent,
			final Binding binding) {
		final IMemento element = parent.createChild(ELEMENT_BINDING);
		element.putString(ATTRIBUTE_CONTEXT_ID, binding.getContextId());
		element.putString(ATTRIBUTE_COMMAND_ID, binding.getCommandId());
		element.putString(ATTRIBUTE_SCHEME_ID, binding.getSchemeId());
		element.putString(ATTRIBUTE_KEY_SEQUENCE, binding.getTriggerSequence()
				.toString());
		element.putString(ATTRIBUTE_LOCALE, binding.getLocale());
		element.putString(ATTRIBUTE_PLATFORM, binding.getPlatform());
	}

	/**
	 * This class should not be constructed.
	 */
	private BindingPersistence() {
		// Should not be called.
	}
}
