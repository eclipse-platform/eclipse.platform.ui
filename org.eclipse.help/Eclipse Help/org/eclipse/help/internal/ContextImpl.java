package org.eclipse.help.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.help.*;

/**
 * IContext Implementation that performs lazy lookup of
 * context ID and HelpContext instantiation.
 * when any of the IContext methods are called.
 */
public class ContextImpl implements IContext {
	private String contextID;
	private IContext context = null;
	private boolean used = false;
	/**
	 * ContextImpl constructor.
	 */
	public ContextImpl(String contextId) {
		super();
		this.contextID = contextId;
	}
	/**
	 * Returns a list of related topics for this help context.
	 * 
	 * @return a list of related help topics
	 */
	public IHelpTopic[] getRelatedTopics() {
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
}
