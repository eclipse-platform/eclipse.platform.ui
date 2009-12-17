/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * Creates a new exception object. Note that as of 3.5, this constructor
	 * expects exactly one exception object in the given array, with all other
	 * array positions being <code>null</code>. The restriction may be lifted in
	 * the future, and clients of this class must not make this assumption.
	 * 
	 * @param references
	 * @param exceptions
	 */
	public MultiPartInitException(IWorkbenchPartReference[] references,
			PartInitException[] exceptions) {
		super(exceptions[findSingleException(exceptions)].getStatus());
		this.references = references;
		this.exceptions = exceptions;
	}

	/**
	 * Returns an array of part references, containing references of parts that
	 * were intialized correctly. Any number of elements of the returned array
	 * may have a <code>null</code> value.
	 * 
	 * @return the part reference array
	 */
	public IWorkbenchPartReference[] getReferences() {
		return references;
	}

	/**
	 * Returns an array of exceptions, corresponding to parts that could not be
	 * intialized correctly. At least one element of the returned array will
	 * have a non-<code>null</code> value.
	 * 
	 * @return the exception array
	 */
	public PartInitException[] getExceptions() {
		return exceptions;
	}

	private static int findSingleException(PartInitException[] exceptions) {
		int index = -1;
		for (int i = 0; i < exceptions.length; i++) {
			if (exceptions[i] != null) {
				if (index == -1) {
					index = i;
				} else
					throw new IllegalArgumentException();
			}
		}
		if (index == -1) {
			throw new IllegalArgumentException();
		}
		return index;
	}

	private static final long serialVersionUID = -9138185942975165490L;

}
