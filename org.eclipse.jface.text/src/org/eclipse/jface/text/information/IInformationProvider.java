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
package org.eclipse.jface.text.information;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;


/**
 * Provides information related to the content of a text viewer.
 * <p>
 * In order to provide backward compatibility for clients of <code>IInformationProvider</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link IInformationProviderExtension} since version 2.1 introducing
 *		the ability to provide the element for a given subject</li>
 * <li>{@link IInformationProviderExtension2} since version 3.0 introducing
 *		the ability to provide its own information control creator</li>
 * </ul>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see org.eclipse.jface.text.information.IInformationProviderExtension
 * @see org.eclipse.jface.text.information.IInformationProviderExtension2
 * @see org.eclipse.jface.text.information.IInformationPresenter
 * @see org.eclipse.jface.text.ITextViewer
 * @since 2.0
 */
public interface IInformationProvider {

	/**
	 * Returns the region of the text viewer's document close to the given
	 * offset that contains a subject about which information can be provided.<p>
	 * For example, if information can be provided on a per code block basis,
	 * the offset should be used to find the enclosing code block and the source
	 * range of the block should be returned.
	 *
	 * @param textViewer the text viewer in which information has been requested
	 * @param offset the offset at which information has been requested
	 * @return the region of the text viewer's document containing the information subject
	 */
	IRegion getSubject(ITextViewer textViewer, int offset);

	/**
	 * Returns the information about the given subject or <code>null</code> if
	 * no information is available. It depends on the concrete configuration in which
	 * format the information is to be provided. For example, information presented
	 * in an information control displaying HTML, should be provided in HTML.
	 *
	 * @param textViewer the viewer in whose document the subject is contained
	 * @param subject the text region constituting the information subject
	 * @return the information about the subject
	 * @see IInformationPresenter
	 * @deprecated As of 2.1, replaced by {@link IInformationProviderExtension#getInformation2(ITextViewer, IRegion)}
	 */
	String getInformation(ITextViewer textViewer, IRegion subject);
}
