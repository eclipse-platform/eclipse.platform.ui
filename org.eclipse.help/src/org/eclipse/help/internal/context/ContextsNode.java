/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import java.util.*;
import org.eclipse.help.internal.util.Resources;
import org.xml.sax.Attributes;
/**
 * Object in hierarchy of context contributions
 */
public abstract class ContextsNode {
	public static final String CONTEXTS_ELEM = "contexts";
	public static final String CONTEXT_ELEM = "context";
	public static final String DESC_ELEM = "description";
	public static final String RELATED_ELEM = "topic";
	public static final String RELATED_HREF = "href";
	public static final String RELATED_LABEL = "label";
	/**
	 * Internal representation of &lt;b&gt; - unlikely to occur in a text
	 */
	public static final String BOLD_CLOSE_TAG = "</@#$b>";
	/**
	 * Internal representation of &lt;b&gt; - unlikely to occur in a text
	 */
	public static final String BOLD_TAG = "<@#$b>";
	public static final String DESC_TXT_BOLD = Resources.getString("bold_tag_name");
	protected List children = new ArrayList();
	/**
	 * When a builder builds the contexts, each node
	 * must "accomodate" the builder by responding to the build() 
	 * command.
	 */
	public abstract void build(ContextsBuilder builder);
	/**
	 * ContextsNode constructor.
	 */
	public ContextsNode(Attributes attrs) {
	}
	/**
	 * Adds a child
	 * @param child IContextsNode
	 */
	public void addChild(ContextsNode child) {
		children.add(children.size(), child);
	}
	/**
	 * Obtains children
	 */
	public List getChildren() {
		return children;
	}
}