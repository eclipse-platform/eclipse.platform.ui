package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.IHelpTopic;
import org.eclipse.help.internal.contributions1_0.Contribution;
/**
 * Context contribution
 */
public interface IContextContribution extends Contribution {
	/** Returns a specific nested context */
	public IContextContribution getContext(String id);
	/** Returns the text description for this context */
	public String getDescription();
	/** Returns the related links */
	public IHelpTopic[] getRelatedTopics();
}