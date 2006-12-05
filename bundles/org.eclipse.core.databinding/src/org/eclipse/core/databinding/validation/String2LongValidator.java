/*
 * Copyright (C) 2005, 2006 db4objects Inc. (http://www.db4o.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 *     Boris Bokowski (IBM Corporation) - bug 118429
 */
package org.eclipse.core.databinding.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * IntegerValidator. Validate String to Long data input
 */
public class String2LongValidator extends String2LongPrimitiveValidator {

	public IStatus validate(Object value) {
		if ("".equals(value)) { //$NON-NLS-1$
			return Status.OK_STATUS;
		}
		return super.validate(value);
	}
}
