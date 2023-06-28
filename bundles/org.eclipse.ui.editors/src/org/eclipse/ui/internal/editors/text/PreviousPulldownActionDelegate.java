/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.ui.texteditor.AnnotationPreference;

/**
 * The previous pulldown action delegate.
 *
 * @since 3.0
 */
public class PreviousPulldownActionDelegate extends NextPreviousPulldownActionDelegate {

	@Override
public String getPreferenceKey(AnnotationPreference annotationPreference) {
		return annotationPreference.getIsGoToPreviousNavigationTargetKey();
	}

}
