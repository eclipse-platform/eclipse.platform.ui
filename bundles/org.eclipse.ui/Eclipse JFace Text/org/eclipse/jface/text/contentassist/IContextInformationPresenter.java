package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;


/**
 * A context information presenter determines the presentation
 * of context information depending on a given document position.
 * The interface can be implemented by clients.
 */
public interface IContextInformationPresenter {

	/**
	 * Installs this presenter for the given context information.
	 *
	 * @param info the context information which this presenter should style
	 * @param viewer the text viewer on which the information is presented
	 * @param documentPosition the document position for which the information has been computed
	 */
	void install(IContextInformation info, ITextViewer viewer, int documentPosition);
	
	/**
	 * Updates the given presentation of the given context information 
	 * at the given document position. Returns whether update changed the
	 * presentation.
	 *
	 * @param information the context information to be styled
	 * @param documentPosition the current position within the document
	 * @param presentation the presentation to be updated
	 * @return <code>true</code> if the given presentation has been changed
	 */
	boolean updatePresentation(int documentPosition, TextPresentation presentation);
}