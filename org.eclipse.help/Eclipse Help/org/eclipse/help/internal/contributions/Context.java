package org.eclipse.help.internal.contributions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.help.*;

/**
 * Context contribution
 */
public interface Context extends Contribution {
	/** Returns a specific nested context */
	public Context getContext(String id);
	/** Returns the text description for this context */
	public String getDescription();
	/** Returns the related links */
	public IHelpTopic[] getRelatedTopics();
}
