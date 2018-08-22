/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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


/**
 * @deprecated use
 *             {@link org.eclipse.jface.text.source.AnnotationRulerColumn#AnnotationRulerColumn(int)}
 *             instead.
 * @since 2.0
 */
@Deprecated
public final class AnnotationColumn extends AnnotationRulerColumn {

	/**
	 * Creates a new <code>AnnotationColumn</code> of the given width.
	 *
	 * @param width the width of this column
	 * @deprecated Use
	 *             {@link org.eclipse.jface.text.source.AnnotationRulerColumn#AnnotationRulerColumn(int)}
	 *             instead
	 */
	@Deprecated
	public AnnotationColumn(int width) {
		super(width);
	}
}
