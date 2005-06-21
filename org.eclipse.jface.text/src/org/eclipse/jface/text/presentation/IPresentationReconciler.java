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
package org.eclipse.jface.text.presentation;


import org.eclipse.jface.text.ITextViewer;


/**
 * An <code>IPresentationReconciler</code> defines and maintains the
 * representation of a text viewer's document in the presence of changes applied
 * to the document. An <code>IPresentationReconciler</code> is a
 * <code>ITextViewer</code> add-on.
 * <p>
 * The presentation reconciler keeps track of changes applied to the text
 * viewer. It sends each change to presentation damagers which are registered
 * for the content types of the regions in which the change occurred. The
 * presentation reconciler passes the computed damage to presentation repairer
 * which construct text presentations. When applied to the presentation
 * reconciler's text viewer, those text presentations bring the document's
 * presentation in sync with the document's content and thus repair the damage.
 * A presentation damager is expected to return damage which is a valid input
 * for a presentation repairer registered for the same content type as the
 * damager.
 * </p>
 * <p>
 * A presentation reconciler should always be configured with a pair of
 * damager/repairer strategies. I.e. for each damager there should be a
 * corresponding repairer.
 * </p>
 * <p>
 * The interface may be implemented by clients. Clients may use
 * <code>PresentationReconciler</code> as the standard implementation of this
 * interface.
 * </p>
 * <p>
 * In order to provided backward compatibility for clients of
 * <code>IPresentationReconciler</code>, extension interfaces are used to
 * provide a means of evolution. The following extension interface exists:
 * <ul>
 * <li>
 * {@link org.eclipse.jface.text.presentation.IPresentationReconcilerExtension}
 * since version 3.0 adding support for documents with multiple partitionings.
 * </li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jface.text.presentation.IPresentationReconcilerExtension
 * @see org.eclipse.jface.text.ITextViewer
 * @see org.eclipse.jface.text.presentation.IPresentationDamager
 * @see org.eclipse.jface.text.presentation.IPresentationRepairer
 * @see org.eclipse.jface.text.TextPresentation
 */
public interface IPresentationReconciler {

	/**
	 * Installs this presentation reconciler on the given text viewer. After
	 * this method has been finished, the reconciler is operational. I.e., it
	 * works without requesting further client actions until
	 * <code>uninstall</code> is called.
	 * <p>
	 * The <code>install</code> and <code>uninstall</code> methods must be
	 * called in sequence; i.e. repeatedly calling <code>install</code>
	 * without calling <code>uninstall</code> may throw an exception.
	 * </p>
	 *
	 * @param viewer the viewer on which this presentation reconciler is
	 *        installed
	 */
	void install(ITextViewer viewer);

	/**
	 * Removes the reconciler from the text viewer it has previously been
	 * installed on.
	 */
	void uninstall();

	/**
	 * Returns the presentation damager registered with this presentation reconciler
	 * for the specified content type.
	 *
	 * @param contentType the content type for which to determine the damager
	 * @return the presentation damager registered for the given content type, or
	 *		<code>null</code> if there is no damager
	 */
	IPresentationDamager getDamager(String contentType);

	/**
	 * Returns the presentation repairer registered with this presentation reconciler
	 * for the specified content type.
	 *
	 * @param contentType the content type for which to determine the repairer
	 * @return the presentation repairer registered for the given content type, or
	 *		<code>null</code> if there is no repairer
	 */
	IPresentationRepairer getRepairer(String contentType);
}
