/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.help.IHelpResource;

/**
 * ITopic is one topic in a hierarchy of topics.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
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

