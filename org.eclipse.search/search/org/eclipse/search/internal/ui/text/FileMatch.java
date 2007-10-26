/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Juerg Billeter, juergbi@ethz.ch - 47136 Search view should show match objects
 *     Ulrich Etter, etteru@ethz.ch - 47136 Search view should show match objects
 *     Roman Fuchs, fuchsro@ethz.ch - 47136 Search view should show match objects
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IFile;

import org.eclipse.search.ui.text.Match;

public class FileMatch extends Match {
	private LineElement fLineElement;
	
	public FileMatch(IFile element) {
		super(element, -1, -1);
		fLineElement= null;
	}
	
	public FileMatch(IFile element, int offset, int length, LineElement lineEntry) {
		super(element, offset, length);
		Assert.isLegal(lineEntry != null);
		fLineElement= lineEntry;
	}
	
	public LineElement getLineElement() {
		return fLineElement;
	}
	
	public IFile getFile() {
		return (IFile) getElement();
	}
	
	public boolean isFileSearch() {
		return fLineElement == null;
	}
}
