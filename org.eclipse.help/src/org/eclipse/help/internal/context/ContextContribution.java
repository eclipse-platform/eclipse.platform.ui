package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;
import org.eclipse.help.*;
import org.eclipse.help.internal.contributions.xml1_0.HelpContribution;
import org.eclipse.help.internal.contributions1_0.Contribution;
import org.eclipse.help.internal.contributors1_0.Contributor;
import org.xml.sax.Attributes;
/**
 * Context object, as defined in the map.xml
 */
public class ContextContribution
	extends HelpContribution
	implements IContextContribution, IContext {
	private String description;
	private Contributor contributor;
	/**
	 * Context constructor comment.
	 */
	public ContextContribution(Attributes attrs) {
		super(attrs);
		///merge(contextNode);
	}
	/**
	 * Overriden to handle <description>
	 */
	public Contribution addChild(Contribution child) {
		return super.addChild(child);
	}
	public IContextContribution getContext(String id) {
		for (Iterator it = children.iterator(); it.hasNext();) {
			IContextContribution c = (IContextContribution) it.next();
			if (c.getID().equals(id))
				return c;
		}
		return null;
	}
	public String getDescription() {
		// description is already NL enabled when the XML files are parsed.
		return description;
	}
	public Contributor getContributor() {
		return contributor;
	}
	/** List of topic ids */
	public IHelpTopic[] getRelatedTopics() {
		if (children.size() > 0) {
			IHelpTopic[] related = new IHelpTopic[children.size()];
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
	public void setContributor(Contributor contributor) {
		this.contributor = contributor;
	}
}