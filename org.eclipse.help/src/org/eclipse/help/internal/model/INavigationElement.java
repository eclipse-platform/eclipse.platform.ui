/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.model;

import java.util.*;

/**
 * Part of help navigation model corresponding to any of: TOC, TOPIC, ANCHOR, or
 * LINK element. After navigation is built, this may contain TOC, TOPIC, LINK,
 * or ANCHOR elements.
 * 
 * @since 3.0
 */
public interface INavigationElement {
	/**
	 * Returns child elements
	 * 
	 * @return List of INavigationElement
	 */
	List getChildren();

}
