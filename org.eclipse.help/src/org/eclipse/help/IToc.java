/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.help.IHelpResource;

/**
 * IToc is the table of contents.  It contains help topics.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
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

