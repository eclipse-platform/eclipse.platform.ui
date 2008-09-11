/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.revisions;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.source.ILineRange;


/**
 * An unmodifiable line range that belongs to a {@link Revision}.
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class RevisionRange implements ILineRange {
	private final Revision fRevision;
	private final int fStartLine;
	private final int fNumberOfLines;

	RevisionRange(Revision revision, ILineRange range) {
		Assert.isLegal(revision != null);
		fRevision= revision;
		fStartLine= range.getStartLine();
		fNumberOfLines= range.getNumberOfLines();
	}

	/**
	 * Returns the revision that this range belongs to.
	 *
	 * @return the revision that this range belongs to
	 */
	public Revision getRevision() {
		return fRevision;
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineRange#getStartLine()
	 */
	public int getStartLine() {
		return fStartLine;
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineRange#getNumberOfLines()
	 */
	public int getNumberOfLines() {
		return fNumberOfLines;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "RevisionRange [" + fRevision.toString() + ", [" + getStartLine() + "+" + getNumberOfLines() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
