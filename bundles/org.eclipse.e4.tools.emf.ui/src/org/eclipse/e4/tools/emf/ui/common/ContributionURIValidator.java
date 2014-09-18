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
import org.eclipse.e4.tools.emf.ui.internal.Plugin;
import org.eclipse.emf.common.util.URI;

public class ContributionURIValidator implements IValidator {

	@Override
	public IStatus validate(Object value) {
		if (value == null) {
			return new Status(IStatus.ERROR, Plugin.ID, "The URI must not be empty!");
		} else if (!value.toString().startsWith("bundleclass:")) {
			return new Status(IStatus.ERROR, Plugin.ID, "The URI has to start with 'platform:/plugin'");
		} else {
			try {
				URI uri = URI.createURI(value.toString());
				if (uri.authority() == null || uri.authority().length() == 0 || uri.segmentCount() != 1) {
					return new Status(IStatus.ERROR, Plugin.ID, "The uri has to have the format 'bundleclass://$$bundleId$$/$$className$$'");
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, Plugin.ID, e.getMessage());
			}

		}

		return Status.OK_STATUS;
	}
}