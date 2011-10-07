/***************************************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others. All rights reserved. This program and the
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
public class SearchHit implements ISearchEngineResult2, Comparable<SearchHit> {

	private String href;
	private String label;
	private float score;
	private IToc toc;
	private String summary;
	private String id;
	private String participantId;
	private boolean isPotentialHit;

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
	 * @param isPotentialHit this may be a false positive hit
	 */
	public SearchHit(String href, String label, String summary, float score, IToc toc, String id,
			String participantId, boolean isPotentialHit) {
		this.href = href;
		this.label = label;
		this.score = score;
		this.toc = toc;
		this.summary = summary;
		this.id = id;
		this.participantId = participantId;
		this.isPotentialHit = isPotentialHit;
	}
	
	public int compareTo(SearchHit o) {
		if (o == this) {
			return 0;
		}
		float s1 = ((SearchHit)this).getScore();
		float s2 = ((SearchHit)o).getScore();
		return (s1 < s2 ? 1 : s1 > s2 ? -1 : 0);
	}

	public boolean equals(Object obj) {
		if (obj instanceof SearchHit) {
			if (obj == this) {
				return true;
			}
			return ((SearchHit)obj).getHref().equals(href);
		}
		return false;
	}
	
	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}

	public float getScore() {
		return score;
	}

	public IToc getToc() {
		return toc;
	}
	
	public int hashCode() {
		return href.hashCode();
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public void setPotentialHit(boolean isPotentialHit) {
		this.isPotentialHit = isPotentialHit;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setToc(IToc toc) {
		this.toc = toc;
	}

	public String getDescription() {
		return getSummary();
	}

	public IHelpResource getCategory() {
		if (participantId == null)
			return toc;
		return BaseHelpSystem.getLocalSearchManager().getParticipantCategory(participantId);
	}

	public String getSummary() {
		return summary;
	}

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
	
	public String getRawId() {
		return id; 
	}

	public String getParticipantId() {
		return participantId;
	}

	public URL getIconURL() {
		if (participantId == null)
			return null;
		return BaseHelpSystem.getLocalSearchManager().getParticipantIconURL(participantId);
	}

	public boolean canOpen() {
		return participantId != null;
	}
	
	public boolean isPotentialHit() {
		return isPotentialHit;
	}

}
