/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.source;


/**
 * @deprecated use <code>AnnotationRulerColumn</code> instead.
 * @since 2.0
 */
public final class AnnotationColumn extends AnnotationRulerColumn {

	/**
	 * Creates a new <code>AnnotationColumn</code> of the given width.
	 * 
	 * @param width the width of this column
	 * @deprecated 
	 */
	public AnnotationColumn(int width) {
		super(width);
	}
}
