/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;


/**
 * Quick Diff preference page.
 * <p>
 * Note: Must be public since it is referenced from plugin.xml
 * </p>
 *
 * @since 3.0
 */
public class QuickDiffPreferencePage extends AbstractConfigurationBlockPreferencePage {

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#getHelpId()
	 */
	protected String getHelpId() {
		return ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE;
	}

	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createDescriptionLabel(org.eclipse.swt.widgets.Composite)
	 */
	protected Label createDescriptionLabel(Composite parent) {
		return null; // no description for new look
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setDescription()
	 */
	protected void setDescription() {
		String description= TextEditorMessages.QuickDiffConfigurationBlock_description;
		setDescription(description);
	}

	/*
	 * @see org.org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setPreferenceStore()
	 */
	protected void setPreferenceStore() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#createConfigurationBlock(org.eclipse.ui.internal.editors.text.OverlayPreferenceStore)
	 */
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new QuickDiffConfigurationBlock(overlayPreferenceStore);
	}
}
