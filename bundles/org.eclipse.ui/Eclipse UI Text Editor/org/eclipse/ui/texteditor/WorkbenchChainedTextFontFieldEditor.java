package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.PropagatingFontFieldEditor;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * This font field editor implements chaining between the workbench's preference
 * store and a given target preference store. Any time the workbench's preference
 * for the text font changes, the change is propagated to the target store.
 * Propagation means that the actual text font stored in the workbench store is set as
 * default text font in the target store. If the target store does not contain a value
 * rather than the default text font, the new default text font is immediately effective.
 * 
 * @see FontFieldEditor 
 */
public class WorkbenchChainedTextFontFieldEditor extends PropagatingFontFieldEditor {
	
	/**
	 * Creates a new font field editor with the given parameters.
	 * 
	 * @param name the editor's name
	 * @param labelText the text shown as editor description
	 * @param parent the editor's parent widget
	 */
	public WorkbenchChainedTextFontFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent, EditorMessages.getString("WorkbenchChainedTextFontFieldEditor.defaultWorkbenchTextFont")); //$NON-NLS-1$
	}
	
	/**
	 * Starts the propagation of the text font preference set in the workbench
	 * to given target preference store using the given preference key.
	 * 
	 * @param target the target preference store
	 * @param targetKey the key to be used in the target preference store
	 */
	public static void startPropagate(IPreferenceStore target, String targetKey) {
		Plugin plugin= Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin= (AbstractUIPlugin) plugin;
			IPreferenceStore store= uiPlugin.getPreferenceStore();
			if (store != null)
				PropagatingFontFieldEditor.startPropagate(store, JFaceResources.TEXT_FONT, target, targetKey);
		}
	}
}

