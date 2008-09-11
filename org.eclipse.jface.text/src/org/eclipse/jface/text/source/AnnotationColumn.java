/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.source;


/**
 * @deprecated use
 *             {@link org.eclipse.jface.text.source.AnnotationRulerColumn#AnnotationRulerColumn(int)}
 *             instead.
 * @since 2.0
 */
public final class AnnotationColumn extends AnnotationRulerColumn {

	/**
	 * Creates a new <code>AnnotationColumn</code> of the given width.
	 *
	 * @param width the width of this column
	 * @deprecated Use
	 *             {@link org.eclipse.jface.text.source.AnnotationRulerColumn#AnnotationRulerColumn(int)}
	 *             instead
	 */
	public AnnotationColumn(int width) {
		super(width);
	}
}
