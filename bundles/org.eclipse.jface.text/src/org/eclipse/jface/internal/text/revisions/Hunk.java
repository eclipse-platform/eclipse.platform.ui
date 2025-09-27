/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.jface.internal.text.revisions;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;

/**
 * A hunk describes a contiguous range of changed, added or deleted lines. <code>Hunk</code>s are separated by
 * one or more unchanged lines.
 *
 * @since 3.3
 */
public final class Hunk {
	/**
	 * The line at which the hunk starts in the current document. Must be in
	 * <code>[0, numberOfLines]</code> &ndash; note the inclusive end; there may be a hunk with
	 * <code>line == numberOfLines</code> to describe deleted lines at then end of the document.
	 */
	public final int line;
	/**
	 * The difference in lines compared to the corresponding line range in the original. Positive
	 * for added lines, negative for deleted lines.
	 */
	public final int delta;
	/** The number of changed lines in this hunk, must be &gt;= 0. */
	public final int changed;

	/**
	 * Creates a new hunk.
	 *
	 * @param line the line at which the hunk starts, must be &gt;= 0
	 * @param delta the difference in lines compared to the original
	 * @param changed the number of changed lines in this hunk, must be &gt;= 0
	 */
	public Hunk(int line, int delta, int changed) {
		Assert.isLegal(line >= 0);
		Assert.isLegal(changed >= 0);
		this.line= line;
		this.delta= delta;
		this.changed= changed;
	}

	@Override
	public String toString() {
		return "Hunk [" + line + ">" + changed + (delta < 0 ? "-" : "+") + Math.abs(delta) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	@Override
	public int hashCode() {
		return Objects.hash(changed, delta, line);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Hunk other) {
			return other.line == this.line && other.delta == this.delta && other.changed == this.changed;
		}
		return false;
	}
}