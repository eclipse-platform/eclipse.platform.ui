package org.eclipse.jface.text.contentassist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


import org.eclipse.jface.text.ITextViewer;


/**
 * A context information validator is used to determine if
 * a displayed context information is still valid or should
 * be dismissed. The interface can be implemented by clients. 
 * Clients may use <code>ContextInformationValidator</code> 
 * as their implementer of this interface. 
 */
public interface IContextInformationValidator {

	/**
	 * Installs this validator for the given context information.
	 *
	 * @param info the context information which this validator should check
	 * @param viewer the text viewer on which the information is presented
	 * @param documentPosition the document position for which the information has been computed
	 */
	void install(IContextInformation info, ITextViewer viewer, int documentPosition);
	/**
	 * Returns whether the information this validator is installed on is still valid
	 * at the given document position.
	 *
	 * @param documentPosition the current position within the document
	 * @return <code>true</code> if the information also valid at the given document position
	 */
	boolean isContextInformationValid(int documentPosition);
}
