package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.help.*;
import org.xml.sax.Attributes;
/**
 * Context object, as defined in the map.xml
 */
public class ContextContribution
	implements IContext, IContextContributionNode {
	private String text;
	private ContextContributor contributor;
	protected List children = new ArrayList(/* of IContextContributionNode */
	);
	protected String pluginID;
	protected String shortID;
	/**
	 * Context constructor.
	 */
	public ContextContribution(Attributes attrs) {
		if (attrs == null)
			return;
		shortID = attrs.getValue("id");
	}
	/**
	 * Adds a child and returns it
	 * @returns com.ibm.itp.contributions.HelpContribution
	 * @param parentNode com.ibm.itp.contributions.HelpContribution
	 */
	public IContextContributionNode addChild(IContextContributionNode child) {
		children.add(children.size(), child);
		return child;
	}
	/**
	 * Obtains children
	 */
	public List getChildren() {
		return children;
	}
	/**
	 * Obtains short id (without plugin)
	 */
	public String getShortId() {
		return shortID;
	}
	public String getText() {
		return text;
	}
	public ContextContributor getContributor() {
		return contributor;
	}
	public IHelpResource[] getRelatedTopics() {
		if (children.size() > 0) {
			IHelpResource[] related = new IHelpResource[children.size()];
			children.toArray(related);
			return related;
		} else {
			// signal empty toc. handled by calling class.
			return null;
		}
	}
	public void setText(String s) {
		text = s;
	}
	public void setContributor(ContextContributor contributor) {
		this.contributor = contributor;
	}
	public String getID() {
		return pluginID + "." + shortID;
	}
	/**
	 * Sets the pluginID.
	 * @param pluginID The pluginID to set
	 */
	public void setPluginID(String pluginID) {
		this.pluginID = pluginID;
	}
	/**
	 * Merges another context contribution with this one
	 */
	public void merge(ContextContribution contribution) {
		if (contribution.getText() != null) {
			if (getText() != null) {
				setText(getText() + "\n" + contribution.getText());
			} else {
				setText(contribution.getText());
			}
		}
		children.addAll(contribution.getChildren());
	}
}