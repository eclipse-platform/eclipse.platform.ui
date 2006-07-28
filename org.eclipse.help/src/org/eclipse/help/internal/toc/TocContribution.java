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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;

public class TocContribution implements ITocContribution {

	private String id;
	private String categoryId;
	private String locale;
	private Toc toc;
	private String linkTo;
	private boolean isPrimary;
	private List extraDocuments = new ArrayList();
	
	public TocContribution(String id, String categoryId, String locale, Toc toc, String linkTo, boolean isPrimary, String[] extraDocuments) {
		this.categoryId = categoryId;
		if (extraDocuments != null) {
			this.extraDocuments.addAll(Arrays.asList(extraDocuments));
		}
		this.id = id;
		this.locale = locale;
		this.toc = toc;
		this.isPrimary = isPrimary;
		this.linkTo = linkTo;
	}
	
	public void addExtraDocument(String docToAdd) {
		extraDocuments.add(docToAdd);
	}
	
	public void addExtraDocuments(String[] docsToAdd) {
		extraDocuments.addAll(Arrays.asList(docsToAdd));
	}
	
	public String getCategoryId() {
		return categoryId;
	}

	public String[] getExtraDocuments() {
		return (String[])extraDocuments.toArray(new String[extraDocuments.size()]);
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
	
	public void setToc(Toc toc) {
		this.toc = toc;
	}
}
