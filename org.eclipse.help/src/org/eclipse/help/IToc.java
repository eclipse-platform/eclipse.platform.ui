/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.help.IHelpResource;

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
	 * Obtains the topics directly contained by a toc.
	 * @return Array of ITopic
	 */
	public ITopic[] getTopics();
	
	/**
	 * Returns a topic with the specified href.
	 * <br> It is possible that a TOC can contain multiple 
	 * topics with the same href, in which case there is no
	 * guarantee on which topic is returned.
	 * @param href The topic's href value.
	 * @return ITopic
	 */
	public ITopic getTopic(String href);
}

