/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * @param offset the start position of the region, must be >= 0
	 * @param length the length of the region, must be >= 0
	 * @param canStartBefore if <code>true</code> then annotations are included
	 *            which start before the region if they end at or after the region's start
	 * @param canEndAfter if <code>true</code> then annotations are included
	 *            which end after the region if they start at or before the region's end
	 * @return all annotations inside the region managed by this model (element type: {@link Annotation})
	 */
	Iterator getAnnotationIterator(int offset, int length, boolean canStartBefore, boolean canEndAfter);
}
