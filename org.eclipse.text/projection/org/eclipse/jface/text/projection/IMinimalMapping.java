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
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Internal class. Do not use.
 * 
 * @since 3.0
 */
interface IMinimalMapping {

	/**
	 * @return
	 */
	IRegion getCoverage();

	/**
	 * @param region
	 * @return
	 */
	IRegion toOriginRegion(IRegion region) throws BadLocationException;

	/**
	 * @param offset
	 * @return
	 */
	int toOriginOffset(int offset) throws BadLocationException;
	
	/**
	 * @return
	 */
	IRegion[] toExactOriginRegions(IRegion region)throws BadLocationException;
	
	/**
	 * @return
	 */
	int getImageLength();
}
