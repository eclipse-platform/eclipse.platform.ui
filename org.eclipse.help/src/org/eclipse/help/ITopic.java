/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * ITopic is one topic in a hierarchy of topics.
 * 
 * @since 2.0
 */
public interface ITopic extends IHelpResource {
	/**
	 * This is element name used for topic in XML files.
	 */
	public final static String TOPIC = "topic"; //$NON-NLS-1$

	/**
	 * Obtains the topics contained in this node.
	 * 
	 * @return Array of ITopic
	 */
	public ITopic[] getSubtopics();
}

