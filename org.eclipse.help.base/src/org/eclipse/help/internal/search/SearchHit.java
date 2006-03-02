/***************************************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.help.internal.search;

import java.net.URL;

import org.eclipse.help.IHelpResource;
import org.eclipse.help.IToc;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.search.ISearchEngineResult2;

/**
 * A search result containing a document reference, score, summary, etc.
 */
public class SearchHit implements ISearchEngineResult2, Comparable {

	private String href;
	private String label;
	private float score;
	private IToc toc;
	private String summary;
	private String id;
	private String participantId;
	private String filters;

	/**
	 * Constructs a new SearchHit.
	 * 
	 * @param href the href to the document
	 * @param label a label describing the hit
	 * @param summary a summary paragraph further describing the hit
	 * @param score how relevant this hit is thought to be
	 * @param toc the matching element in the TOC
	 * @param id the unique id of the document
	 * @param participantId the participant the hit came from
	 * @param filters all the filters this doc is sensitive to
	 */
	public SearchHit(String href, String label, String summary, float score, IToc toc, String id,
			String participantId, String filters) {
		this.href = href;
		this.label = label;
		this.score = score;
		this.toc = toc;
		this.summary = summary;
		this.id = id;
		this.participantId = participantId;
		this.filters = filters;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o == this) {
			return 0;
		}
		float s1 = ((SearchHit)this).getScore();
		float s2 = ((SearchHit)o).getScore();
		return (s1 < s2 ? 1 : s1 > s2 ? -1 : 0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof SearchHit) {
			if (obj == this) {
				return true;
			}
			return ((SearchHit)obj).getHref().equals(href);
		}
		return false;
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

	/**
	 * Returns the filters for which this hit's document is sensitive to.
	 * e.g. "os=linux,ws=gtk,plugin=org.eclipse.help"
	 */
	public String getFilters() {
		return filters;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return href.hashCode();
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * Sets the filters for which this hit's document is sensitive to.
	 * e.g. "os=linux,ws=gtk,plugin=org.eclipse.help"
	 */
	public void setFilters(String filters) {
		this.filters = filters;
	}
	
	public void setScore(float score) {
		this.score = score;
	}

	public void setToc(IToc toc) {
		this.toc = toc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngineResult#getDescription()
	 */
	public String getDescription() {
		return getSummary();
	}

	public IHelpResource getCategory() {
		if (participantId == null)
			return toc;
		return BaseHelpSystem.getSearchManager().getParticipantCategory(participantId);
	}

	/**
	 * @return Returns the summary.
	 */
	public String getSummary() {
		return summary != null ? (summary + "...") : null; //$NON-NLS-1$
	}

	/**
	 * @param summary
	 *            The summary to set.
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean getForceExternalWindow() {
		return participantId == null ? false : true;
	}

	public String toAbsoluteHref(String href, boolean frames) {
		return href;
	}

	public String getId() {
		return participantId + "/" + id; //$NON-NLS-1$
	}

	public String getParticipantId() {
		return participantId;
	}

	public URL getIconURL() {
		if (participantId == null)
			return null;
		return BaseHelpSystem.getSearchManager().getParticipantIconURL(participantId);
	}

	public boolean canOpen() {
		return participantId != null;
	}
}
