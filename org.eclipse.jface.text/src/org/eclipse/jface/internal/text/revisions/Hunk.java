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
package org.eclipse.jface.internal.text.revisions;

import org.eclipse.jface.text.Assert;

/**
 * A hunk is a sequence of changed, added or deleted lines. <code>Hunk</code>s are separated by
 * one or more unchanged lines.
 * 
 * @since 3.2
 */
public final class Hunk {
	/** The line at which the hunk starts. */
	public final int line;
	/** The difference in lines compared to the corresponding line region in the original. */
	public final int delta;
	/** The number of changed lines in this hunk. */
	public final int changed;

	/**
	 * Creates a new hunk.
	 * 
	 * @param line the line at which the hunk starts
	 * @param delta the difference in lines compared to the original
	 * @param changed the number of changed lines in this hunk
	 */
	public Hunk(int line, int delta, int changed) {
		Assert.isLegal(line >= 0);
		Assert.isLegal(changed >= 0);
		this.line= line;
		this.delta= delta;
		this.changed= changed;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Hunk [" + line + ">" + changed + (delta < 0 ? "-" : "+") + Math.abs(delta) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
}