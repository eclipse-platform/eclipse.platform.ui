/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * ICharacterPairMatcher.java
 */
public interface ICharacterPairMatcher {

	int RIGHT= 0;
	int LEFT= 1;

	/**
	 * Method dispose.
	 */
	void dispose();

	/**
	 * Method match.
	 * @param iDocument
	 * @param i
	 * @return IRegion
	 */
	IRegion match(IDocument iDocument, int i);

	/**
	 * Method getAnchor.
	 * @return int
	 */
	int getAnchor();
}
