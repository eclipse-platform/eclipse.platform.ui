/*******************************************************************************
 * Copyright (c) 2002, 2011 Object Factory Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.dtd.schema;

import org.eclipse.ant.internal.ui.dtd.IAtom;

/**
 * Atom contains information common to elements and attributes.
 * @author Bob Foster
 */
public class Atom implements IAtom {
	
	protected String fName;
	protected int fKind;

	protected Atom(int kind, String name) {
		fKind = kind;
		fName = name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.dtd.IAtom#getName()
	 */
	public String getName() {
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return fName;
	}
}
