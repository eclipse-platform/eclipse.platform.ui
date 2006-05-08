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

package org.eclipse.ui.handlers;

import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.commands.ToggleState;

/**
 * <p>
 * A toggle state that can be read from the registry. This stores a piece of
 * boolean state information.
 * </p>
 * <p>
 * When parsing from the registry, this state understands two parameters:
 * <code>default</code>, which is the default value for this item; and
 * <code>persisted</code>, which is whether the state should be persisted
 * between sessions. The <code>default</code> parameter defaults to
 * <code>false</code>, and the <code>persisted</code> parameter defaults to
 * <code>true</code>. If only one parameter is passed (i.e., using the class
 * name followed by a colon), then it is assumed to be the <code>default</code>
 * parameter.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
 * </p>
 * 
 * @since 3.2
 */
public final class RegistryToggleState extends ToggleState implements
		IExecutableExtension {

	/**
	 * Reads the <code>default</code> parameter from the given string. This
	 * converts the string to a boolean, using <code>true</code> as the
	 * default.
	 * 
	 * @param defaultString
	 *            The string to parse; may be <code>null</code>.
	 */
	private final void readDefault(final String defaultString) {
		if ("true".equalsIgnoreCase(defaultString)) { //$NON-NLS-1$
			setValue(Boolean.TRUE);
		}
	}

	/**
	 * Reads the <code>persisted</code> parameter from the given string. This
	 * converts the string to a boolean, using <code>true</code> as the
	 * default.
	 * 
	 * @param persistedString
	 *            The string to parse; may be <code>null</code>.
	 */
	private final void readPersisted(final String persistedString) {
		if ("false".equalsIgnoreCase(persistedString)) { //$NON-NLS-1$
			setShouldPersist(false);
		}

		setShouldPersist(true);
	}

	public final void setInitializationData(
			final IConfigurationElement configurationElement,
			final String propertyName, final Object data) {
		if (data instanceof String) {
			// This is the default value.
			readDefault((String) data);
			setShouldPersist(true);

		} else if (data instanceof Hashtable) {
			final Hashtable parameters = (Hashtable) data;
			final Object defaultObject = parameters.get("default"); //$NON-NLS-1$
			if (defaultObject instanceof String) {
				readDefault((String) defaultObject);
			}

			final Object persistedObject = parameters.get("persisted"); //$NON-NLS-1$
			if (persistedObject instanceof String) {
				readPersisted((String) persistedObject);
			}

		} else {
			setShouldPersist(true);
			
		}
	}
}
