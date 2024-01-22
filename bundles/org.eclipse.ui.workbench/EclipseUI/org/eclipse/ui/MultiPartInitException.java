/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
package org.eclipse.ui;

/**
 * A checked exception indicating one or more workbench parts could not be
 * initialized correctly. The message text provides a further description of the
 * problem.
 *
 * @since 3.5
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MultiPartInitException extends WorkbenchException {

	private IWorkbenchPartReference[] references;
	private PartInitException[] exceptions;

	/**
	 * Creates a new exception object. Note that as of 3.5, this constructor expects
	 * exactly one exception object in the given array, with all other array
	 * positions being <code>null</code>. The restriction may be lifted in the
	 * future, and clients of this class must not make this assumption.
	 *
	 * @param references the parts in error
	 * @param exceptions the exceptions
	 */
	public MultiPartInitException(IWorkbenchPartReference[] references, PartInitException[] exceptions) {
		super(exceptions[findFirstException(exceptions)].getStatus());
		this.references = references;
		this.exceptions = exceptions;
	}

	/**
	 * Returns an array of part references, containing references of parts that were
	 * intialized correctly. Any number of elements of the returned array may have a
	 * <code>null</code> value.
	 *
	 * @return the part reference array
	 */
	public IWorkbenchPartReference[] getReferences() {
		return references;
	}

	/**
	 * Returns an array of exceptions, corresponding to parts that could not be
	 * intialized correctly. At least one element of the returned array will have a
	 * non-<code>null</code> value.
	 *
	 * @return the exception array
	 */
	public PartInitException[] getExceptions() {
		return exceptions;
	}

	private static int findFirstException(PartInitException[] exceptions) {
		for (int i = 0; i < exceptions.length; i++) {
			if (exceptions[i] != null)
				return i;
		}
		throw new IllegalArgumentException();
	}

	private static final long serialVersionUID = -9138185942975165490L;

}
