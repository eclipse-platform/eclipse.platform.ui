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

package org.eclipse.ui.internal.commands.registry.old;

import java.io.IOException;
import java.util.List;

/**
 * JAVADOC
 * 
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IMutableRegistry extends IRegistry {

	/**
	 * JAVADOC
	 * 
	 * @throws IOException
	 */	
	void save()
		throws IOException;

	/**
	 * JAVADOC
	 * 
	 * @param activeGestureConfigurations
	 */	
	void setActiveGestureConfigurations(List activeGestureConfigurations)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param activeKeyConfigurations
	 */	
	void setActiveKeyConfigurations(List activeKeyConfigurations)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param categories
	 */	
	void setCategories(List categories)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param commands
	 */
	void setCommands(List commands)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 * 
	 * @param contextBindings
	 */
	void setContextBindings(List contextBindings)
		throws IllegalArgumentException;

	/**
	 * JAVADOC
	 * 
	 * @param contexts
	 */
	void setContexts(List contexts)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param gestureBindings
	 */
	void setGestureBindings(List gestureBindings)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param gestureConfigurations
	 */
	void setGestureConfigurations(List gestureConfigurations)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param keyBindings
	 */
	void setKeyBindings(List keyBindings)
		throws IllegalArgumentException;
	
	/**
	 * JAVADOC
	 * 
	 * @param keyConfigurations
	 */
	void setKeyConfigurations(List keyConfigurations)
		throws IllegalArgumentException;
}
