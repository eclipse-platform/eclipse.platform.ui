package org.eclipse.help.internal.contributors;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.*;

/**
 * Map (contexts) contributor
 */
public interface ActionContributor extends Contributor {
	public static final String ACTIONS_ELEM = "actions";
	public static final String ACTIONS_NAME_ATTR = NAME_ATTR;
	public static final String ACTIONS_VIEW_ATTR = "infoview";
	public static final String ACTIONS_STANDALONE_ATTR = "standalone";
	public static final String INSERT_AS_ATTR = "as";
	public static final String INSERT_AS_CHILD = "child";
	public static final String INSERT_AS_FIRST_CHILD = "first-child";
	public static final String INSERT_AS_LAST_CHILD = "last-child";
	public static final String INSERT_AS_NEXT_SIB = "next-sib";
	public static final String INSERT_AS_PREV_SIB = "prev-sib";
	public static final String INSERT_ELEM = "insert";
	public static final String INSERT_FROM_ATTR = "from";
	public static final String INSERT_TO_ATTR = "to";
}
