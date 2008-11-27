/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.equivalence;


/**
 * @since 3.2
 */
public final class IntHash extends Hash {

	private final int fHash;

	public IntHash(int hash) {
		fHash= hash;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IntHash) {
			return fHash == ((IntHash) obj).fHash;
		}

		return false;
	}
	
	public int hashCode() {
		return fHash;
	}

}
