package org.eclipse.update.internal.core;

import org.eclipse.update.core.ICategory;

public class DefaultCategory implements ICategory {
	
	private String name;
	private String label;
	
	/**
	 * Default Constructor
	 */
	public DefaultCategory(){}
	
	/**
	 * Constructor
	 */
	public DefaultCategory(String name, String label){
		this.name = name;
		this.label = label;
	}


	/**
	 * @see ICategory#getName()
	 */
	public String getName() {
		return null;
	}

	/**
	 * @see ICategory#getLabel()
	 */
	public String getLabel() {
		return null;
	}

	/**
	 * Sets the name
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the label
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}

