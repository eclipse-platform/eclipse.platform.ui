package org.eclipse.help;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.*;

/**
 * A help topic.
 * <p>
 * This interface models a help topic that can be defined in a help context object.
 * An array of help topics is typically associated with an IContext object, and would be 
 * displayed to the user when context sensitive help (F1) is requested.  
 * </p>
 * <p>
 * In the current implementation of the Help system, valid contexts can be 
 * contributed through the <code>contexts</code> element of the 
 * <code>"org.eclipse.help.contexts"</code> extension point. If there 
 * is a need to override this behavior, then this IHelpTopic interface, and the IContext
 * interface could be implemented by a client and registered with the SWT control or 
 * JFace action.  
 * </p> 
 */
public interface IHelpTopic {
	/**
	 * Returns the string URL associated with this help topic.
	 *
	 * @return the string URL of the topic
	 */
	public String getHref();
	/**
	 * Returns the label of this help topic.
	 *
	 * @return the label
	 */
	public String getLabel();
}
