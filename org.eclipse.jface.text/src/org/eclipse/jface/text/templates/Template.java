/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

/**
 * A template consisting of a name and a pattern.
 * 
 * @since 3.0
 */
public class Template {

	/** The name of this template */
	private String fName;
	/** A description of this template */
	private String fDescription;
	/** The name of the context type of this template */
	private String fContextTypeName;
	/** The template pattern. */
	private String fPattern;
	/** A flag indicating if the template is active or not. */
	private boolean fEnabled= true;

	/**
	 * Creates an empty template.
	 */
	public Template() {
		this("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	/**
	 * Creates a copy of a template.
	 * 
	 * @param template the template to copy
	 */
	public Template(Template template) {
		this(template.getName(), template.getDescription(), template.getContextTypeName(), template.getPattern());	
	}

	/**
	 * Creates a template.
	 * 
	 * @param name the name of the template.
	 * @param description the description of the template.
	 * @param contextTypeName the name of the context type in which the template can be applied.
	 * @param pattern the template pattern.
	 */		
	public Template(String name, String description, String contextTypeName, String pattern) {
		fName= name;
		fDescription= description;
		fContextTypeName= contextTypeName;
		fPattern= pattern;
	}
	
	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Template))
			return false;
			
		Template template= (Template) object;

		if (template == this)
			return true;		

		return
			template.fName.equals(fName) &&
			template.fPattern.equals(fPattern) &&
			template.fContextTypeName.equals(fContextTypeName);
	}
	
	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fName.hashCode() ^ fPattern.hashCode() ^ fContextTypeName.hashCode();
	}

	/**
	 * Sets the description of the template.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
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
	 * @param contextTypeName the new context type name
	 */
	public void setContext(String contextTypeName) {
		fContextTypeName= contextTypeName;
	}
	
	/**
	 * Returns the name of the context type in which the template can be applied.
	 * 
	 * @return the name of the context type in which the template can be applied
	 */
	public String getContextTypeName() {
		return fContextTypeName;
	}

	/**
	 * Sets the name of the template.
	 * 
	 * @param name the name of the template
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
	 * Sets the enable state of the template.
	 * 
	 * @param enable the new enable state of the template
	 */
	public void setEnabled(boolean enable) {
		fEnabled= enable;	
	}
	
	/**
	 * Returns <code>true</code> if template is enabled, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if template is enabled, <code>false</code> otherwise
	 */
	public boolean isEnabled() {
		return fEnabled;	
	}
	
	/**
	 * Returns <code>true</code> if template is enabled and matches the context,
	 * <code>false</code> otherwise.
	 * 
	 * @param prefix the prefix (e.g. inside a document) to match
	 * @param contextTypeName the context type name to match
	 * @return <code>true</code> if template is enabled and matches the context,
	 * <code>false</code> otherwise
	 */
	public boolean matches(String prefix, String contextTypeName) {
		return fEnabled && fContextTypeName.equals(contextTypeName);
	}

}
