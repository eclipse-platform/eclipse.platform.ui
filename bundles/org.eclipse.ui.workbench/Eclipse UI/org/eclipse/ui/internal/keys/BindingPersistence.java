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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;

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
	private static final String ATTRIBUTE_SCHEME_ID = "keyConfigurationId"; //$NON-NLS-1$

	/**
	 * Whether this class should print out debugging information when it reads
	 * in data, or writes to the preference store.
	 */
	private static final boolean DEBUG = Policy.DEBUG_KEY_BINDINGS;

	/**
	 * The name of the element storing the active scheme. This is called a
	 * 'keyConfiguration' for legacy reasons.
	 */
	private static final String ELEMENT_ACTIVE_SCHEME = "activeKeyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing the binding. This is called a
	 * 'keyBinding' for legacy reasons.
	 */
	private static final String ELEMENT_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The preference key for the workbench preference store. This keys is
	 * called 'commands' for legacy reasons.
	 */
	private static final String WORKBENCH_PREFERENCE_KEY = "org.eclipse.ui.commands"; //$NON-NLS-1$

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
			final Set bindings) throws IOException {
		// Print out debugging information, if requested.
		if (DEBUG) {
			System.out.println("BINDINGS >> Persisting active scheme '" //$NON-NLS-1$
					+ activeScheme.getId() + "'"); //$NON-NLS-1$
			System.out.println("BINDINGS >> Persisting bindings"); //$NON-NLS-1$
		}

		// Write the simple preference key to the UI preference store.
		writeActiveScheme(activeScheme);

		// Build the XML block for writing the bindings and active scheme.
		final XMLMemento xmlMemento = XMLMemento
				.createWriteRoot(WORKBENCH_PREFERENCE_KEY);
		if (activeScheme != null) {
			writeActiveScheme(xmlMemento, activeScheme);
		}
		if (bindings != null) {
			final Iterator bindingItr = bindings.iterator();
			while (bindingItr.hasNext()) {
				final Binding binding = (Binding) bindingItr.next();
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
			preferenceStore.setValue(WORKBENCH_PREFERENCE_KEY, writer
					.toString());
		} finally {
			writer.close();
		}
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
