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
package org.eclipse.ui.forms.parts;

import org.eclipse.swt.widgets.Composite;

/**
 * Classes that extend abstract class Layout and implement
 * this interface can take part in layout computation of
 * the HTMLTableLayout manager. The said layout uses
 * alternative algorithm that computes columns before rows.
 * It allows it to 'flow' wrapped text proportionally
 * (similar to web browser layout engines). Custom layout 
 * managers that implement this interface allow recursive 
 * reflow to be performed.
 */
public interface ILayoutExtension {
/**
 * Computes the minimum width of the parent. All widgets
 * capable of word wrapping should return the width
 * of the longest word that cannot be wrapped.
 * @param parent
 * @param changed
 * @return
 */
	public int computeMinimumWidth(Composite parent, boolean changed);
/**
 * Computes the maximum width of the parent. All widgets
 * capable of word wrapping should return the length 
 * of the entire text without wrapping.
 * @param parent
 * @param changed
 * @return
 */
	public int computeMaximumWidth(Composite parent, boolean changed); 
}
