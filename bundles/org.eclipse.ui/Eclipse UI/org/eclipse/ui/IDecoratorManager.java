package org.eclipse.ui;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelDecorator;

/**
 * Manages the decorators contributed via the decorators extension point.
 * Views which allow decoration of their elements should use the label
 * decorator returned by <code>getLabelDecorator()</code>.
 */

public interface IDecoratorManager {

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
	 * Returns the label decorator for the specified decorator if it
	 * is enabled. Otherwise returns <code>null</code>.
	 * 
	 * @param decoratorId the decorator id
	 * @return the label decorator
	 */
	ILabelDecorator getLabelDecorator(String decoratorId);

	/**
	 * Returns whether the specified decorator is enabled.
	 * 
	 * @param decoratorId the decorator id
	 * @return <code>true</code> if the decorator is enabled, or
	 *   <code>false</code> if not
	 */
	boolean getEnabled(String decoratorId);

	/**
	 * Sets whether the specified decorator is enabled.
	 * 
	 * @param decoratorId the decorator id
	 * @param enabled <code>true</code> to enable the decorator, or
	 * <code>false</code> to disable it
	 * @throws CoreException if the decorator cannot be instantiated
	 */
	void setEnabled(String decoratorId, boolean enabled) throws CoreException;		

}