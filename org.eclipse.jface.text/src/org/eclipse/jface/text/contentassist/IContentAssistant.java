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
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;
 
 
/** 
 * An <code>IContentAssistant</code> provides support on interactive content completion.
 * The content assistant is a <code>ITextViewer</code> add-on. Its
 * purpose is to propose, display, and insert completions of the content
 * of the text viewer's document at the viewer's cursor position. In addition
 * to handle completions, a content assistant can also be requested to provide
 * context information. Context information is shown in a tool tip like popup.
 * As it is not always possible to determine the exact context at a given
 * document offset, a content assistant displays the possible contexts and requests
 * the user to choose the one whose information should be displayed.<p>
 * A content assistant has a list of  <code>IContentAssistProcessor</code>
 * objects each of which is registered for a  particular document content
 * type. The content assistant uses the processors to react on the request 
 * of completing documents or presenting context information.<p>
 * The interface can be implemented by clients. By default, clients use
 * <code>ContentAssistant</code> as the standard implementer of this interface. 
 *
 * @see ITextViewer
 * @see IContentAssistProcessor 
 */
 public interface IContentAssistant {
	
	//------ proposal popup orientation styles ------------
	/** The context info list will overlay the list of completion proposals. */
	public final static int PROPOSAL_OVERLAY= 10;
	/** The completion proposal list will be removed before the context info list will be shown. */
	public final static int PROPOSAL_REMOVE=  11;
	/** The context info list will be presented without hiding or overlapping the completion proposal list. */
	public final static int PROPOSAL_STACKED= 12;
	
	//------ context info box orientation styles ----------
	/** Context info will be shown above the location it has been requested for without hiding the location. */
	public final static int CONTEXT_INFO_ABOVE= 20;
	/** Context info will be shown below the location it has been requested for without hiding the location. */
	public final static int CONTEXT_INFO_BELOW= 21;
	
	
	/**
	 * Installs content assist support on the given text viewer.
	 *
	 * @param textViewer the text viewer on which content assist will work
	 */
	void install(ITextViewer textViewer);
	
	/**
	 * Uninstalls content assist support from the text viewer it has 
	 * previously be installed on.
	 */
	void uninstall();
	
	/**
	 * Shows all possible completions of the content at the viewer's cursor position.
	 *
	 * @return an optional error message if no proposals can be computed
	 */
	String showPossibleCompletions();
	
	/**
	 * Shows context information for the content at the viewer's cursor position.
	 *
	 * @return an optional error message if no context information can be computed
	 */
	String showContextInformation();	
	
	/**
	 * Returns the content assist processor to be used for the given content type.
	 *
	 * @param contentType the type of the content for which this
	 *        content assistant is to be requested
	 * @return an instance content assist processor or
	 *         <code>null</code> if none exists for the specified content type
	 */
	IContentAssistProcessor getContentAssistProcessor(String contentType);
}
