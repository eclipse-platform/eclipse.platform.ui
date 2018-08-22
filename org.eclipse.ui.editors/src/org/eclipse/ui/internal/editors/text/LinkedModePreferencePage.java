/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal.editors.text;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;

/**
 * The page for setting the editor options.
 *
 * @since 3.2
 */
public final class LinkedModePreferencePage extends AbstractConfigurationBlockPreferencePage {

	@Override
	protected String getHelpId() {
		return ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE;
	}

	@Override
	protected void setDescription() {
		String description= TextEditorMessages.LinkedModeConfigurationBlock_linking_title;
		setDescription(description);
	}

	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(EditorsUI.getPreferenceStore());
	}


	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return null; // no description for new look.
	}

	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new LinkedModeConfigurationBlock(overlayPreferenceStore);
	}
}
