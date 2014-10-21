/*******************************************************************************
 * Copyright (c) 2011-2014 EclipseSource Muenchen GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Jonas - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.osgi.util.NLS;

/**
 * @author Jonas
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.tools.emf.editor3x.messages"; //$NON-NLS-1$
	public static String ContributionEditor_CurrentURLIsInvalid;
	public static String ContributionEditor_FailedToOpenEditor;
	public static String ContributionEditor_InvalidURL;
	public static String ModelEditorPreferencePage_Color;
	public static String ModelEditorPreferencePage_ForcesReadOnlyXMITab;
	public static String ModelEditorPreferencePage_FormTab;
	public static String ModelEditorPreferencePage_GeneratedID;
	public static String ModelEditorPreferencePage_ListTabe;
	public static String ModelEditorPreferencePage_NotRenderedColor;
	public static String ModelEditorPreferencePage_NotVisibleAndNotRenderedColor;
	public static String ModelEditorPreferencePage_NotVisibleColor;
	public static String ModelEditorPreferencePage_RememberColumnSettings;
	public static String ModelEditorPreferencePage_RememberFilterSettings;
	public static String ModelEditorPreferencePage_RequiresReopeningModel;
	public static String ModelEditorPreferencePage_SearchableTree;
	public static String ModelEditorPreferencePage_ShowID;
	public static String ModelMoveParticipant_CreatingChange;
	public static String RedoAction_Redo;
	public static String UndoAction_Undo;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
