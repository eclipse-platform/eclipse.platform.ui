/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

/**
 * Search hit.
 */
public class SearchHit {
	private String href;
	private String label;
	private float score;
	public SearchHit(String href, String label, float score) {
		this.href = href;
		this.label = label;
		this.score = score;
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

}