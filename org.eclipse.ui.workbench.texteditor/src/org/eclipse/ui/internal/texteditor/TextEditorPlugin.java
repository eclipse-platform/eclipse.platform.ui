/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.ISharedTextColors;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

/**
 * The plug-in runtime class for the text editor UI plug-in (id <code>"org.eclipse.ui.workbench.texteditor"</code>).
 * This class provides static methods for:
 * <ul>
 *  <li> getting the last editor position.</li>
 * </ul>
 * <p>
 * This class provides static methods and fields only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @since 2.1
 */
public final class TextEditorPlugin extends AbstractUIPlugin {

	/** The plugin instance */
	private static TextEditorPlugin fgPlugin;
	
	/** The last edit position */
	private EditPosition fLastEditPosition;
	/** The action which goes to the last edit position */
	private Set fLastEditPositionDependentActions;

	private ISharedTextColors fSharedTextColors;


	/**
	 * Creates a plug-in instance.
	 * 
	 * @param descriptor the plug-in descriptor
	 */
	public TextEditorPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		Assert.isTrue(fgPlugin == null);
		fgPlugin= this;
	}

	/**
	 * Returns the plug-in instance.
	 * 
	 * @return the text editor plug-in instance
	 */
	public static TextEditorPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Text editor UI plug-in Id (value <code>"org.eclipse.ui.workbench.texteditor"</code>).
	 */
	public static final String PLUGIN_ID= "org.eclipse.ui.workbench.texteditor"; //$NON-NLS-1$

	/*
	 * @see AbstractUIPlugin#initializeDefaultPluginPreferences()
	 */
	protected void initializeDefaultPluginPreferences() {
		MarkerAnnotationPreferences.initializeDefaultValues(getPreferenceStore());
	}
	
	/**
	 * Returns the last edit position.
	 *
	 * @return	the last edit position or <code>null</code>	if there is no last edit position
	 * @see EditPosition
	 */
	EditPosition getLastEditPosition() {
		return fLastEditPosition;
	}

	/**
	 * Sets the last edit position.
	 *
	 * @param lastEditPosition	the last edit position
	 * @see EditPosition
	 */
	public void setLastEditPosition(EditPosition lastEditPosition) {
		fLastEditPosition= lastEditPosition;
		if (fLastEditPosition != null && fLastEditPositionDependentActions != null) {
			Iterator iter= fLastEditPositionDependentActions.iterator();
			while (iter.hasNext())
				((IAction)iter.next()).setEnabled(true);
			fLastEditPositionDependentActions= null;
		}
	}
	
	/**
	 * Adds the given action to the last edit position dependent actions.
	 * 
	 * @param action the goto last edit position action
	 */
	void addLastEditPositionDependentAction(IAction action) {
		if (fLastEditPosition != null)
			return;
		if (fLastEditPositionDependentActions == null)
			fLastEditPositionDependentActions= new HashSet();
		fLastEditPositionDependentActions.add(action);
	}

	/**
	 * Removes the given action from the last edit position dependent actions.
	 * 
	 * @param action the action that depends on the last edit position
	 */
	void removeLastEditPositionDependentAction(IAction action) {
		if (fLastEditPosition != null)
			return;
		if (fLastEditPositionDependentActions != null)
			fLastEditPositionDependentActions.remove(action);
	}
	
	public ISharedTextColors getSharedTextColors() {
		if (fSharedTextColors == null)
			fSharedTextColors= new SharedTextColors();
		return fSharedTextColors;
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.EditorsPlugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		if (fSharedTextColors != null) {
			fSharedTextColors.dispose();
			fSharedTextColors= null;
		}
		super.shutdown();
	}
	
	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}	
}