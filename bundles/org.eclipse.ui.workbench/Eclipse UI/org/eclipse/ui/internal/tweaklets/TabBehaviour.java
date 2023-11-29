/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.tweaklets.Tweaklets.TweakKey;

/**
 * @since 3.3
 */
public abstract class TabBehaviour {

	public static TweakKey KEY = new Tweaklets.TweakKey(TabBehaviour.class);

	static {
		Tweaklets.setDefault(TabBehaviour.KEY, new TabBehaviourMRU());
	}

	public abstract boolean alwaysShowPinAction();

	public abstract IEditorReference findReusableEditor(WorkbenchPage page);

	public abstract IEditorReference reuseInternalEditor(WorkbenchPage page, Object manager, Object editorPresentation,
			EditorDescriptor desc, IEditorInput input, IEditorReference reusableEditorRef);

	/**
	 * Does nothing by default. Can be overridden by subclasses.
	 */
	public void setPreferenceVisibility(Composite editorReuseGroup, Button showMultipleEditorTabs) {
	}

	public boolean autoPinOnDirty() {
		return false;
	}

	public boolean isPerTabHistoryEnabled() {
		return false;
	}

	public int getReuseEditorMatchFlags(int originalMatchFlags) {
		return originalMatchFlags;
	}

	public int getEditorReuseThreshold() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		return store.getInt(IPreferenceConstants.REUSE_EDITORS);
	}

	public boolean enableMRUTabVisibility() {
		return true;
	}

	public boolean sortEditorListAlphabetically() {
		return true;
	}

	public Color createVisibleEditorsColor(Display display, RGB originalForegroundColor, RGB originalBackgroundColor) {
		return new Color(display, originalForegroundColor);
	}

	public Font createVisibleEditorsFont(Display display, Font originalFont) {
		FontData fontData[] = originalFont.getFontData();
		return new Font(display, fontData);
	}

	public Font createInvisibleEditorsFont(Display display, Font originalFont) {
		FontData fontData[] = originalFont.getFontData();
		// Adding the bold attribute
		for (FontData element : fontData) {
			element.setStyle(element.getStyle() | SWT.BOLD);
		}
		return new Font(display, fontData);
	}

}
