package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;
/**
 * Context object, as defined in the map.xml
 */
public interface IContextContributionNode {
	/**
	 * Adds a child and returns it
	 * @returns com.ibm.itp.contributions.HelpContribution
	 * @param parentNode com.ibm.itp.contributions.HelpContribution
	 */
	public IContextContributionNode addChild(IContextContributionNode child);
	public Iterator getChildren();
}