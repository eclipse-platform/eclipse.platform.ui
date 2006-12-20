/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.history;

import org.eclipse.core.resources.IFile;
import org.eclipse.team.internal.ui.history.EditionHistoryPage;
import org.eclipse.ui.part.Page;

/**
 * A history page source that can create history pages for a sub-element of a file.
 * @since 3.3
 */
public abstract class ElementLocalHistoryPageSource extends HistoryPageSource {

	/**
	 * Create an instance of the page source.
	 */
	public ElementLocalHistoryPageSource() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.history.IHistoryPageSource#canShowHistoryFor(java.lang.Object)
	 */
	public final boolean canShowHistoryFor(Object object) {
		return getFile(object) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.history.IHistoryPageSource#createPage(java.lang.Object)
	 */
	public final Page createPage(Object object) {
		return new EditionHistoryPage(getFile(object), object);
	}
	
	/**
	 * Return the file that contains the given element of <code>null</code>
	 * if this page source can not show history for the given element.
	 * @param element the element
	 * @return the file that contains the given element of <code>null</code>
	 */
	protected abstract IFile getFile(Object element);
	
}
