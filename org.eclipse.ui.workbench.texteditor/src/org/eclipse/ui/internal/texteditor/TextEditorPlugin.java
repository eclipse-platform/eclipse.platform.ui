/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.plugin.AbstractUIPlugin;

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

	private static TextEditorPlugin fgPlugin;
	
	private EditPosition fLastEditPosition;
	private Set fLastEditPositionDependentActions;

	public TextEditorPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		Assert.isTrue(fgPlugin == null);
		fgPlugin= this;
	}

	public static TextEditorPlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * Text editor UI plug-in Id (value <code>"org.eclipse.ui.workbench.texteditor"</code>).
	 */
	public static final String PLUGIN_ID= "org.eclipse.ui.workbench.texteditor"; //$NON-NLS-1$

	/**
	 * Returns the last edit position.
	 *
	 * @return	the last edit position or <code>null</code>	if there is no last edit position
	 * @see EditPosition
	 */
	EditPosition getLastEditPosition() {
		return fLastEditPosition;
	}

	public void setLastEditPosition(EditPosition lastEditPosition) {
		fLastEditPosition= lastEditPosition;
		if (fLastEditPosition != null && fLastEditPositionDependentActions != null) {
			Iterator iter= fLastEditPositionDependentActions.iterator();
			while (iter.hasNext())
				((IAction)iter.next()).setEnabled(true);
			fLastEditPositionDependentActions= null;
		}
	}
	
	void addLastEditPositionDependentAction(IAction action) {
		if (fLastEditPosition != null)
			return;
		if (fLastEditPositionDependentActions == null)
			fLastEditPositionDependentActions= new HashSet();
		fLastEditPositionDependentActions.add(action);
	}

	void removeLastEditPositionDependentAction(IAction action) {
		if (fLastEditPosition != null)
			return;
		if (fLastEditPositionDependentActions != null)
			fLastEditPositionDependentActions.remove(action);
	}
}
