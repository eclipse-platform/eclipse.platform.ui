package org.eclipse.ui.examples.readmetool;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * Creates resolutions for readme markers.
 */
public class ReadmeMarkerResolutionGenerator implements IMarkerResolutionGenerator {
	/*(non-Javadoc)
	 * Method declared on IMarkerResolutionGenerator.
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] {new AddSentenceResolution()};
	}
}
