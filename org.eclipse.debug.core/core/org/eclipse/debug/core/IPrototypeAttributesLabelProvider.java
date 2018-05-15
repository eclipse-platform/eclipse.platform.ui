/*******************************************************************************
 * Copyright (c) 2017 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

/**
 * A prototype attributes label provider is contributed as an optional attribute
 * of a <code>launchConfigurationType</code> extension and is responsible for
 * displaying launch configurations prototype attributes of that type.
 *
 * @since 3.12
 */
public interface IPrototypeAttributesLabelProvider {

	/**
	 * Get a human readable label to associate to this attribute.
	 *
	 * @param attribute the given attribute.
	 * @return a human readable label of this attribute.
	 */
	String getAttributeLabel(String attribute);
}
