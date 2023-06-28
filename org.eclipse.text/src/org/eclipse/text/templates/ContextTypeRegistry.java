/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.text.templates;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * A registry for context types. Editor implementors will usually instantiate a
 * registry and configure the context types available in their editor.
 * <p>
 * In order to pick up templates contributed using the <code>org.eclipse.ui.editors.templates</code>
 * extension point, use a <code>ContributionContextTypeRegistry</code>.
 * </p>
 *
 * @since 3.7
 */
public class ContextTypeRegistry {

	/** all known context types */
	private final Map<String, TemplateContextType> fContextTypes= new LinkedHashMap<>();

	/**
	 * Adds a context type to the registry. If there already is a context type
	 * with the same ID registered, it is replaced.
	 *
	 * @param contextType the context type to add
	 */
	public void addContextType(TemplateContextType contextType) {
		fContextTypes.put(contextType.getId(), contextType);
	}

	/**
	 * Returns the context type if the id is valid, <code>null</code> otherwise.
	 *
	 * @param id the id of the context type to retrieve
	 * @return the context type if <code>name</code> is valid, <code>null</code> otherwise
	 */
	public TemplateContextType getContextType(String id) {
		return fContextTypes.get(id);
	}

	/**
	 * Returns an iterator over all registered context types.
	 *
	 * @return an iterator over all registered context types
	 */
	public Iterator<TemplateContextType> contextTypes() {
		return fContextTypes.values().iterator();
	}
}
