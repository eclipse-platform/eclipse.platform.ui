package org.eclipse.help.internal.remote;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;

import org.eclipse.help.internal.HelpSystem;

/**
 * This class is an implementation of the pluggable help support.
 * It does not use the support extension point, as it is hard-coded
 * so it works in the same install image to the real help plugin.
 */
public class Help implements IHelp {
	/**
	 * BaseHelpViewer constructor comment.
	 */
	public Help() {
		super();
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param url java.lang.Object[]
	 */
	public void displayHelp(Object[] contextIds, int x, int y) {
		// NOT CURRENTLY SUPPORTED
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param trigger java.lang.Object object triggering help
	 * @param contextIds java.lang.String[]
	 */
	public void displayHelp(String[] contexts, int x, int y) {

		// NOT CURRENTLY SUPPORTED  
	}
	/**
	 * Displays context-sensitive help for specified contexts
	 * @param trigger java.lang.Object object triggering help
	 * @param contextIds java.lang.String[]
	 */
	public void displayHelp(IContext[] contexts, int x, int y) {

		// NOT CURRENTLY SUPPORTED  
	}
	/**
		 * Display help.
		 */
	public void displayHelp(String url) {
		// NOT CURRENTLY SUPPORTED
	}
	/**
	 * Computes context information for a given context ID.
	 * @param contextID java.lang.String ID of the context
	 * @return IContext
	 */
	public IContext findContext(String contextID) {
		return HelpSystem.getContextManager().getContext(contextID);
	}
}
