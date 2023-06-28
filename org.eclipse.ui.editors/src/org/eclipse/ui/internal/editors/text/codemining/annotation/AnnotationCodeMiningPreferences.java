/*******************************************************************************
 * Copyright (c) 2019 Altran Netherlands B.V. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Niko Stotz (Altran Netherlands B.V.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.codemining.annotation;

import org.eclipse.jdt.annotation.Nullable;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Simplifies access to user preferences related to Annotation-based code minings.
 *
 * <p>
 * All methods fall back to defaults if the preference store is unavailable.
 * </p>
 *
 * <p>
 * The following preferences are available:
 * </p>
 *
 * <dl>
 * <dt>show infos <i>(default: <code>false</code>)</i></dt>
 * <dd>Whether INFO-level annotations should be shown as code minings.</dd>
 *
 * <dt>show warnings <i>(default: <code>false</code>)</i></dt>
 * <dd>Whether WARNING-level annotations should be shown as code minings.</dd>
 *
 * <dt>show errors <i>(default: <code>false</code>)</i></dt>
 * <dd>Whether ERROR-level annotations should be shown as code minings.</dd>
 *
 * <dt>Maximum annotations shown <i>(default: <code>100</code>)</i></dt>
 * <dd>How many annotations should be shown at most as code minings. Mainly to prevent bad editor
 * performance.</dd>
 * </dl>
 *
 * @since 3.13
 */
public class AnnotationCodeMiningPreferences {
	private IPreferenceStore preferenceStore;

	public boolean isInfoEnabled() {
		return (getLevel() & AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__INFO) > 0;
	}

	public boolean isWarningEnabled() {
		return (getLevel() & AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__WARNING) > 0;
	}

	public boolean isErrorEnabled() {
		return (getLevel() & AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__ERROR) > 0;
	}

	public boolean isEnabled() {
		final IPreferenceStore node= getPreferences();
		if (node == null) {
			return false;
		}

		final int max= getMaxMinings();

		return max > 0 && getLevel() > 0;
	}

	int getLevel() {
		IPreferenceStore node= getPreferences();

		return node != null
				? node.getInt(AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL)
				: AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_LEVEL__DEFAULT;
	}

	int getMaxMinings() {
		final IPreferenceStore node= getPreferences();

		return node != null
				? node.getInt(AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_MAX)
				: AnnotationCodeMiningPreferenceConstants.SHOW_ANNOTATION_CODE_MINING_MAX__DEFAULT;
	}

	protected @Nullable IPreferenceStore getPreferences() {
		if (preferenceStore == null) {
			preferenceStore= AnnotationCodeMiningPreferenceConstants.getPreferenceStore();
		}

		return preferenceStore;
	}
}
