/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.util.ContextResources;
import org.xml.sax.Attributes;
/**
 * Default implementation for a topic contribution
 */
public class RelatedTopic extends ContextsNode implements IHelpResource {
	protected String href;
	protected String label;
	protected String translatedLabel;
	protected String plugin;
	public RelatedTopic(Attributes attrs) {
		super(attrs);
		if (attrs == null)
			return;
		href = attrs.getValue(ContextsNode.RELATED_HREF);
		this.label = attrs.getValue(ContextsNode.RELATED_LABEL);
		if (this.label == null)
			this.label = "undefined";
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * Returns the translated label
	 */
	public String getLabel() {
		if (translatedLabel == null) {
			translatedLabel = label;
			if (translatedLabel.indexOf('%') == 0 && translatedLabel.length() >= 2) {
				translatedLabel =
					ContextResources.getPluginString(plugin, translatedLabel.substring(1));
			}
		}
		return translatedLabel;
	}
	/**
	 */
	public void setPlugin(String id) {
		this.plugin = id;
	}
	/**
	 * @see IContextContributionNode#build(ContextBuilder)
	 */
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
}