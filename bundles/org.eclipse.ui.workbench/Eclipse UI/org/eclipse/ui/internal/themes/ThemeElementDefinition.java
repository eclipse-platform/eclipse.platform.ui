/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.themes;

import java.util.ResourceBundle;

/**
 * @since 3.5
 *
 */
public class ThemeElementDefinition {
	private String id;

	private String label;

	private String description;

	private String categoryId;

	private boolean overridden;

	private boolean addedByCss;

	private String overriddenLabel;

	public ThemeElementDefinition(String id, String label, String description, String categoryId) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.categoryId = categoryId;
	}

	/**
	 * @return the id of this definition. Should not be <code>null</code>.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the label.
	 * 
	 * @return String
	 */
	public String getName() {
		return label;
	}

	public void setName(String label) {
		this.label = label;
		setOverridden(true);
	}

	/**
	 * Returns the description.
	 * 
	 * @return String or
	 * 
	 *         <pre>
	 * null
	 * </pre>
	 * 
	 *         .
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		setOverridden(true);
	}

	/**
	 * Returns the categoryId.
	 * 
	 * @return String
	 */
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
		setOverridden(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.css.swt.definition.IDefinitionOverridable#isOverriden()
	 */
	public boolean isOverridden() {
		return overridden;
	}

	protected void setOverridden(boolean overridden) {
		this.overridden = overridden;
		if (isAddedByCss()) {
			return;
		}

		boolean hasOverriddenLabel = description.endsWith(getOverriddenLabel());
		if (overridden && !hasOverriddenLabel) {
			description += ' ' + getOverriddenLabel();
		} else if (!overridden && hasOverriddenLabel) {
			description = description.substring(0, description.length()
					- getOverriddenLabel().length() - 1);
		}
	}

	public boolean isAddedByCss() {
		return addedByCss;
	}

	public void setAddedByCss(boolean addedByCss) {
		this.addedByCss = addedByCss;
	}

	public String getOverriddenLabel() {
		if (overriddenLabel == null) {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(Theme.class.getName());
			overriddenLabel = resourceBundle.getString("Overridden.by.css.label"); //$NON-NLS-1$
		}
		return overriddenLabel;
	}

	public void resetToDefaultValue() {
		setOverridden(false);
		setAddedByCss(false);
	}
}
