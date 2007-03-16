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
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.widgets.Composite;
/**
 * Classes that extend abstract class Layout and implement this interface can
 * take part in layout computation of the TableWrapLayout manager. This layout
 * uses alternative algorithm that computes columns before rows. It allows it
 * to 'flow' wrapped text proportionally (similar to the way web browser
 * renders tables). Custom layout managers that implement this interface will
 * allow TableWrapLayout to properly compute width hint to pass.
 * 
 * @see TableWrapLayout
 * @see ColumnLayout
 * @since 3.0
 */
public interface ILayoutExtension {
	/**
	 * Computes the minimum width of the parent. All widgets capable of word
	 * wrapping should return the width of the longest word that cannot be
	 * broken any further.
	 * 
	 * @param parent the parent composite
	 * @param changed <code>true</code> if the cached information should be
	 * flushed, <code>false</code> otherwise.
	 * @return the minimum width of the parent composite
	 */
	public int computeMinimumWidth(Composite parent, boolean changed);
	/**
	 * Computes the maximum width of the parent. All widgets capable of word
	 * wrapping should return the length of the entire text with wrapping
	 * turned off.
	 * 
	 * @param parent the parent composite
	 * @param changed <code>true</code> if the cached information
	 * should be flushed, <code>false</code> otherwise.
	 * @return the maximum width of the parent composite
	 */
	public int computeMaximumWidth(Composite parent, boolean changed);
}
