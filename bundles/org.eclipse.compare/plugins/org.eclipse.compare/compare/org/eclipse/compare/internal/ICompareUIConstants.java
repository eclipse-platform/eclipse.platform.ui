package org.eclipse.compare.internal;


public interface ICompareUIConstants {
	public final String PREFIX = CompareUIPlugin.getPluginId() + "."; //$NON-NLS-1$
	
	public static final String DTOOL_NEXT= "dlcl16/next_nav.gif";	//$NON-NLS-1$
	public static final String ETOOL_NEXT= "elcl16/next_nav.gif";	//$NON-NLS-1$
	public static final String CTOOL_NEXT= ETOOL_NEXT;
	
	public static final String DTOOL_PREV= "dlcl16/prev_nav.gif";	//$NON-NLS-1$
	public static final String ETOOL_PREV= "elcl16/prev_nav.gif";	//$NON-NLS-1$
	public static final String CTOOL_PREV= ETOOL_PREV;

	public static final String RETARGET_PROJECT= "eview16/compare_view.gif";	//$NON-NLS-1$
	
	public static final String IGNORE_WHITESPACE_ENABLED= "etool16/ignorews_edit.gif";	//$NON-NLS-1$
	public static final String IGNORE_WHITESPACE_DISABLED= "dtool16/ignorews_edit.gif";	//$NON-NLS-1$
	
	public static final String REVERSE_PATCH_ENABLED = "etool16/reverse_enabled.gif";	//$NON-NLS-1$
	
	public static final String PROP_ANCESTOR_VISIBLE = PREFIX + "AncestorVisible"; //$NON-NLS-1$
	public static final String PROP_IGNORE_ANCESTOR = PREFIX + "IgnoreAncestor"; //$NON-NLS-1$
	public static final String PROP_TITLE = PREFIX + "Title"; //$NON-NLS-1$
	public static final String PROP_TITLE_IMAGE = "TitleImage"; //$NON-NLS-1$
}
