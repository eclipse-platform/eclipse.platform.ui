/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.help.IHelpResource;

/**
 * IToc is the table of contents (contains help topics)
 * @since 2.0
 */
public interface IToc extends IHelpResource {
	public final static String TOC = "toc";
	
	/**
	 * Returns the topics directly contained by a toc.
	 * @return Array of ITopic
	 */
	public ITopic[] getTopics();
}

