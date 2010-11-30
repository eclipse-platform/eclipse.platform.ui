/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.base;

/**
 * @since 3.6
 * Abstract class representing a help display which can be used to override the Eclipse help
 * system UI using the extension point org.eclipse.help.base.display. Classes extending this
 * abstract class must be capable of returning the help home page and other help related URLs 
 */

public abstract class AbstractHelpDisplay {
	
	/**
	 * Returns the URL to the help home page 
	 * @param hostname the hostname of the Eclipse help system
	 * @param port the port of the Eclipse help system
	 * @param tab is one of "search" "toc" "index" "bookmarks" or null, 
	 * In the Eclipse help webapp these correspond to a tab which is in focus when the help 
	 * system is started.
	 * For other help presentations this parameter should be seen as a hint representing 
	 * an action the user wishes to perform
	 * @return String help home path
	 */
	public abstract String getHelpHome(String hostname, int port, String tab);

	/** 
	 * Returns the help page, including any frames, for a specific topic.
	 * @param hostname the hostname of the Eclipse help system
	 * @param port the port of the Eclipse help system
	 * @param topic The path of a topic in the help system. May be a relative path,
	 * representing a topic within the help system or a full URL including protocol.
	 * @return String URL translated for overriding help system
	 */
	public abstract String getHelpForTopic(String topic, String hostname, int port);
}
