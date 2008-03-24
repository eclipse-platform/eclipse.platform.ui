/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys.model;

/**
 * @since 3.4
 *
 */
public class ModelElement {

	public static final String PROP_PARENT = "parent"; //$NON-NLS-1$
	public static final String PROP_ID = "id"; //$NON-NLS-1$
	public static final String PROP_NAME = "name"; //$NON-NLS-1$
	public static final String PROP_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String PROP_MODEL_OBJECT = "modelObject"; //$NON-NLS-1$
	protected KeyController controller;
	private ModelElement parent;
	private String id;
	private String name;
	private String description;
	private Object modelObject;

	public ModelElement(KeyController kc) {
		controller = kc;
	}

	/**
	 * @return Returns the parent.
	 */
	public ModelElement getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(ModelElement parent) {
		ModelElement old = this.parent;
		this.parent = parent;
		controller.firePropertyChange(this, PROP_PARENT, old, parent);
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		String old = this.id;
		this.id = id;
		controller.firePropertyChange(this, PROP_ID, old, id);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		String old = this.name;
		this.name = name;
		controller.firePropertyChange(this, PROP_NAME, old, name);
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
		controller.firePropertyChange(this, PROP_DESCRIPTION, old, description);
	}

	/**
	 * @return Returns the context.
	 */
	public Object getModelObject() {
		return modelObject;
	}

	/**
	 * @param context The context to set.
	 */
	public void setModelObject(Object o) {
		Object old = this.modelObject;
		modelObject = o;
		controller.firePropertyChange(this, PROP_MODEL_OBJECT, old, o);
	}
}
