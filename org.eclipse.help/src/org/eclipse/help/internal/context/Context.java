/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import java.util.List;

import org.eclipse.help.*;
import org.xml.sax.Attributes;
/**
 * Context object, as defined in the map.xml
 */
public class Context extends ContextsNode implements IContext {
	private String text;
	protected String pluginID;
	protected String shortID;
	/**
	 * Context constructor.
	 */
	public Context(Attributes attrs) {
		super(attrs);
		if (attrs == null)
			return;
		shortID = attrs.getValue("id");
	}
	public String getText() {
		return text;
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
	/**
	 * Obtains short id (without plugin)
	 */
	public String getShortId() {
		return shortID;
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
	 * @see ContextsNode#build(ContextsBuilder)
	 */
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
	/**
	 * Replaces children list
	 */
	public void setChildren(List children) {
		this.children = children;
	}
}