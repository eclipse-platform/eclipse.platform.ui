/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.topics;
/**
 * ITopics is a root of a hierarchy of topics.
 */
public interface ITopics extends IDescriptor, ITopicNode {
	public final static String TOPICS = "topics";
	
	/**
	 * Returns a topic with the specified href.
	 * <br> It is possible that multiple topics have 
	 * the same href, in which case there is no guarantee 
	 * which one is returned.
	 * @param href The topic's href value.
	 */
	ITopic getTopic(String href);
}

