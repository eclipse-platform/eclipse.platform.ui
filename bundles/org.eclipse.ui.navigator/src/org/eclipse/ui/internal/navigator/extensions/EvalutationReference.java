/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import java.lang.ref.SoftReference;

/**
 * @since 3.3
 * 
 */
public class EvalutationReference extends SoftReference {

	private final int hashCode;

	private final Class type;

	/**
	 * @param referent
	 *            The object to be referenced
	 */
	public EvalutationReference(Object referent) {
		super(referent);
		hashCode = referent.hashCode();
		type = referent.getClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		else if (obj instanceof EvalutationReference) {
			if (!type.equals(((EvalutationReference) obj).type))
				return false;
			return hashCode == obj.hashCode();
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		Object referent = get();
		if(referent == null)
			return "Evalutation[type="+ type +"]";  //$NON-NLS-1$//$NON-NLS-2$
		return "Evalutation[referent="+ referent +"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
