/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.search;

import java.net.URL;

import org.eclipse.help.IToc;

/**
 * This class holds information for a single Search Result
 * @since 3.6
 * 
 */
public interface ISearchResult {
	
	/**
	 * Get the href of this search result topic
	 * 
	 * @return
	 */
	public String getHref();
	
	/**
	 * Get the description of this search result topic
	 * 
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Gets a summary of this search result
	 * 
	 * @return
	 */
	public String getSummary();
	
	/**
	 * Gets the label for this search result
	 * 
	 * @return
	 */
	public String getLabel();

	/**
	 * Gets the ID
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * Gets the participant ID
	 * 
	 * @return
	 */
	public String getParticipantId();
	
	/**
	 * Gets the Icon for this topic
	 * 
	 * @return
	 */
	public URL getIcon();
	
	/**
	 * Gets the score float value associated with this search result
	 * 
	 * @return
	 */
	public float getScore();

	/**
	 * Gets the IToc parent of this result
	 * 
	 * @return
	 */
	public IToc getToc();
	
	/**
	 * Returns whether the result a potential hit
	 * 
	 * @return
	 */
	public boolean isPotentialHit();
}
