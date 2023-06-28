/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * This interface represents a textual selection that can be made of multiple discontinued selected
 * ranges.
 *
 * @since 3.19
 */
public interface IMultiTextSelection extends ITextSelection {

	/**
	 * Returns a non-empty array containing the selected text range for each line covered by the
	 * selection.
	 *
	 * @return an array containing a the covered text range for each line covered by the receiver
	 */
	IRegion[] getRegions();

}
