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
 * A directive indicating the criteria information of a Toc or Topic described in xml
 * 
 * @since 3.5
 */

public interface ICriteria extends IUAElement {

	/**
	 * Returns the name of the criteria element, e.g.
	 * "Platform"
	 * 
	 * @return the name of the criteria element
	 */
	public String getName();
	
	/**
	 * Returns the value of the criteria element, e.g.
	 * "AIX,Windows"
	 * 
	 * @return the value of the criteria element
	 */
	public String getValue();
}
