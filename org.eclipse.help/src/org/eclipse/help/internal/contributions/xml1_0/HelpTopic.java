package org.eclipse.help.internal.contributions.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.help.internal.contributors1_0.TopicContributor;
import org.eclipse.help.internal.contributions1_0.Topic;
import org.eclipse.help.internal.contributions1_0.Visitor;
import org.xml.sax.*;
import org.eclipse.help.IHelpTopic;

/**
 * Default implementation for a topic contribution
 */
public class HelpTopic extends HelpContribution implements Topic, IHelpTopic {

	protected String href;

	/**
	 * HelpTopic constructor comment.
	 */
	public HelpTopic(Attributes attrs) {
		super(attrs);
		if (attrs != null)
			href = attrs.getValue(TopicContributor.TOPIC_HREF_ATTR);
	}
	/**
	 * Implements the method for the Visitor pattern
	 * @param visitor com.ibm.itp.contributions.Visitor
	 */
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
}
