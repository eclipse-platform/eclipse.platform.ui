/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;


/**
 * Hyperlink detectors preference page.
 * <p>
 * Note: Must be public since it is referenced from plugin.xml
 * </p>
 *
 * @since 3.3
 */
public class HyperlinkDetectorsPreferencePage extends AbstractConfigurationBlockPreferencePage {

	@Override
	protected String getHelpId() {
		return ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE;
	}

	@Override
	protected void setDescription() {
		String description= TextEditorMessages.HyperlinkDetectorsConfigurationBlock_description;
		setDescription(description);
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return super.createDescriptionLabel(parent);
	}

	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new HyperlinkDetectorsConfigurationBlock(this, overlayPreferenceStore);
	}
}
