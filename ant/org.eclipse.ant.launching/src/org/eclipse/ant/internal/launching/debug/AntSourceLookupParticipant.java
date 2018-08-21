/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
package org.eclipse.ant.internal.launching.debug;

import org.eclipse.ant.internal.launching.debug.model.AntStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;

/**
 * The Ant source lookup participant knows how to translate a Ant stack frame into a source file name
 */
public class AntSourceLookupParticipant extends AbstractSourceLookupParticipant {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
	@Override
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof AntStackFrame) {
			return ((AntStackFrame) object).getFilePath();
		}
		if (object instanceof String) {
			// assume it's a file name
			return (String) object;
		}
		return null;
	}
}
