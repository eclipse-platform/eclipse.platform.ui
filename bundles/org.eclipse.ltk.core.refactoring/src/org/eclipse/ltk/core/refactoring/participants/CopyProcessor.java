/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring.participants;

/**
 * A special processor that performs copy operations. A copy processor is
 * responsible for actually copying elements to a shared clipboard.
 * <p>
 * This class should be subclassed by clients wishing to provide a special copy
 * processor.
 * </p>
 * <p>
 * The main purpose of this class is type safety for the generic copy
 * refactoring
 * </p>
 *
 * @since 3.1
 */
public abstract class CopyProcessor extends RefactoringProcessor {

}
