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

package org.eclipse.help.internal.search;

import java.net.URL;

import org.eclipse.help.IToc;
import org.eclipse.help.search.ISearchResult;

public class SearchResult implements ISearchResult{

	private String href;
	private String id;
	private String participantId;
	private String description;
	private String summary;
	private String label;
	private URL icon;
	private float score;
	private IToc toc;
	private boolean isPotentialHit;
	
	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public URL getIcon() {
		return icon;
	}
	
	public void setIcon(URL icon) {
		this.icon = icon;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}

	
	public IToc getToc() {
		return toc;
	}

	
	public void setToc(IToc toc) {
		this.toc = toc;
	}

	
	public String getParticipantId() {
		return participantId;
	}

	
	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	public boolean isPotentialHit() {
		return isPotentialHit;
	}

	public void setPotentialHit(boolean isPotentialHit) {
		this.isPotentialHit = isPotentialHit;
	}
		
}
