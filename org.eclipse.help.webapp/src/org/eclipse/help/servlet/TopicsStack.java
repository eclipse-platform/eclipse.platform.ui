/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;

import java.util.*;
import org.w3c.dom.*;

/**
 * Stack of topics elements. Used to implement recursive traversal of xml dom.
 */

public class TopicsStack extends ArrayList {
	public void pushChildren(Element e) {
		NodeList children = e.getChildNodes();
		int n = children.getLength();
		for (int i=n-1; i >= 0; i--) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				add(node);
		}
	}

	public void push(Element e) {
		add(e);
	}
	
	public Element pop() {
		if (isEmpty())
			return null;
		else
			return (Element) remove(size() - 1);
	}
	
	public Element peek()
	{
		if (isEmpty())
			return null;
		else
			return (Element) get(size() - 1);
	}
}