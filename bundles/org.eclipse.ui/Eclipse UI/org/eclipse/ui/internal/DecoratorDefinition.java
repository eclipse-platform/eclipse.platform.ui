package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.ILabelDecorator;

/**
 * The DecoratorDefinition is the class that holds onto
 * the label decorator, the name and the name of the
 * class a decorator definition applies to,
 */

public class DecoratorDefinition {

	private String name;
	private String objectClass;
	private ILabelDecorator decorator;
	private boolean enabled;

	/**
	 * Create a new instance of the receiver with the
	 * supplied values.
	 */

	DecoratorDefinition(
		String label,
		String className,
		boolean apply,
		ILabelDecorator definedDecorator) {
		name = label;
		objectClass = className;
		enabled = apply;
		decorator = definedDecorator;
	}

	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the objectClass.
	 * @return Returns a String
	 */
	public String getObjectClass() {
		return objectClass;
	}

	/**
	 * Sets the objectClass.
	 * @param objectClass The objectClass to set
	 */
	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	/**
	 * Gets the decorator.
	 * @return Returns a ILabelDecorator
	 */
	public ILabelDecorator getDecorator() {
		return decorator;
	}

	/**
	 * Sets the decorator.
	 * @param decorator The decorator to set
	 */
	public void setDecorator(ILabelDecorator decorator) {
		this.decorator = decorator;
	}

	/**
	 * Gets the enabled.
	 * @return Returns a boolean
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled.
	 * @param enabled The enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}