/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help;

/**
 * <code>IToc2</code> extends <code>IToc </code> by adding methods to support functionality
 * for criteria, topic sorting and custom icons
 * @since 3.5
 */
public interface IToc2 extends IToc{

	/**
	 * Return the criteria information of this toc.
	 * 
	 * @return array of CriterionResource
	 */
	public ICriteria[] getCriteria();
	
	/**
	 * Toc elements can have non standard icons which are declared using a
	 * tocIcon element in the org.eclipse.help.toc extension point 
	 * @return NULL if the standard icons are to be used, otherwise the name of
	 * an icon declared in an org.eclipse.help.toc extension 
	 */
	public String getIcon();
	
	/**
	 * Allows child elements to be sorted alphabetically regardless of their actual
	 * order in the list of children.
	 * @return true if the children should be sorted alphabetically
	 */
	public boolean isSorted();
}
