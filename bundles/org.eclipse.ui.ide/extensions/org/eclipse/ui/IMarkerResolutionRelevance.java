/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * A relevance for the marker resolution. This interface gives the relevance of
 * the marker which can be used to sort all the marker resolutions. Resolutions
 * with higher relevance should be listed before resolutions with lower
 * relevance.
 * 
 * @since 3.14
 */
public interface IMarkerResolutionRelevance {

	/**
	 * Returns the relevance of this marker resolution.
	 * <p>
	 * The relevance is used to determine if this resolution is more relevant than
	 * another resolution.
	 * </p>
	 *
	 * @return the relevance of this resolution
	 */
	default public int getRelevanceForResolution() {
		return 0;
	}
}
