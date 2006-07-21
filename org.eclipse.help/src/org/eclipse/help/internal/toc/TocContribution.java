/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

	private String id;
	private String categoryId;
	private String locale;
	private Toc toc;
	private String linkTo;
	private boolean isPrimary;
	private String[] extraDocuments;
	
	public TocContribution(String id, String categoryId, String locale, Toc toc, String linkTo, boolean isPrimary, String[] extraDocuments) {
		this.categoryId = categoryId;
		this.extraDocuments = extraDocuments;
		this.id = id;
		this.locale = locale;
		this.toc = toc;
		this.isPrimary = isPrimary;
		this.linkTo = linkTo;
	}
	
	public void addExtraDocuments(String[] docsToAdd) {
		if (extraDocuments == null) {
			extraDocuments = docsToAdd;
		}
		else if (docsToAdd != null && docsToAdd.length > 0) {
			String[] combined = new String[extraDocuments.length + docsToAdd.length];
			System.arraycopy(extraDocuments, 0, combined, 0, extraDocuments.length);
			System.arraycopy(docsToAdd, 0, combined, extraDocuments.length, docsToAdd.length);
			extraDocuments = combined;
		}
	}
	
	public String getCategoryId() {
		return categoryId;
	}

	public String[] getExtraDocuments() {
		return extraDocuments;
	}

	public String getId() {
		return id;
	}

	public String getLocale() {
		return locale;
	}

	public IToc getToc() {
		return toc;
	}

	public boolean isPrimary() {
		return isPrimary;
	}
	
	public String getLinkTo() {
		return linkTo;
	}
}
