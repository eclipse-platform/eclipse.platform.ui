package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.util.DocResources;
import org.xml.sax.Attributes;
/**
 * Context object, as defined in the map.xml
 */
public class ContextContribution implements IContext, IContextContributionNode {
	private String description;
	private ContextContributor contributor;
	protected List children = new ArrayList(/* of HelpContribution */
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
	 */
	public Iterator getChildren() {
		return children.iterator();
	}
	/**
	 */
	public String getShortId() {
		return shortID;
	}
	public String getDescription() {
		// description is already NL enabled when the XML files are parsed.
		return description;
	}
	public ContextContributor getContributor() {
		return contributor;
	}
	/** List of topic ids */
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
	public String getText() {
		return getDescription();
	}
		public void setDescription(String s) {
		description = s;
	}
	public void setContributor(ContextContributor contributor) {
		this.contributor = contributor;
	}
	public String getID(){
		return pluginID+"."+shortID;
	}
	/**
	 * Sets the pluginID.
	 * @param pluginID The pluginID to set
	 */
	public void setPluginID(String pluginID) {
		this.pluginID = pluginID;
	}

}
