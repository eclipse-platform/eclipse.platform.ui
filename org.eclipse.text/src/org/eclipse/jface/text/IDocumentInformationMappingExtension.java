/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * Extension to {@link org.eclipse.jface.text.IDocumentInformationMapping}.
 * <p>
 * Extends the information available in the mapping by providing explicit access
 * to the isomorphic portion of the basically homomorphic information mapping.
 *
 * @see org.eclipse.jface.text.IDocumentInformationMapping
 * @since 3.0
 */
public interface IDocumentInformationMappingExtension {

	/**
	 * Adheres to
	 * <code>originRegion=toOriginRegion(toExactImageRegion(originRegion))</code>,
	 * if <code>toExactImageRegion(originRegion) != null</code>. Returns
	 * <code>null</code> if there is no image for the given origin region.
	 *
	 * @param originRegion the origin region
	 * @return the exact image region or <code>null</code>
	 * @throws BadLocationException if origin region is not a valid region in
	 *             the origin document
	 */
	IRegion toExactImageRegion(IRegion originRegion) throws BadLocationException;

	/**
	 * Returns the segments of the image document that exactly correspond to the
	 * given region of the original document. Returns <code>null</code> if
	 * there are no such image regions.
	 *
	 * @param originRegion the region in the origin document
	 * @return the segments in the image document or <code>null</code>
	 * @throws BadLocationException in case the given origin region is not valid
	 *             in the original document
	 */
	IRegion[] toExactImageRegions(IRegion originRegion) throws BadLocationException;

	/**
	 * Returns the fragments of the original document that exactly correspond to
	 * the given region of the image document.
	 *
	 * @param imageRegion the region in the image document
	 * @return the fragments in the origin document
	 * @throws BadLocationException in case the given image region is not valid
	 *             in the image document
	 */
	IRegion[] toExactOriginRegions(IRegion imageRegion) throws BadLocationException;

	/**
	 * Returns the length of the image document.
	 *
	 * @return the length of the image document
	 */
	int getImageLength();

	/**
	 * Returns the maximal sub-regions of the given origin region which are
	 * completely covered. I.e. each offset in a sub-region has a corresponding
	 * image offset. Returns <code>null</code> if there are no such
	 * sub-regions.
	 *
	 * @param originRegion the region in the origin document
	 * @return the sub-regions with complete coverage or <code>null</code>
	 * @throws BadLocationException in case the given origin region is not valid
	 *             in the original document
	 */
	IRegion[] getExactCoverage(IRegion originRegion) throws BadLocationException;
}
