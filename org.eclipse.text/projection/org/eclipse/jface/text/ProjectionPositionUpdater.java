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

package org.eclipse.jface.text;

import org.eclipse.jface.text.DefaultPositionUpdater;

/**
 * The position updater used to adapt the positions representing
 * the child document ranges to changes of the parent document.
 */
public class ProjectionPositionUpdater extends DefaultPositionUpdater {

	/**
	 * Creates the position updated.
	 */
	protected ProjectionPositionUpdater(String category) {
		super(category); 
	}
	
	/**
	 * Child document ranges cannot be deleted other then by calling
	 * freeChildDocument.
	 */
	protected boolean notDeleted() {
		return true;
	}
	
	/**
	 * If an insertion happens at a child document's start offset, the
	 * position is extended rather than shifted. Also, if something is added 
	 * right behind the end of the position, the position is extended rather
	 * than kept stable.
	 */
	protected void adaptToInsert() {
		
		int myStart= fPosition.offset;
		int myEnd=   fPosition.offset + fPosition.length;
		myEnd= Math.max(myStart, myEnd);
		
		int yoursStart= fOffset;
		int yoursEnd=   fOffset + fReplaceLength -1;
		yoursEnd= Math.max(yoursStart, yoursEnd);
		
		if (myEnd < yoursStart)
			return;
		
		if (myStart <= yoursStart)
			fPosition.length += fReplaceLength;
		else
			fPosition.offset += fReplaceLength;
	}
}
