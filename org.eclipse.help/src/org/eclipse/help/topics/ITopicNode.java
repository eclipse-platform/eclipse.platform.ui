/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.topics;
import java.util.List;
/**
 * ITopicNode is an object in the chierarchy
 * of topics.  It can contains other topics
 * below it.
 */
public interface ITopicNode {
	/**
	 * Obtains list of topics
	 * in this folder
	 * @return List of ITopicNode
	 */
	public List getChildTopics();
}