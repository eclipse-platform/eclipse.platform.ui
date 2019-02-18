/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jface.text.source;

import java.util.Iterator;


/**
 * Extends {@link org.eclipse.jface.text.source.IAnnotationModel} with the
 * ability to retrieve a set of annotations within a given region.
 *
 * @since 3.4
 */
public interface IAnnotationModelExtension2 {

	/**
	 * Returns an iterator over all annotations managed by this model that are
	 * inside the given region.
	 *
	 * @param offset the start position of the region, must be &gt;= 0
	 * @param length the length of the region, must be &gt;= 0
	 * @param canStartBefore if <code>true</code> then annotations are included
	 *            which start before the region if they end at or after the region's start
	 * @param canEndAfter if <code>true</code> then annotations are included
	 *            which end after the region if they start at or before the region's end
	 * @return all annotations inside the region managed by this model
	 */
	Iterator<Annotation> getAnnotationIterator(int offset, int length, boolean canStartBefore, boolean canEndAfter);
}
