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

package org.eclipse.ui.commands;

import java.util.List;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface ICommand extends Comparable {

	// TODO getInContext(), List getKeySequences(), Map getImageUrisByStyle(), SortedSet getContextIds();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getCategoryId();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getContextBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getDescription();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getHelpId();
		
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getId();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getImageBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	List getKeyBindings();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getName();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isActive();
}
