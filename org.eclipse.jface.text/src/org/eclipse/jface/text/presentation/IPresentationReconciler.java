/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.presentation;


import org.eclipse.jface.text.ITextViewer;


/**
 * An <code>IPresentationReconciler</code> defines and maintains the representation of a 
 * text viewer's document in the presence of changes applied to the document. 
 * An <code>IPresentationReconciler</code> is a <code>ITextViewer</code> add-on.<p>
 * The presentation reconciler keeps track of changes applied to the text viewer. It sends 
 * each change to presentation damagers which are registered for the content types of the
 * regions in which the change occurred. The presentation reconciler passes the computed
 * damage to presentation repairers which construct text presentations. Those text presentation
 * when applied to the presentation reconciler's text viewer bring the document's presentation 
 * in sync with the document's content and thus repair the  damage. A presentation damager 
 * is expected to return damage which is a valid input for a presentation repairer registered 
 * for the same content type as the damager.<p>
 * A presentation reconciler should always be configured with damager/repairer pairs. I.e. 
 * for each damager there should be a corresponding repairer.<p>
 * The interface can be implemented by clients. By default, clients use
 * <code>PresentationReconciler</code> as the standard implementer of this interface. 
 *
 * @see ITextViewer
 * @see IPresentationDamager
 * @see IPresentationRepairer
 * @see org.eclipse.jface.text.TextPresentation
 */
public interface IPresentationReconciler {
			
	/**
	 * Installs this presentation reconciler on the given text viewer. After 
	 * this method has been finished, the reconciler is operational. I.e., it 
	 * works without requesting further client actions until <code>uninstall</code> 
	 * is called.
	 * 
	 * @param viewer the viewer on which this presentation reconciler is installed
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
	 *		<code>null</code> if there is no such strategy
	 */
	IPresentationDamager getDamager(String contentType);
	
	/**
	 * Returns the presentation repairer registered with this presentation reconciler
	 * for the specified content type.
	 *
	 * @param contentType the content type for which to determine the repairer
	 * @return the presentation repairer registered for the given content type, or
	 *		<code>null</code> if there is no such strategy
	 */
	IPresentationRepairer getRepairer(String contentType);
}
