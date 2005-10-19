/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

public final class Hunk {
	public final int line;
	public final int delta;
	public final int changed;
	
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
		return "Hunk [" + line + ">" + changed + (delta < 0 ? "-" : "+") + Math.abs(delta) +"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
}