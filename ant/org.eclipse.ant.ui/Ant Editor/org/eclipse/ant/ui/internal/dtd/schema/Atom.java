/*******************************************************************************
 * Copyright (c) 2002, 2003 Object Factory Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.dtd.schema;

import org.eclipse.ant.ui.internal.dtd.IAtom;

/**
 * Atom contains information common to elements and attributes.
 * @author Bob Foster
 */
public class Atom implements IAtom {
	
	protected String fName;
	protected int fKind;

	protected Atom(int kind, String name) {
		fKind = kind;
		fName = name.intern();
	}
	
	public String getName() {
		return fName;
	}
	
	public String toString() {
		return fName;
	}
}
