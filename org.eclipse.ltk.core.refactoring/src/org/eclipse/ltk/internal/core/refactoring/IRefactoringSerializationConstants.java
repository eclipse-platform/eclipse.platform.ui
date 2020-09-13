/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

/**
 * Interface for constants related to refactoring serialization.
 *
 * @since 3.2
 */
public interface IRefactoringSerializationConstants {

	/** The comment attribute */
	String ATTRIBUTE_COMMENT= "comment"; //$NON-NLS-1$

	/** The description attribute */
	String ATTRIBUTE_DESCRIPTION= "description"; //$NON-NLS-1$

	/** The flags attribute */
	String ATTRIBUTE_FLAGS= "flags"; //$NON-NLS-1$

	/** The id attribute */
	String ATTRIBUTE_ID= "id"; //$NON-NLS-1$

	/** The project attribute */
	String ATTRIBUTE_PROJECT= "project"; //$NON-NLS-1$

	/** The time stamp attribute */
	String ATTRIBUTE_STAMP= "stamp"; //$NON-NLS-1$

	/** The version attribute */
	String ATTRIBUTE_VERSION= "version"; //$NON-NLS-1$

	/** The current version tag */
	String CURRENT_VERSION= RefactoringSessionDescriptor.VERSION_1_0;

	/** The refactoring element */
	String ELEMENT_REFACTORING= "refactoring"; //$NON-NLS-1$

	/** The session element */
	String ELEMENT_SESSION= "session"; //$NON-NLS-1$

	/** The output encoding */
	String OUTPUT_ENCODING= "utf-8"; //$NON-NLS-1$

	/** The indent flag */
	String OUTPUT_INDENT= "yes"; //$NON-NLS-1$

	/** The output method */
	String OUTPUT_METHOD= "xml"; //$NON-NLS-1$
}
