/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.help.IHelpResource;

/**
 * ITopic is one topic in a hierarchy of topics.
 */
public interface ITopic extends IHelpResource {
	public final static String TOPIC = "topic";
	
	/**
	 * Returns the topics contained in this node
	 * @return Array of ITopic
	 */
	public ITopic[] getSubtopics();
}

