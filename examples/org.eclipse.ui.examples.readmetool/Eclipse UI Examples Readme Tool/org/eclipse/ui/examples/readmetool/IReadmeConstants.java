package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * This interface contains constants for use only within the
 * Readme example.
 */
public interface IReadmeConstants {
	public static final String PLUGIN_ID ="org.eclipse.ui.examples.readmetool";
	public static final String PREFIX = PLUGIN_ID+".";
	public static final String P_CONTENT_OUTLINE=PREFIX+"content_outline";
	public static final String P_SECTIONS=PREFIX+"sections";
	public static final String EXTENSION = "readme";
	public static final String TAG_PARSER = "parser";
	public static final String ATT_CLASS = "class";
	public static final String PP_SECTION_PARSER = "sectionParser";

	// Preference constants
	public static final String PRE_CHECK1=PREFIX+"check1";
	public static final String PRE_CHECK2=PREFIX+"check2";
	public static final String PRE_CHECK3=PREFIX+"check3";
	public static final String PRE_RADIO_CHOICE = PREFIX+"radio_choice";
	public static final String PRE_TEXT = PREFIX+"text";

	// Help context ids
	public static final String EDITOR_ACTION1_CONTEXT = PREFIX+"editor_action1_context";
	public static final String EDITOR_ACTION2_CONTEXT = PREFIX+"editor_action2_context";
	public static final String EDITOR_ACTION3_CONTEXT = PREFIX+"editor_action3_context";
	public static final String SECTIONS_VIEW_CONTEXT = PREFIX+"sections_view_context";
	public static final String PREFERENCE_PAGE_CONTEXT = PREFIX+"preference_page_context";
	public static final String PROPERTY_PAGE_CONTEXT = PREFIX+"property_page_context";
	public static final String PROPERTY_PAGE2_CONTEXT = PREFIX+"property_page2_context";
	public static final String EDITOR_CONTEXT = PREFIX+"editor_context";
	public static final String SECTIONS_DIALOG_CONTEXT = PREFIX+"sections_dialog_context";
	public static final String CONTENT_OUTLINE_PAGE_CONTEXT = PREFIX+"content_outline_page_context";
	public static final String CREATION_WIZARD_PAGE_CONTEXT = PREFIX+"creation_wizard_page_context";

}
