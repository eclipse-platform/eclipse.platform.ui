/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
/**
 * IContext Implementation that performs lazy lookup of
 * context ID and HelpContext instantiation.
 * when any of the IContext methods are called.
 */
public class ContextProxy implements IContext {
	private String contextID;
	private IContext context = null;
	private boolean used = false;
	/**
	 * ContextImpl constructor.
	 */
	public ContextProxy(String contextId) {
		super();
		this.contextID = contextId;
	}
	/**
	 * Returns a list of related topics for this help context.
	 * 
	 * @return a list of related help topics
	 */
	public IHelpResource[] getRelatedTopics() {
		if (!used) {
			context = HelpSystem.getContextManager().getContext(contextID);
			used = true;
		}
		if (context == null)
			return null;
		return context.getRelatedTopics();
	}
	/**
	 * Returns the text description for this context.
	 *
	 * @return the text description
	 */
	public String getText() {
		if (!used) {
			context = HelpSystem.getContextManager().getContext(contextID);
			used = true;
		}
		if (context == null)
			return null;
		return context.getText();
	}
	public String getID() {
		return contextID;
	}
}
