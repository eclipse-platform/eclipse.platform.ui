/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


/**
 * A hyperlink detector tries to find a hyperlink at
 * a given location in a given text viewer.
 * <p>
 * In order to provide backward compatibility for clients of <code>IHyperlinkDetector</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link IHyperlinkDetectorExtension} since version 3.3,
 * 		adds the ability to dispose a hyperlink detector
 * </li>
 * <li>{@link IHyperlinkDetectorExtension2} since version 3.3,
 * 		adds the ability to specify the state mask of the modifier
 *      keys that need to be pressed for this hyperlink detector
 * </li>
 * </ul></p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
 * @since 3.1
 */
public interface IHyperlinkDetector {

	/**
	 * Tries to detect hyperlinks for the given region in
	 * the given text viewer and returns them.
	 * <p>
	 * In most of the cases only one hyperlink should be returned.
	 * </p>
	 * @param textViewer the text viewer on which the hover popup should be shown
	 * @param region the text range in the text viewer which is used to detect the hyperlinks
	 * @param canShowMultipleHyperlinks tells whether the caller is able to show multiple links
	 * 						to the user.
	 * 						If <code>true</code> {@link IHyperlink#open()} should directly open
	 * 							the link and not show any additional UI to select from a list.
	 * 						If <code>false</code> this method should only return one hyperlink
	 * 							which upon {@link IHyperlink#open()} may allow to select from a list.
	 * @return the hyperlinks or <code>null</code> if no hyperlink was detected
	 */
	IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks);

}
