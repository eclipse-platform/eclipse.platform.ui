/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;

public class TocContribution implements ITocContribution {

	private String categoryId;
	private String contributorId;
	private String[] extraDocuments;
	private String id;
	private String locale;
	private Toc toc;
	private boolean isPrimary;
	
	public String getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getContributorId() {
		return contributorId;
	}
	
	public void setContributorId(String contributorId) {
		this.contributorId = contributorId;
	}
	
	public String[] getExtraDocuments() {
		return extraDocuments;
	}
	
	public void setExtraDocuments(String[] extraDocuments) {
		this.extraDocuments = extraDocuments;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLocale() {
		return locale;
	}
	
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public IToc getToc() {
		return toc;
	}
	
	public void setToc(Toc toc) {
		this.toc = toc;
		toc.setTocContribution(this);
	}
	
	public boolean isPrimary() {
		return isPrimary;
	}
	
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
}
