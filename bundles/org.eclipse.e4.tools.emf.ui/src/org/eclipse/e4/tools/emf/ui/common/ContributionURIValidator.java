/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.emf.common.util.URI;

public class ContributionURIValidator implements IValidator {

	@Override
	public IStatus validate(Object value) {
		if (value == null) {
			return new Status(IStatus.ERROR, Plugin.ID, Messages.ContributionURIValidator_No_Empty_URI);
		} else if (!value.toString().startsWith("bundleclass:")) { //$NON-NLS-1$
			return new Status(IStatus.ERROR, Plugin.ID, Messages.ContributionURIValidator_URI_starts_with_platform);
		} else {
			try {
				URI uri = URI.createURI(value.toString());
				if (uri.authority() == null || uri.authority().length() == 0 || uri.segmentCount() != 1) {
					return new Status(IStatus.ERROR, Plugin.ID, Messages.ContributionURIValidator_Malformed_URI);
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, Plugin.ID, e.getMessage());
			}

		}

		return Status.OK_STATUS;
	}
}