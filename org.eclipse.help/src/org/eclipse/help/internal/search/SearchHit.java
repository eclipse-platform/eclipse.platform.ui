/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import org.eclipse.help.IToc;

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
	 * @param toc TOC containing topic or null
	 */
	public SearchHit(String href, String label, float score, IToc toc) {
		this.href = href;
		this.label = label;
		this.score = score;
		this.toc = toc;
	}
	/**
	 * Gets the href.
	 * @return Returns a String
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Gets the label.
	 * @return Returns a String
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * Gets the score.
	 * @return Returns a float
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Gets the toc.
	 * @return Returns IToc or null
	 */
	public IToc getToc() {
		return toc;
	}

}