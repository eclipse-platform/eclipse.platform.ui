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
package org.eclipse.jface.text.information;


import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;


/**
 * Extends {@link org.eclipse.jface.text.information.IInformationProvider} with
 * the ability to provide the element for a given subject.
 *
 * @see org.eclipse.jface.text.information.IInformationProvider
 * @since 2.1
 */
public interface IInformationProviderExtension {

	/**
	 * Returns the element for the given subject or <code>null</code> if
	 * no element is available.
	 * <p>
	 * Implementers should ignore the text returned by {@link IInformationProvider#getInformation(ITextViewer, IRegion)}.
	 * </p>
	 *
	 * @param textViewer the viewer in whose document the subject is contained
	 * @param subject the text region constituting the information subject
	 * @return the element for the subject
	 *
	 * @see IInformationProvider#getInformation(ITextViewer, IRegion)
	 * @see org.eclipse.jface.text.ITextViewer
	 */
	Object getInformation2(ITextViewer textViewer, IRegion subject);
}
