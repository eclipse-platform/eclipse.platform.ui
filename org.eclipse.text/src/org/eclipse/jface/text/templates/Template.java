/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.core.runtime.Assert;


/**
 * A template consisting of a name and a pattern.
 * <p>
 * Clients may instantiate this class. May become final in the future.
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Template {

	/** The name of this template */
	private /*final*/ String fName;
	/** A description of this template */
	private /*final*/ String fDescription;
	/** The name of the context type of this template */
	private /*final*/ String fContextTypeId;
	/** The template pattern. */
	private /*final*/ String fPattern;
	/**
	 * The auto insertable property.
	 * @since 3.1
	 */
	private final boolean fIsAutoInsertable;

	/**
	 * Creates an empty template.
	 */
	public Template() {
		this("", "", "", "", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * Creates a copy of a template.
	 *
	 * @param template the template to copy
	 */
	public Template(Template template) {
		this(template.getName(), template.getDescription(), template.getContextTypeId(), template.getPattern(), template.isAutoInsertable());
	}

	/**
	 * Creates a template.
	 *
	 * @param name the name of the template
	 * @param description the description of the template
	 * @param contextTypeId the id of the context type in which the template can be applied
	 * @param pattern the template pattern
	 * @deprecated as of 3.1 replaced by {@link #Template(String, String, String, String, boolean)}
	 */
	public Template(String name, String description, String contextTypeId, String pattern) {
		this(name, description, contextTypeId, pattern, true); // templates are auto insertable per default
	}

	/**
	 * Creates a template.
	 *
	 * @param name the name of the template
	 * @param description the description of the template
	 * @param contextTypeId the id of the context type in which the template can be applied
	 * @param pattern the template pattern
	 * @param isAutoInsertable the auto insertable property of the template
	 * @since 3.1
	 */
	public Template(String name, String description, String contextTypeId, String pattern, boolean isAutoInsertable) {
		Assert.isNotNull(description);
		fDescription= description;
		fName= name;
		Assert.isNotNull(contextTypeId);
		fContextTypeId= contextTypeId;
		fPattern= pattern;
		fIsAutoInsertable= isAutoInsertable;
	}

	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fName.hashCode() ^ fPattern.hashCode() ^ fContextTypeId.hashCode();
	}

	/**
	 * Sets the description of the template.
	 *
	 * @param description the new description
	 * @deprecated Templates should never be modified
	 */
	public void setDescription(String description) {
		Assert.isNotNull(description);
		fDescription= description;
	}

	/**
	 * Returns the description of the template.
	 *
	 * @return the description of the template
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Sets the name of the context type in which the template can be applied.
	 *
	 * @param contextTypeId the new context type name
	 * @deprecated Templates should never be modified
	 */
	public void setContextTypeId(String contextTypeId) {
		Assert.isNotNull(contextTypeId);
		fContextTypeId= contextTypeId;
	}

	/**
	 * Returns the id of the context type in which the template can be applied.
	 *
	 * @return the id of the context type in which the template can be applied
	 */
	public String getContextTypeId() {
		return fContextTypeId;
	}

	/**
	 * Sets the name of the template.
	 *
	 * @param name the name of the template
	 * @deprecated Templates should never be modified
	 */
	public void setName(String name) {
		fName= name;
	}

	/**
	 * Returns the name of the template.
	 *
	 * @return the name of the template
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Sets the pattern of the template.
	 *
	 * @param pattern the new pattern of the template
	 * @deprecated Templates should never be modified
	 */
	public void setPattern(String pattern) {
		fPattern= pattern;
	}

	/**
	 * Returns the template pattern.
	 *
	 * @return the template pattern
	 */
	public String getPattern() {
		return fPattern;
	}

	/**
	 * Returns <code>true</code> if template is enabled and matches the context,
	 * <code>false</code> otherwise.
	 *
	 * @param prefix the prefix (e.g. inside a document) to match
	 * @param contextTypeId the context type id to match
	 * @return <code>true</code> if template is enabled and matches the context,
	 * <code>false</code> otherwise
	 */
	public boolean matches(String prefix, String contextTypeId) {
		return fContextTypeId.equals(contextTypeId);
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Template))
			return false;

		Template t= (Template) o;
		if (t == this)
			return true;

		return t.fName.equals(fName)
				&& t.fPattern.equals(fPattern)
				&& t.fContextTypeId.equals(fContextTypeId)
				&& t.fDescription.equals(fDescription)
				&& t.fIsAutoInsertable == fIsAutoInsertable;
	}

	/**
	 * Returns the auto insertable property of the template.
	 *
	 * @return the auto insertable property of the template
	 * @since 3.1
	 */
	public boolean isAutoInsertable() {
		return fIsAutoInsertable;
	}
}
