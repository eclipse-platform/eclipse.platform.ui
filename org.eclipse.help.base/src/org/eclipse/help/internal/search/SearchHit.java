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
package org.eclipse.help.internal.search;

import org.eclipse.help.*;

/**
 * Search hit.
 */
public class SearchHit {
	private String href;
	private String label;
	private float score;
	private IToc toc;
	/**
	 * Constructor
	 * 
	 * @param toc
	 *            TOC containing topic or null
	 */
	public SearchHit(String href, String label, float score, IToc toc) {
		this.href = href;
		this.label = label;
		this.score = score;
		this.toc = toc;
	}
	/**
	 * Gets the href.
	 * 
	 * @return Returns a String
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Gets the label.
	 * 
	 * @return Returns a String
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * Gets the score.
	 * 
	 * @return Returns a float
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Gets the toc.
	 * 
	 * @return Returns IToc or null
	 */
	public IToc getToc() {
		return toc;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setToc(IToc toc) {
		this.toc = toc;
	}
}
