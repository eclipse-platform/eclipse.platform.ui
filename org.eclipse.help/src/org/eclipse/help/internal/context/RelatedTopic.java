package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.util.ContextResources;
import org.xml.sax.Attributes;
/**
 * Default implementation for a topic contribution
 */
public class RelatedTopic
	implements IHelpResource, IContextContributionNode {
	private final static List EMPTY_LIST = new ArrayList(0);
	protected String href;
	protected String label;
	protected String translatedLabel;
	protected String plugin;
	public RelatedTopic(Attributes attrs) {
		if (attrs == null)
			return;
		href = attrs.getValue(ContextContributor.RELATED_HREF);
		this.label = attrs.getValue(ContextContributor.RELATED_LABEL);
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
			if (translatedLabel.indexOf('%') == 0) {
				translatedLabel =
					ContextResources.getPluginString(plugin, translatedLabel.substring(1));
			}
		}
		return translatedLabel;
	}
	/*
	 * @see IContextContributionNode#addChild(IContextContributionNode)
	 */
	public IContextContributionNode addChild(IContextContributionNode child) {
		return child;
	}
	/*
	 * @see IContextContributionNode#getChildren()
	 */
	public List getChildren() {
		return EMPTY_LIST;
	}
	/**
	 */
	public void setPlugin(String id) {
		this.plugin = id;
	}
}