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

package org.eclipse.jface.text;

/**
 * Extension interface {@link org.eclipse.jface.text.IInformationControl}.
 * <p>
 * As it is the responsibility of the implementer of
 * {@link org.eclipse.jface.text.IInformationControl} and
 * {@link org.eclipse.jface.text.IInformationControlExtension2} to specify the
 * concrete nature of the information control's input, only the implementer can
 * know whether it has something to show or not.
 *
 * @since 2.0
 */
public interface IInformationControlExtension {

	/**
	 * Returns whether this information control has contents to be displayed.
	 *
	 * @return <code>true</code> if there is contents to be displayed.
	 */
	boolean hasContents();
}
