/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.preferences;

import java.util.Vector;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntViewContentProvider;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	/**
	 * Constructor
	 */
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Preferences.getPreferenceStore());
		setDescription(ResourceMgr.getString("Preferences.PageDescription"));
		Preferences.setDefaults();
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(
			IAntViewConstants.PREF_PROJECT_DISPLAY,
			ResourceMgr.getString("Preferences.ProjectNode.Label"),
			3,
			new String[][] {  
				 { ResourceMgr.getString("Preferences.ProjectNode.NameAttr"), IAntViewConstants.PROJECT_DISPLAY_NAMEATTR },
				 { ResourceMgr.getString("Preferences.ProjectNode.DirLoc"),   IAntViewConstants.PROJECT_DISPLAY_DIRLOC }, 
 				 { ResourceMgr.getString("Preferences.ProjectNode.Both"),     IAntViewConstants.PROJECT_DISPLAY_BOTH }
		    }, 
		    getFieldEditorParent()
		));
		addField(new RadioGroupFieldEditor(
			IAntViewConstants.PREF_TARGET_DISPLAY,
			ResourceMgr.getString("Preferences.TargetNode.Label"),
			3,
			new String[][] {  
				 { ResourceMgr.getString("Preferences.TargetNode.NameAttr"), IAntViewConstants.TARGET_DISPLAY_NAMEATTR },
				 { ResourceMgr.getString("Preferences.TargetNode.DescAttr"), IAntViewConstants.TARGET_DISPLAY_DESCATTR }, 
 				 { ResourceMgr.getString("Preferences.TargetNode.Both"),     IAntViewConstants.TARGET_DISPLAY_BOTH }
		    }, 
		    getFieldEditorParent()
		));
		addField(new RadioGroupFieldEditor(
			IAntViewConstants.PREF_TARGET_FILTER,
			ResourceMgr.getString("Preferences.TargetFilter.Label"),
			2,
			new String[][] {  
				 { ResourceMgr.getString("Preferences.TargetFilter.None"),     IAntViewConstants.TARGET_FILTER_NONE },
				 { ResourceMgr.getString("Preferences.TargetFilter.DescAttr"), IAntViewConstants.TARGET_FILTER_DESCATTR } 
		    }, 
		    getFieldEditorParent()
		)); 
		addField(new StringFieldEditor(
		    IAntViewConstants.PREF_ANT_BUILD_FILE, 
		    ResourceMgr.getString("Preferences.AntBuildFile.Label"),
		    getFieldEditorParent()
		));
	}	
	/**
	 * Method performOK.
	 * @return boolean
	 */
	public boolean performOk() { 
		if (super.performOk()) { 
           AntView antView = AntUtil.getAntView();
			if (antView == null) {
				return true;
			}
		   AntViewContentProvider viewContentProvider =  antView.getViewContentProvider();
			if (viewContentProvider == null) {
				return true;
			}
		   Vector targetVector = viewContentProvider.getTargetVector();
         	
		   targetVector.removeAllElements();
		   viewContentProvider.reset();
		   antView.refresh();	
		
		   return true;
		}
		return false;
	}
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}
