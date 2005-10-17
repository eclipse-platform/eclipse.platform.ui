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

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.ILineRange;

/**
 * A change region describes a contiguous range of lines that was changed in the same revision
 * of a document.
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
class ChangeRegion {
	final ILineRange fLines;
	final Revision fRevision;
	/**
	 * Creates a new change region for the given revision and line range.
	 * 
	 * @param revision the revision of the new region
	 * @param lines the line range of the new region
	 */
	ChangeRegion(Revision revision, ILineRange lines) {
		Assert.isLegal(revision != null);
		Assert.isLegal(lines != null);
		fLines= lines;
		fRevision=revision;
	}
	
	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ChangeRegion [" + fRevision.toString() + ", [" + fLines.getStartLine() + "+" + fLines.getNumberOfLines() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
