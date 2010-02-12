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
 * ICriteriaDefinition represents the criteria definition of one plug-in.
 * It contains criterion definitions, each of them is criterion id and its
 * display name, and criterion values id and their display names. 
 * 
 * @since 3.5
 */

public interface ICriteriaDefinition extends IUAElement{

	/**
	 * Obtains the criterion definitions contained in the definition file.
	 * 
	 * @return Array of ICriterionDefinition
	 */
	ICriterionDefinition[] getCriterionDefinitions();
}
