package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.internal.contributors1_0.Contributor;
import org.eclipse.help.internal.util.Resources;
/**
 * Map (contexts) contributor
 */
public interface IContextContributor extends Contributor {
	// Only for DESC_TXT_BOLD property, lets use the properties file
	// since ID writers get exposed to this field.
	public static final String CONTEXT_ELEM = "context";
	public static final String CONTEXTS_ELEM = "contexts";
	public static final String DESC_ELEM = "description";
	public static final String RELATED_ELEM = "topic";
	public static final String RELATED_HREF = "href";
	public static final String RELATED_LABEL = "label";
	public static final String BOLD_CLOSE_TAG =
		"</" + Resources.getString("bold_tag_name") + ">";
	public static final String BOLD_TAG =
		"<" + Resources.getString("bold_tag_name") + ">";
	public static final String DESC_TXT_BOLD = Resources.getString("bold_tag_name");
}