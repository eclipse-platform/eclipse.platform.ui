package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * This font field editor implements chaining between the workbench's preference
 * store and a given target preference store. Any time the workbench's preference
 * store changes the change is propagated to the target store. Propagation means 
 * that the actual value stored in the workbench store is set as default value in
 * the target store. If the target store does not contain a value rather than the
 * default value, the new default value is immediately effective.
 * 
 * @see FontFieldEditor 
 */
public class PropagatingFontFieldEditor extends FontFieldEditor {
	
	
	private Composite fParent;
	
	/**
	 * Creates a new font field editor with the given parameters.
	 * 
	 * @param name the editor's name
	 * @param labelText the text shown as editor description
	 * @param parent the editor's parent widget
	 */
	public PropagatingFontFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		fParent= parent;
	}
	
	/*
	 * @see FontFieldEditor#doLoad()
	 */
	protected void doLoad() {
		if (getPreferenceStore().isDefault(getPreferenceName()))
			loadDefault();
		super.doLoad();
		checkForDefault();
	}
	
	/*
	 * @see FontFieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		super.doLoadDefault();
		checkForDefault();
	}
	
	/**
	 * Checks whether this editor presents the default value "inheritated"
	 * from the workbench rather than its own font.
	 */
	private void checkForDefault() {
		if (presentsDefaultValue()) {
			Control c= getValueControl(fParent);
			if (c instanceof Label)
				((Label) c).setText(EditorMessages.getString("PropagatingFontFieldEditor.defaultWorkbenchFont")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Propagates the text font set in the destination store to the
	 * target store using the given key in the target store.
	 * 
	 * @param destination the store from which to read the text font
	 * @param target the store to which to propagate the text font
	 * @param targetKey teh key to be used in the target store
	 */
	private static void propagateFont(IPreferenceStore destination, IPreferenceStore target, String targetKey) {
		FontData fd= PreferenceConverter.getFontData(destination, JFaceResources.TEXT_FONT);
		if (fd != null) {
			boolean isDefault= target.isDefault(targetKey);	// save old state!
			PreferenceConverter.setDefault(target, targetKey, fd);
			if (isDefault) {			
				// restore old state
				target.setToDefault(targetKey);
			}
		}
	}
	
	/**
	 * Starts the propagation of the text font preference set in the workbench
	 * to given target preference store using the given preference key.
	 * 
	 * @param target the target preference store
	 * @param targetKey the key to be used in the target preference store
	 */
	public static void startPropagate(final IPreferenceStore target, final String targetKey) {
		IPreferenceStore wps= null;
		Plugin plugin= Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin= (AbstractUIPlugin) plugin;
			wps= uiPlugin.getPreferenceStore();
		}
		
		if (wps != null) {
			final IPreferenceStore wps2= wps;
			wps.addPropertyChangeListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (JFaceResources.TEXT_FONT.equals(event.getProperty()))						
						propagateFont(wps2, target, targetKey);
				}
			});
			
			propagateFont(wps, target, targetKey);
		}
	}
}

