/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.EditorAreaHelper;
import org.eclipse.ui.internal.EditorManager;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * @since 3.3
 * 
 */
public abstract class TabBehaviour {

	public static TweakKey KEY = new Tweaklets.TweakKey(TabBehaviour.class);

	static {
		Tweaklets.setDefault(TabBehaviour.KEY, new TabBehaviourMRU());
	}

	/**
	 * 
	 * @return
	 */
	public abstract boolean alwaysShowPinAction();

	/**
	 * 
	 * @param page
	 * @return
	 */
	public abstract IEditorReference findReusableEditor(WorkbenchPage page);

	public abstract IEditorReference reuseInternalEditor(WorkbenchPage page,
			EditorManager manager, EditorAreaHelper editorPresentation,
			EditorDescriptor desc, IEditorInput input,
			IEditorReference reusableEditorRef);

	/**
	 * Does nothing by default. Can be overridden by subclasses. 
	 * 
	 * @param editorReuseGroup
	 * @param showMultipleEditorTabs
	 */
	public void setPreferenceVisibility(Composite editorReuseGroup,
			Button showMultipleEditorTabs) {
	}

	/**
	 * @return
	 */
	public boolean autoPinOnDirty() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isPerTabHistoryEnabled() {
		return false;
	}

	/**
	 * @param originalMatchFlags
	 * @return
	 */
	public int getReuseEditorMatchFlags(int originalMatchFlags) {
		return originalMatchFlags;
	}

	/**
	 * @return
	 */
	public int getEditorReuseThreshold() {
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		return store.getInt(IPreferenceConstants.REUSE_EDITORS);
	}

	/**
	 * @return
	 */
	public boolean enableMRUTabVisibility() {
		return true;
	}

}
