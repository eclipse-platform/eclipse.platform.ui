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
package org.eclipse.help;


/**
 * IToc is the table of contents.  It contains help topics.
 * @since 2.0
 */
public interface IToc extends IHelpResource {
	/**
	 * This is element name used for TOC in XML files.
	 */
	public final static String TOC = "toc";
	/**
	 * This is the attribute used for description topic in XML files.
	 */
	public final static String TOPIC = "topic";
	
	/**
	 * Obtains the topics directly contained by a toc.
	 * @return Array of ITopic
	 */
	public ITopic[] getTopics();
	
	/**
	 * Returns a topic with the specified href defined by this TOC.
	 * <br> If the TOC contains multiple 
	 * topics with the same href only of them (arbitrarily chosen) will 
	 * be returned.
	 * <p> If no topic is specified, then the TOC description topic is 
	 * returned, or null if there is no description topic for the TOC.
	 * </p>
	 * @param href The topic's URL.
	 * @return ITopic
	 */
	public ITopic getTopic(String href);
}

