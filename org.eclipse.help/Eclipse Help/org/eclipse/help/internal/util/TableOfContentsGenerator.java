package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions.*;

/**
 * This class is resposible for generating a Table of Contents for
 * a given Topic hierarchy.
 */
public class TableOfContentsGenerator {

	// flag needed when doing recursion in findTopic()
	private static boolean foundTopic = false;
	public TableOfContentsGenerator() {
		super();
	}
	/**
	 * Recursively finds of a Topic in a list of Topics with the given topicId
	 */
	private Topic findTopic(List children, String topicId) {
		Topic topic = null;
		for (int i = 0; i < children.size() && !foundTopic; i++) {
			topic = ((Topic) children.get(i));
			if (topic.getID().equals(topicId)) {
				foundTopic = true;
				break;
			}
		}

		if (foundTopic)
			return topic;
		else {
			for (int i = 0; i < children.size() && !foundTopic; i++) {
				Topic child = topic = ((Topic) children.get(i));
				topic = findTopic(child.getChildrenList(), topicId);
				if ((topic != null) && (foundTopic))
					return topic;
			}
			return null;
		}

	}
	private void generateHeader(StringBuffer buffer) {
		buffer.append("<HEAD>");
		// officially, a HEAD element needs a TITLE. fake it.
		buffer.append("<TITLE/>");

		// make sure that we have everything in UTF-8 because this is
		// what this string buffer will converted to.
		buffer.append("<META http-equiv=\"Content-Type\" ");
		buffer.append("content=\"text/html; charset=utf-8\">");

		// set Expires to any old date to avoid caching by IE.
		// HTTP servers sometimes return this info as part of the
		// respone.  
		buffer.append("<META HTTP-EQUIV=\"Expires\" ");
		buffer.append("CONTENT=\"Mon, 04 Dec 2000 11:11:11 GMT\"> ");
		buffer.append("</HEAD>");

	}
	/** 
	 * generates a Table Of Contents as an InputStream
	 */
	private InputStream generateTableOfContents(Topic[] topicList) {

		StringBuffer tableOfContents = new StringBuffer();
		tableOfContents.append("<html>");
		generateHeader(tableOfContents);

		tableOfContents.append("<body>");
		tableOfContents.append("<h1 ALIGN=CENTER>");
		tableOfContents.append(Resources.getString("Table_Of_Contents"));
		tableOfContents.append("</h1>");
		tableOfContents.append("<h3>");

		try {
			tableOfContents.append("<ol>");
			for (int i = 0; i < topicList.length; i++) {
				tableOfContents.append("<li>");
				tableOfContents.append(topicList[i].getLabel());
				tableOfContents.append("</li>");
			}
			tableOfContents.append("<ol>");
			tableOfContents.append("</h3>");

			tableOfContents.append("</body></html>");
			byte[] bytes = tableOfContents.toString().getBytes("UTF-8");

			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			return inputStream;
		} catch (Exception e) {
			// null handled by calling class
			return null;
		}

	}
	/** 
	 * generates a Table Of Contents as an InputStream
	 */
	public InputStream generateTableOfContents(
		String infosetId,
		String viewId,
		String topicId) {

		try {
			// get to the topic in the navigation model. Find the infoset, and then
			// the view.
			InfoSet infoset =
				HelpSystem.getInstance().getNavigationManager().getInfoSet(infosetId);
			InfoView view = infoset.getView(viewId);
			List children = view.getChildrenList();

			// now find the topic in the view.
			Topic topic = findTopic(children, topicId);

			// cleanup for next rev.
			foundTopic = false;

			// create the list of children topics.
			Topic[] topicList = getTopicList(topic);

			return generateTableOfContents(topicList);

		} catch (Exception e) {
			// return null to signal problem.
			return null;
		}

	}
	/**
	 * Returns an Topic array off all topics to be printed.
	 * returns null if array could not be created.
	 */
	public static Topic[] getTopicList(Topic rootTopic) {
		try {
			Vector objectVector = new Vector();
			populateTopicVector(rootTopic, objectVector);

			Topic[] topicList = new Topic[objectVector.size()];
			objectVector.toArray(topicList);

			return topicList;
		} catch (Exception e) {
			// signal problem
			return null;
		}
	}
	/** 
	 * recursive method to generate the left to right, top to bottom 
	 * list of topic URLs.
	 */
	private static void populateTopicVector(Object selection, Vector topicVector) {

		if (selection instanceof Topic) {
			Topic topicElement = (Topic) selection;

			if (topicElement.getHref() != null && topicElement.getHref() != "")
				topicVector.add(topicElement);
			java.util.List topicList = topicElement.getChildrenList();
			for (int i = 0; i < topicList.size(); i++) {
				populateTopicVector(topicList.get(i), topicVector);
			}
		}
	}
}
