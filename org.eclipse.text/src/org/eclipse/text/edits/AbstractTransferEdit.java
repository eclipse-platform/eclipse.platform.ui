/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import java.util.Iterator;
import java.util.List;

/**
 * Abstract superclass for all transfer edits. Transfer edits are edits which copy or 
 * move a text region inside a document to another location.
 * <p>
 * This class isn't intended to be subclassed outside of the edit framework. Clients 
 * are only allowed to subclass <code>MultiTextEdit</code>.
 * 
 * @since 3.0
 */
public abstract class AbstractTransferEdit extends TextEdit {

	/* package */ int fMode;
	/* package */ final static int UNDEFINED= 0;
	/* package */ final static int INSERT= 1;
	/* package */ final static int DELETE= 2;

	/* package */ AbstractTransferEdit(int offset, int length) {
		super(offset, length);
	}
	
	/*
	 * Copy constructor
	 */
	/* package */ AbstractTransferEdit(AbstractTransferEdit other) {
		super(other);
		
	}

	/* package */ static void move(List children, int delta) {
		if (children != null) {
			for (Iterator iter= children.iterator(); iter.hasNext();) {
				TextEdit edit= (TextEdit)iter.next();
				edit.adjustOffset(delta);
				move(edit.internalGetChildren(), delta);
			}
		}
	}	
}
