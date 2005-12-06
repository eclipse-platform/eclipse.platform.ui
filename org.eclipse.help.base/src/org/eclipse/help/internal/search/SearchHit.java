/***************************************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
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
 * Search hit.
 */
public class SearchHit implements ISearchEngineResult2 {

	private String href;
	private String label;
	private float score;
	private IToc toc;
	private String summary;
	private String id;
	private String participantId;

	/**
	 * Constructor
	 * 
	 * @param toc
	 *            TOC containing topic or null
	 */
	public SearchHit(String href, String label, String summary, float score, IToc toc, String id,
			String participantId) {
		this.href = href;
		this.label = label;
		this.score = score;
		this.toc = toc;
		this.summary = summary;
		this.id = id;
		this.participantId = participantId;
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
		return participantId + "/" + id;
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
