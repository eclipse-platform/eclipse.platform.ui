/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;
/**
 * A context registered for context-sensitive help.
 * <p>
 * This interface models the context-sensitive help that can be associated with
 * SWT menus, menu items, and controls, and with JFace actions. A help context 
 * provides the text description of the object with which it is associated with, 
 * as well as topic links that contain more related information. This information
 * would be displayed to the user when context sensitive help (F1) is requested. 
 * </p>
 * <p>
 * In the current implementation of the Help system, valid contexts can be 
 * contributed through the <code>contexts</code> element of the 
 * <code>"org.eclipse.help.contexts"</code> extension point. The 
 * <code>IHelp.findContext(String)</code> method is used at runtime to create 
 * or fetch IContext objects using there fully qualified contextIds. If there 
 * is a need to override this behavior, then this IContext interface could be 
 * implemented by a client and registered with the SWT control or JFace action.  
 * </p>
 */
public interface IContext {
	/**
	 * Returns a list of related topics for this help context.
	 * 
	 * @return a list of related help topics
	 * @since 2.0
	 */
	public IHelpResource[] getRelatedTopics();
	/**
	 * Returns the text description for this context.
	 *
	 * @return the text description
	 */
	public String getText();
}