package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.xml.sax.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Context object, as defined in the map.xml
 */
public class HelpContext
	extends HelpContribution
	implements Context, IContext {
	private String description;
	/**
	 * Context constructor comment.
	 */
	public HelpContext(Attributes attrs) {
		super(attrs);
		///merge(contextNode);
	}
	/**
	 * Overriden to handle <description>
	 */
	public Contribution addChild(Contribution child) {
		/*
		if (child instanceof Topic)
			return super.addChild(child);
		else
			description = "some description. Change the <description> specs and parsing.";
		return null;
		*/
		return super.addChild(child);
	}
	public Context getContext(String id) {
		for (Iterator it = children.iterator(); it.hasNext();) {
			Context c = (Context) it.next();
			if (c.getID().equals(id))
				return c;
		}
		return null;
	}
	public String getDescription() {
		// description is already NL enabled when the XML files are parsed.
		return description;
	}
	/** List of topic ids */
	public IHelpTopic[] getRelatedTopics() {
		if (children.size() > 0) {
			IHelpTopic[] related = new IHelpTopic[children.size()];
			children.toArray(related);
			return related;
		} else {
			// signal empty topics. handled by calling class.
			return null;
		}
	}
	public String getText() {
		return getDescription();
	}
	/**
	 * Merges the current context with info from an XML
	 * definition of the context.
	 * Contexts are nested and maintained as trees;
	 */
	void merge(HelpContext context) {
		/*
		for (Iterator it=context.getChildren(); it.hasNext(); )
		{
			HelpContext child = (Context)it.next();
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element childElement = (Element)child;
		
			if (childElement.getTagName().equals(ContextContributor.DESC_ELEM))
			{
				Node text = childElement.getFirstChild();
				if (text != null)
					description = text.getNodeValue();
		
				continue;
			}
		
			if (childElement.getTagName().equals(ContextContributor.RELATED_ELEM))
			{
				if (relatedTopics == null) relatedTopics = new Vector();
				relatedTopics.add(childElement.getAttribute(ContextContributor.RELATED_ATTR));
				continue;
			}
		
			if (!childElement.getTagName().equals(ContextContributor.CONTEXT_ELEM))
			{
				System.out.println("error: unexpected " + childElement.getTagName());
				continue;
			}
				
			String id = childElement.getAttribute(ContextContributor.ID_ATTR);
			HelpContext oldChildContext = (HelpContext)getContext(id);
			if (oldChildContext == null)
			{
				Context newChildContext = new HelpContext(childElement);
				addChild(newChildContext);
			}
			else
			{
				oldChildContext.merge(childElement);
			}
		}
		*/
	}
	public void setDescription(String s) {
		description = s;
	}
}
