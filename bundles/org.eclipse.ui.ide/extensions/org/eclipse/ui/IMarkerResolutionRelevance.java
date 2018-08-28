/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
