package org.eclipse.ui;

import org.eclipse.jface.viewers.ILabelDecorator;

/**
 * Manages the decorators contributed via the decorators extension point.
 * Views which allow decoration of their elements should use the label
 * decorator returned by <code>getLabelDecorator()</code>.
 */

public interface IDecoratorManager extends ILabelDecorator {

	/**
	 * Returns the label decorator which applies the decorations from all
	 * enabled decorators.
	 * Views which allow decoration of their elements should use this 
	 * label decorator.
	 *
	 * @return the label decorator
	 * @see DecoratingLabelProvider
	 */
	ILabelDecorator getLabelDecorator();

	/**
	 * Returns the label decorator for the specified decoratorID if it
	 * exists.
	 * 
	 * @return ILabelDecorator
	 * @param decoratorId String
	 * @see DecoratingLabelProvider
	 */
	ILabelDecorator getLabelDecorator(String decoratorId);

	/**
	 * Returns whether the specified decorator is enabled.
	 * 
	 * @param decoratorId the decorator id
	 * @return <code>true</code> if the decorator is enabled, or
	 * <code>false</code> if not
	 */
	boolean getEnabled(String decoratorId);

	/**
	 * Sets whether the specified decorator is enabled.
	 * 
	 * @param decoratorId the decorator id
	 * @param enabled <code>true</code> to enable the decorator, or
	 * <code>false</code> to disable it
	 */
	void setEnabled(String decoratorId, boolean enabled);

	/**
	 * Reset the cachedDecorators and fire listeners as
	 * the enabled state of some decorators has changed.
	 * Also store the currently enabled decorators as
	 * a workbench preference.
	 */
	public void reset();
}