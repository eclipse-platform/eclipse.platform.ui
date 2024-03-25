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
package org.eclipse.jface.text.information;

import org.eclipse.jface.text.IInformationControlCreator;

/**
 * Extends {@link org.eclipse.jface.text.information.IInformationProvider} with
 * the ability to provide its own information presenter control creator.
 *
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.information.IInformationProvider
 * @since 3.0
 */
public interface IInformationProviderExtension2 {

	/**
	 * Returns the information control creator of this information provider.
	 *
	 * @return the information control creator or <code>null</code> if none is available
	 */
	IInformationControlCreator getInformationPresenterControlCreator();
}
