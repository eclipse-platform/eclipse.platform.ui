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
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.Assert;

/**
 * A generic rename refactoring. The actual refactoring is done
 * by the rename processor passed to the constructor.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RenameRefactoring extends ProcessorBasedRefactoring {

	private RenameProcessor fProcessor;

	/**
	 * Creates a new rename refactoring with the given rename processor.
	 *
	 * @param processor the rename processor
	 */
	public RenameRefactoring(RenameProcessor processor) {
		super(processor);
		Assert.isNotNull(processor);
		fProcessor= processor;
	}

	@Override
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}
}
