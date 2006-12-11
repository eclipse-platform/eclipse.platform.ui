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

import java.util.Date;

import org.eclipse.core.databinding.conversion.DateConversionSupport;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * DateValidator. An IValidator implementation for dates.
 */
public class String2DateValidator extends DateConversionSupport implements
		IValidator {

	public IStatus validate(Object value) {
		return parse((String) value) != null ? Status.OK_STATUS
				: ValidationStatus.error(getHint());
	}

	private String getHint() {
		Date sampleDate = new Date();
		StringBuffer samples = new StringBuffer();
		for (int formatterIdx = 1; formatterIdx < numFormatters() - 2; formatterIdx++) {
			samples.append('\'');
			samples.append(format(sampleDate, formatterIdx));
			samples.append("', "); //$NON-NLS-1$
		}
		samples.append('\'');
		samples.append(format(sampleDate, 0));
		samples.append('\'');
		return BindingMessages.getString("Examples") + ": " + samples + ",..."; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}
