package org.eclipse.help.internal.contributors;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.Iterator;
import org.eclipse.help.*;

/**
 * ContextManager maintains the list of contexts
 * and performs look-ups.
 */
public interface ContextManager {

	public static final String CONTEXT_EXTENSION = "org.eclipse.help.contexts";

	/**
	 * Finds the context, given context ID.
	 */
	public IContext getContext(String contextId);
	/**
	 * Finds the context description to display, given
	 * a an ordered list of (nested) context objects.
	 */
	public String getDescription(Object[] contexts);
	/**
	 * Finds the context related topics to display, given
	 * a an ordered list of (nested) context objects.
	 * Finds rest of the topics not returned by
	 * getRelatedTopics(Object[]).  May take long to execute.
	 */
	public IHelpTopic[] getMoreRelatedTopics(Object[] contexts);
	/**
	 * Finds the context related topics to display, given
	 * a an ordered list of (nested) context objects
	 * Finds only some of the topics.
	 */
	public IHelpTopic[] getRelatedTopics(Object[] contexts);
}
