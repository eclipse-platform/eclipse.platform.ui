/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.revisions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.Assert;

/**
 * Encapsulates revision info for one line-based document.
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
public final class RevisionInformation {
	private final List fRevisions= new ArrayList();
	private final List fRORevisions= Collections.unmodifiableList(fRevisions);
	
	/**
	 * Creates a new annotate information object.
	 */
	public RevisionInformation() {
	}
	
	/**
	 * Adds a revision.
	 * 
	 * @param revision a revision
	 */
	public void addRevision(Revision revision) {
		Assert.isLegal(revision != null);
		fRevisions.add(revision);
	}

	/**
	 * Returns the contained revisions.
	 * 
	 * @return an unmodifiable view of the contained revisions (element type: {@link Revision})
	 */
	public List getRevisions() {
		return fRORevisions;
	}
	
}
