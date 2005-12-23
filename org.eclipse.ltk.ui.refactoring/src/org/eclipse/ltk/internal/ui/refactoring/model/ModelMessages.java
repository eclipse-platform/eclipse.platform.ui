/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.osgi.util.NLS;

public final class ModelMessages extends NLS {

	private static final String BUNDLE_NAME= "org.eclipse.ltk.internal.ui.refactoring.model.Modelmessages"; //$NON-NLS-1$

	public static String RefactoringDescriptorCompareInput_pending_refactoring;

	public static String RefactoringDescriptorDiffNode_diff_string;

	public static String RefactoringDescriptorViewer_breaking_change_message;

	public static String RefactoringDescriptorViewer_closure_change_message;

	public static String RefactoringDescriptorViewer_structural_change_message;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ModelMessages.class);
	}

	private ModelMessages() {
	}
}