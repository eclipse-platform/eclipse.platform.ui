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
	 * @return the href of this search result topic
	 */
	public String getHref();
	
	/**
	 * Get the description of this search result topic
	 * 
	 * @return the description of this search result topic
	 */
	public String getDescription();
	
	/**
	 * Gets a summary of this search result
	 * 
	 * @return a summary of this search result
	 */
	public String getSummary();
	
	/**
	 * Gets the label for this search result
	 * 
	 * @return the label for this search result
	 */
	public String getLabel();

	/**
	 * Gets the ID
	 * 
	 * @return the ID
	 */
	public String getId();
	
	/**
	 * Gets the participant ID
	 * 
	 * @return the participant ID
	 */
	public String getParticipantId();
	
	/**
	 * Gets the Icon for this topic
	 * 
	 * @return the Icon for this topic
	 */
	public URL getIcon();
	
	/**
	 * Gets the score float value associated with this search result
	 * 
	 * @return the score float value associated with this search result
	 */
	public float getScore();

	/**
	 * Gets the IToc parent of this result
	 * 
	 * @return the IToc parent of this result
	 */
	public IToc getToc();
	
	/**
	 * Returns whether the result a potential hit
	 * 
	 * @return <code>true</code> if the result a potential hit, <code>false</code> otherwise
	 */
	public boolean isPotentialHit();
}
