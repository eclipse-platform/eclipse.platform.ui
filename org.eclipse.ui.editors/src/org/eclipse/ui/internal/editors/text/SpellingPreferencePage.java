/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor;


/**
 * Spelling preference page.
 * <p>
 * Note: Must be public since it is referenced from plugin.xml
 * </p>
 *
 * @since 3.1
 */
public class SpellingPreferencePage extends AbstractConfigurationBlockPreferencePage {

	/**
	 * Status monitor.
	 */
	private class StatusMonitor implements IPreferenceStatusMonitor {

		/*
		 * @see org.eclipse.ui.texteditor.spelling.IStatusMonitor#statusChanged(org.eclipse.core.runtime.IStatus)
		 */
		public void statusChanged(IStatus status) {
			handleStatusChanged(status);
		}
	}

	/**
	 * Handles status changes.
	 *
	 * @param status the new status
	 */
	protected void handleStatusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#getHelpId()
	 */
	protected String getHelpId() {
		// XXX: To be made public after 3.3, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=188281
		return IAbstractTextEditorHelpContextIds.PREFIX + "spelling_preference_page_context"; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setDescription()
	 */
	protected void setDescription() {
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigurationBlockPreferencePage#setPreferenceStore()
	 */
	protected void setPreferenceStore() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.AbstractConfigureationBlockPreferencePage#createConfigurationBlock(org.eclipse.ui.internal.editors.text.OverlayPreferenceStore)
	 */
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new SpellingConfigurationBlock(overlayPreferenceStore, new StatusMonitor());
	}
}
