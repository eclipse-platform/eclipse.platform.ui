/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.dynamic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * A DOM node adapter/proxy - provides the same interface as Node but instead
 * reads and writes to an underlying DOM node. This allows us to use the same
 * processor for two models, by adapting one of them.
 */
public class DocumentNode extends org.eclipse.help.Node {

	private Node node;
	
	/*
	 * Creates an adapter for the given DOM node. All operations on this node
	 * will be performed on the given node instead.
	 */
	public DocumentNode(Node node) {
		this.node = node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#appendChild(org.eclipse.help.Node)
	 */
	public void appendChild(org.eclipse.help.Node newChild) {
		DocumentNode newNode = copyNode(newChild);
		node.appendChild(newNode.node);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getAttribute(java.lang.String)
	 */
	public String getAttribute(String name) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element)node;
			if (elem.hasAttribute(name)) {
				return elem.getAttribute(name);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getAttributes()
	 */
	public Set getAttributes() {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			NamedNodeMap map = node.getAttributes();
			Set names = new HashSet();
			for (int i=0;i<map.getLength();++i) {
				Node attribute = map.item(i);
				names.add(attribute.getNodeName());
			}
			return names;
		}
		return new HashSet(0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getChildNodes()
	 */
	public org.eclipse.help.Node[] getChildNodes() {
		if (node.hasChildNodes()) {
			List children = new ArrayList();
			NodeList list = node.getChildNodes();
			for (int i=0;i<list.getLength();++i) {
				Node child = list.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE || child.getNodeType() == Node.TEXT_NODE) {
					children.add(new DocumentNode(child));
				}
			}
			return (org.eclipse.help.Node[])children.toArray(new org.eclipse.help.Node[children.size()]);
		}
		return new org.eclipse.help.Node[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getNodeName()
	 */
	public String getNodeName() {
		return node.getNodeName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getParentNode()
	 */
	public org.eclipse.help.Node getParentNode() {
		return new DocumentNode(node.getParentNode());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getNodeValue()
	 */
	public String getNodeValue() {
		return node.getNodeValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#insertBefore(org.eclipse.help.Node, org.eclipse.help.Node)
	 */
	public void insertBefore(org.eclipse.help.Node newChild, org.eclipse.help.Node refChild) {
		if (refChild instanceof DocumentNode) {
			DocumentNode newNode = copyNode(newChild);
			node.insertBefore(newNode.node, ((DocumentNode)refChild).node);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element)node;
			elem.removeAttribute(name);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#removeChild(org.eclipse.help.Node)
	 */
	public void removeChild(org.eclipse.help.Node nodeToRemove) {
		if (nodeToRemove instanceof DocumentNode) {
			node.removeChild(((DocumentNode)nodeToRemove).node);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String name, String value) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element elem = (Element)node;
			elem.setAttribute(name, value);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#setNodeValue(java.lang.String)
	 */
	public void setNodeValue(String value) {
		node.setNodeValue(value);
	}
	
	/*
	 * Copies the given node as a DOMNode for this document.
	 */
	private DocumentNode copyNode(org.eclipse.help.Node nodeToCopy) {
		// copy the node itself
		Document dom = node.getOwnerDocument();
		Node newNode;
		if (nodeToCopy.getValue() == null) {
			newNode = dom.createElement(nodeToCopy.getNodeName());
		}
		else {
			newNode = dom.createTextNode(nodeToCopy.getValue());
		}
		if (newNode.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)newNode;
			Iterator iter = nodeToCopy.getAttributes().iterator();
			while (iter.hasNext()) {
				String name = (String)iter.next();
				element.setAttribute(name, nodeToCopy.getAttribute(name));
			}
		}
		
		// copy children
		DocumentNode copy = new DocumentNode(newNode);
		org.eclipse.help.Node[] children = nodeToCopy.getChildNodes();
		for (int i=0;i<children.length;++i) {
			copy.appendChild(children[i]);
		}
		return copy;
	}
}
