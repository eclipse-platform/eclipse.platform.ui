package org.eclipse.help.internal.contributors;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.lang.*;

/**
 * View Contributor
 */
public interface ViewContributor extends Contributor {

	public static final String VIEW_ELEM = "infoview";
	public static final String VIEW_ID_ATTR = ID_ATTR;
	public static final String VIEW_LABEL_ATTR = "label";
	public static final String INFOSET_ELEM = "infoset";
	public static final String INFOSET_HREF_ATTR = "href";
	public static final String INFOSET_ID_ATTR = ID_ATTR;
	public static final String INFOSET_LABEL_ATTR = "label";
	public static final String INFOSET_NAME_ATTR = NAME_ATTR;
	public static final String INFOSET_STANDALONE_ATTR = "standalone";
}
