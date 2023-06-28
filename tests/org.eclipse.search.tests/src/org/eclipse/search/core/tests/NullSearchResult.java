/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.core.tests;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

import org.eclipse.search.internal.ui.text.FileSearchResult;

public class NullSearchResult extends FileSearchResult { // inherit from FileSearchResult so a search result view can be found

	private final NullQuery fNullQuery;
	public NullSearchResult(NullQuery query) {
		super(null);
		fNullQuery= query;
	}
	@Override
	public String getLabel() {
		return "Null Query"; //$NON-NLS-1$
	}
	@Override
	public String getTooltip() {
		return "Null Query"; //$NON-NLS-1$
	}
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	@Override
	public ISearchQuery getQuery() {
		return fNullQuery;
	}
	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return null;
	}
	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return null;
	}
}
