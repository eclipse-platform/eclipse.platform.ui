package org.eclipse.help.internal.contributors;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.lang.*;

import org.eclipse.help.internal.util.*;

/**
 * Map (contexts) contributor
 */
public interface ContextContributor extends Contributor {

	// Only for DESC_TXT_BOLD property, lets use the properties file
	// since ID writers get exposed to this field.
	public static final String MAP_ELEM = "map";
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
