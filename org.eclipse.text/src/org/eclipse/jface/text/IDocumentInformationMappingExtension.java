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

/**
 * Extension to <code>IDocumentInformationMapping</code>.
 * 
 * @since 3.0
 */
public interface IDocumentInformationMappingExtension {
	
	/**
	 * Adheres to originRegion= toOriginRegion(toExactImageRegion(originRegion)), if toExactImageRegion(originRegion) != null.
	 * 
	 * @param originRegion the origin region
	 * @return the exact image region
	 * @throws BadLocationException if origin region is not valid a valid region in the master document
	 */
	IRegion toExactImageRegion(IRegion originRegion) throws BadLocationException;
	
	
	/**
	 * Returns the fragments of the original document that exactly correspond
	 * to the given region of the image document.
	 * 
	 * @param imageRegion the region in the image document
	 * @return the fragments of the original document
	 * @throws BadLocationException in case the given image region is not valid in the image document
	 */
	IRegion[] toExactOriginRegions(IRegion imageRegion) throws BadLocationException;
	
	/**
	 * Returns the length of the image document.
	 * 
	 * @return the length of the image document
	 */
	int getImageLength();
}
