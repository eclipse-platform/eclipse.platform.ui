/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.text;

import org.eclipse.jface.util.Assert;

final public class AnnotationType {
	
	public final static AnnotationType UNKNOWN= new AnnotationType();
	public final static AnnotationType BOOKMARK= new AnnotationType();
	public final static AnnotationType TASK= new AnnotationType();
	public final static AnnotationType ERROR= new AnnotationType();
	public final static AnnotationType WARNING= new AnnotationType();
	public final static AnnotationType INFO= new AnnotationType();
	public final static AnnotationType SEARCH= new AnnotationType();
	
	private AnnotationType() {
	}

	public String toString() {
		if (this == UNKNOWN)
			return "AnnotationType.UNKNOWN"; //$NON-NLS-1$

		if (this == BOOKMARK)
			return "AnnotationType.BOOKMARK"; //$NON-NLS-1$

		if (this == TASK)
			return "AnnotationType.TASK"; //$NON-NLS-1$
		
		if (this == ERROR)
			return "AnnotationType.ERROR"; //$NON-NLS-1$

		if (this == WARNING)
			return "AnnotationType.WARNING"; //$NON-NLS-1$

		if (this == SEARCH)
			return "AnnotationType.SEARCH"; //$NON-NLS-1$

		Assert.isLegal(false);
		return ""; //$NON-NLS-1$
	}
}
