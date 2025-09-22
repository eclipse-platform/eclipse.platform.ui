/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Juerg Billeter, juergbi@ethz.ch - 47136 Search view should show match objects
 *     Ulrich Etter, etteru@ethz.ch - 47136 Search view should show match objects
 *     Roman Fuchs, fuchsro@ethz.ch - 47136 Search view should show match objects
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.Region;

import org.eclipse.search.ui.text.Match;

public class FileMatch extends Match {
	private final LineElement fLineElement;
	private Region fOriginalLocation;

	public FileMatch(IFile element) {
		super(element, -1, -1);
		fLineElement= null;
		fOriginalLocation= null;
	}

	public FileMatch(IFile element, int offset, int length, LineElement lineEntry) {
		super(element, offset, length);
		Assert.isLegal(lineEntry != null);
		fLineElement= lineEntry;
	}

	@Override
	public void setOffset(int offset) {
		if (fOriginalLocation == null) {
			// remember the original location before changing it
			fOriginalLocation= new Region(getOffset(), getLength());
		}
		super.setOffset(offset);
	}

	@Override
	public void setLength(int length) {
		if (fOriginalLocation == null) {
			// remember the original location before changing it
			fOriginalLocation= new Region(getOffset(), getLength());
		}
		super.setLength(length);
	}

	public int getOriginalOffset() {
		if (fOriginalLocation != null) {
			return fOriginalLocation.getOffset();
		}
		return getOffset();
	}

	public int getOriginalLength() {
		if (fOriginalLocation != null) {
			return fOriginalLocation.getLength();
		}
		return getLength();
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
