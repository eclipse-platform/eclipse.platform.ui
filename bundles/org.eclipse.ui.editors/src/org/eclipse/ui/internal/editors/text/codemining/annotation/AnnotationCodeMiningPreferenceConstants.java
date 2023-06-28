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

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

/**
 * Preference constants used for the annotation code mining preference store.
 *
 * @since 3.13
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AnnotationCodeMiningPreferenceConstants {
	private AnnotationCodeMiningPreferenceConstants() {

	}

	/**
	 * A named preference that controls which {@link org.eclipse.jface.text.source.Annotation
	 * Annotations} level should be shown as code minings.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 *
	 * @since 3.13
	 */
	public final static String SHOW_ANNOTATION_CODE_MINING_LEVEL= "showAnnotationAsCodeMiningLevel"; //$NON-NLS-1$

	/**
	 * Value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL} to show no annotation code minings.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__NONE= 0b0;

	/**
	 * Value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL} to show info annotation code minings.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__INFO= 0b10;

	/**
	 * Value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL} to show warning annotation code minings.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__WARNING= 0b100;

	/**
	 * Value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL} to show error annotation code minings.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__ERROR= 0b1000;

	/**
	 * Value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL} to show error and warning annotation
	 * code minings.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__ERROR_WARNING= SHOW_ANNOTATION_CODE_MINING_LEVEL__ERROR
			| SHOW_ANNOTATION_CODE_MINING_LEVEL__WARNING;

	/**
	 * Value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL} to show error, warning, and info
	 * annotation code minings.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__ERROR_WARNING_INFO= SHOW_ANNOTATION_CODE_MINING_LEVEL__ERROR
			| SHOW_ANNOTATION_CODE_MINING_LEVEL__WARNING
			| SHOW_ANNOTATION_CODE_MINING_LEVEL__INFO;

	/**
	 * Default value for {@link #SHOW_ANNOTATION_CODE_MINING_LEVEL}.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_LEVEL__DEFAULT= SHOW_ANNOTATION_CODE_MINING_LEVEL__NONE;

	/**
	 * A named preference that controls how many {@link org.eclipse.jface.text.source.Annotation
	 * Annotations}s should be shown at most as code minings.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 *
	 * @since 3.13
	 */
	public final static String SHOW_ANNOTATION_CODE_MINING_MAX= "showAnnotationAsCodeMiningMax"; //$NON-NLS-1$

	/**
	 * Default value for {@link #SHOW_ANNOTATION_CODE_MINING_MAX}.
	 *
	 * @since 3.13
	 */
	public final static int SHOW_ANNOTATION_CODE_MINING_MAX__DEFAULT= 100;

	/**
	 * Returns the Generic Editor preference store.
	 *
	 * @return the Generic Editor preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return EditorsPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes the given preference store with the default values.
	 *
	 * @param store the preference store to be initialized
	 *
	 * @since 3.13
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(SHOW_ANNOTATION_CODE_MINING_LEVEL, SHOW_ANNOTATION_CODE_MINING_LEVEL__DEFAULT);
		store.setDefault(SHOW_ANNOTATION_CODE_MINING_MAX, SHOW_ANNOTATION_CODE_MINING_MAX__DEFAULT);
	}

}
