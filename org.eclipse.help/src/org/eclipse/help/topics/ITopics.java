/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.topics;

import org.eclipse.help.IHelpResource;

/**
 * ITopics is a root of a hierarchy of topics.
 */
public interface ITopics extends IHelpResource {
	public final static String TOPICS = "topics";
	
	/**
	 * Returns the topics contained in this node
	 * @return Array of ITopic
	 */
	public ITopic[] getTopics();
	
	/**
	 * Returns a topic with the specified href.
	 * <br> It is possible that multiple topics have 
	 * the same href, in which case there is no guarantee 
	 * which one is returned. The search can traverse the entire
	 * tree to find the first topic by the specified href.
	 * @param href The topic's href value.
	 */
	ITopic getTopic(String href);
}

