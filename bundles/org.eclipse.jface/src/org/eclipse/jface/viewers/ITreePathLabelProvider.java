/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * An extension to {@link ILabelProvider} that is given the
 * path of the element being decorated, when it is available.
 * @since 3.2
 */
public interface ITreePathLabelProvider extends IBaseLabelProvider {

	/**
	 * Updates the label for the given element.
	 *
	 * @param label the label to update
	 * @param elementPath the path of the element being decorated
	 */
	public void updateLabel(ViewerLabel label, TreePath elementPath);
}
