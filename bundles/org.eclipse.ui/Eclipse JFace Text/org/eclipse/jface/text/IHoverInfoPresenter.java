package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A hover info presenter determines the presentation of information
 * displayed in a hover window.
 * The interface can be implemented by clients.
 */
public interface IHoverInfoPresenter {
	
	/**
	 * Updates the given presentation of the given hover information.
	 * Returns whether this method changed the presentation.
	 *
	 * @param hoverInfo the information to be styled
	 * @param presentation the presentation to be updated
	 * @return <code>true</code> if the given presentation has been changed
	 */
	boolean updatePresentation(String hoverInfo, TextPresentation presentation);
}

