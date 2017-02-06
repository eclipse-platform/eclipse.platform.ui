/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests.contributions;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IMarker;

import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {

	public static final String FIXME= MarkerResolutionGenerator.class.getName() + ".fixme";
	
	private static class MarkerResolution implements IMarkerResolution2 {
		@Override
		public String getDescription() {
			return "resolution.description";
		}
		@Override
		public Image getImage() {
			return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
		}
		@Override
		public String getLabel() {
			return FIXME;
		}

		@Override
		public void run(IMarker marker) {
			try {
				marker.delete();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		if (marker.getAttribute(FIXME, false)) {
			return new IMarkerResolution[] {
				new MarkerResolution()
			};
		}
		return new IMarkerResolution[0];
	}

}
