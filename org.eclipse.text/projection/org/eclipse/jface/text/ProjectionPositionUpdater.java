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
 * The position updater used to adapt the positions corresponding to the fragments of 
 * projection documents. The startegy of this updater differs from the 
 * <code>DefaultPositionUpdater</code>. If an insertion happens at a start offset of a
 * range corresponding to a fragment, the range is extended rather than  shifted. Also, if 
 * text is added  right behind the end of such a range, the range is extended rather than
 * kept stable.<p>
 * This class is for internal use only.
 * 
 * @since 2.1
 */
public class ProjectionPositionUpdater extends DefaultPositionUpdater {

	/**
	 * Creates the position updater for the given category.
	 * 
	 * @param category the category used to manage the positions representing the ranges corresponding to fragments
	 */
	protected ProjectionPositionUpdater(String category) {
		super(category); 
	}
	
	/**
	 * Projection document ranges cannot be deleted other then by calling
	 * <code>ProjectionDocumentManager#removeFragment</code>.
	 * @return <code>true</code>
	 */
	protected boolean notDeleted() {
		return true;
	}
	
	/**
	 * If an insertion happens at a start offset of a
	 * range corresponding to a fragment, the range is extended rather than  shifted. Also, if 
	 * text is added  right behind the end of such a range, the range is extended rather than
	 * kept stable.
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
