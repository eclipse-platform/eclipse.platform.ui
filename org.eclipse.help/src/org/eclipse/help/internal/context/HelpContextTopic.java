package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.help.IHelpTopic;
import org.eclipse.help.internal.util.ContextResources;
import org.xml.sax.Attributes;
/**
 * Default implementation for a topic contribution
 */
public class HelpContextTopic implements IHelpTopic, IContextContributionNode {
	protected String href;
	protected String label;
	protected String translatedLabel;
	protected String plugin;
	public HelpContextTopic(Attributes attrs) {
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
	/**
	 * Sets the label without translation, as it would appear if this was created from xml navigation file
	 * @param label - raw label, which needs to appear in the property fille, or untranslatable label
	 */
	public void setLabel(String rawLabel) {
		this.label = rawLabel;
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
	public Iterator getChildren() {
		return new ArrayList().iterator();
	}
	/**
	 */
	public void setPlugin(String id) {
		this.plugin = id;
	}
}