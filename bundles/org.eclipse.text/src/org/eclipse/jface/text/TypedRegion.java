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
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * Default implementation of {@link org.eclipse.jface.text.ITypedRegion}. A
 * <code>TypedRegion</code> is a value object.
 */
public class TypedRegion extends Region implements ITypedRegion {

	/** The region's type */
	private final String fType;

	/**
	 * Creates a typed region based on the given specification.
	 *
	 * @param offset the region's offset
	 * @param length the region's length
	 * @param type the region's type
	 */
	public TypedRegion(int offset, int length, String type) {
		super(offset, length);
		fType= type;
	}

	@Override
	public String getType() {
		return fType;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TypedRegion r) {
			return super.equals(r) && ((fType == null && r.getType() == null) || fType.equals(r.getType()));
		}
		return false;
	}

	@Override
	public int hashCode() {
	 	int type= fType == null ? 0 : fType.hashCode();
	 	return super.hashCode() | type;
	 }

	@Override
	public String toString() {
		return fType + " - " + super.toString(); //$NON-NLS-1$
	}

}
