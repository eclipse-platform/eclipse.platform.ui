/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.help.IHelpResource;

/**
 * ITopic is one topic in a hierarchy of topics.
 * @since 2.0
 */
public interface ITopic extends IHelpResource {
	/**
	 * This is element name used for topic in XML files.
	 */
	public final static String TOPIC = "topic";
	
	/**
	 * Obtains the topics contained in this node.
	 * @return Array of ITopic
	 */
	public ITopic[] getSubtopics();
}

