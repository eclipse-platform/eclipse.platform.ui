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

import org.eclipse.help.*;

/**
 * Part of help navigation model corresponding to TOC element. It may contain
 * TOPIC, LINK, or ANCHOR elements.
 * 
 * @since 3.0
 */
public interface ITocElement extends IToc, INavigationElement {
	/**
	 * @return the URL (as a string) of description topic.
	 */
	public String getTocTopicHref();
	/**
	 * Returns a topic with the specified href defined by this TOC, without
	 * looking in children TOCs <br>
	 * If the TOC contains multiple topics with the same href only of them
	 * (arbitrarily chosen) will be returned. TOC Descritpion topic is ignored.
	 * 
	 * @param href
	 *            the topic's URL.
	 * @return ITopic or null
	 */
	public ITopic getOwnedTopic(String href);
	/**
	 * Returns a topic with the specified href found in extra dir defined by
	 * this TOC, without looking in children TOCs
	 * 
	 * @param href
	 *            the topic's URL.
	 * @return ITopic or null
	 */
	public ITopic getOwnedExtraTopic(String href);
	/**
	 * Gets the childrenTocs.
	 * 
	 * @return Returns a List of ITocElement
	 */
	public List getChildrenTocs();
}
