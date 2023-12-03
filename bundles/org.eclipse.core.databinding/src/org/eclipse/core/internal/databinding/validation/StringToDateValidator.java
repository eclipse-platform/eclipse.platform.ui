/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
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
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix for 217940
 *******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import java.util.Date;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.conversion.DateConversionSupport;
import org.eclipse.core.internal.databinding.conversion.StringToDateConverter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 */
public class StringToDateValidator implements IValidator<String> {
	private final StringToDateConverter converter;

	public StringToDateValidator(StringToDateConverter converter) {
		this.converter = converter;
	}

	@Override
	public IStatus validate(String value) {
		if (value.trim().isEmpty()) {
			return Status.OK_STATUS;
		}
		Date convertedValue = converter.convert(value);
		// The StringToDateConverter returns null if it can't parse the date.
		if (convertedValue == null) {
			return ValidationStatus.error(getErrorMessage());
		}

		return Status.OK_STATUS;
	}

	protected String getErrorMessage() {
		Date sampleDate = new Date();

		// FIXME We need to use the information from the
		// converter, not use another instance of DateConversionSupport.
		FormatUtil util = new FormatUtil();
		StringBuilder samples = new StringBuilder();
		for (int formatterIdx = 1; formatterIdx < util.numFormatters() - 2; formatterIdx++) {
			samples.append('\'');
			samples.append(util.format(sampleDate, formatterIdx));
			samples.append("', "); //$NON-NLS-1$
		}
		samples.append('\'');
		samples.append(util.format(sampleDate, 0));
		samples.append('\'');
		return BindingMessages.getString(BindingMessages.EXAMPLES)
				+ ": " + samples + ",..."; //$NON-NLS-1$//$NON-NLS-2$
	}

	private static class FormatUtil extends DateConversionSupport {
		@Override
		protected int numFormatters() {
			return super.numFormatters();
		}

		@Override
		protected String format(Date date) {
			return super.format(date);
		}

		@Override
		protected String format(Date date, int formatterIdx) {
			return super.format(date, formatterIdx);
		}
	}
}
