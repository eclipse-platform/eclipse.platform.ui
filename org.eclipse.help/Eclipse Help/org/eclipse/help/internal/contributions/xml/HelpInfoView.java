package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.xml.sax.*;

/**
 * View contribution implementation.
 */
public class HelpInfoView extends HelpContribution implements InfoView {
	private Hashtable topicMap = new Hashtable(/* of Topic*/);
	/**
	 * Constructor
	 */
	public HelpInfoView(Attributes attrs) {
		super(attrs);
	}
	/**
	 * @param visitor com.ibm.itp.ua.types.Visitor
	 */
	public void accept(Visitor visitor) {
		visitor.visit(this);
		/*
		// skip over the dummy root and visit its getChildren
		Topic dummyRoot = (Topic)getRoot();
		for (Enumeration e = dummyRoot.getChildren(); e.hasMoreElements(); )
		{
			Topic c = (Topic)e.nextElement();
			c.accept(visitor);
		}
		*/
	}
	/**
	* Adds a child and returns it
	* @returns com.ibm.itp.contributions.HelpContribution
	* @param parentNode com.ibm.itp.contributions.HelpContribution
	*/
	public Contribution addChild(Contribution child) {
		children.add(child);
		// detach from old parent, if any
		if (((HelpContribution) child).parent != null)
			 ((HelpContribution) child).parent.children.remove(child);
		// set the new parent
		 ((HelpContribution) child).parent = this;
		return child;
	}
	public Contribution getContribution(String id) {
		return (Contribution) topicMap.get(id);
	}
	public Topic getRoot() {
		return (Topic) children.get(0);
	}
	/**
	 * Registers the topic with the view.
	 * The insert actions are for a specific view, so every time
	 * a topic is created must be registered with its view
	 * 
	 * @param child com.ibm.itp.contributions.Contribution
	 */
	public void registerTopic(Contribution topic) {
		// If the topic already exists in the view then there are two cases:
		// - the topic was directly inserted under the view because it did not
		//   exist in the view. In this case we must first detach the topic from the
		//   view so it will be re-attached later
		// - or, this is caused by a second insertion of the same topic, in which case
		//   we must change its ID, assuming no further inserts are possible under it.
		if (topicMap.containsKey(topic.getID())) {
			// topic has been inserted more than once, so we need a new,
			// unique id for the new topic object.
			((HelpContribution) topic).setID(
				topic.getID() + "_" + SequenceGenerator.next());
		} else {
			// first time the topic has been added to the view, do nothing
		}
		// Keep track of this topic object
		topicMap.put(topic.getID(), topic);
	}
	/**
	 * @return java.lang.String
	 */
	public String toString() {
		return id;
	}
}
