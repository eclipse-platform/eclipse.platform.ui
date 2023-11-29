/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * TestResolutionGenerator is a marker resolution generator for testing
 * {@link org.eclipse.ui.views.markers.WorkbenchMarkerResolution}
 *
 * @since 3.2
 */
public class TestResolutionGenerator implements IMarkerResolutionGenerator2 {

	private static class TestMarkerResolution extends WorkbenchMarkerResolution {

		@Override
		public IMarker[] findOtherMarkers(IMarker[] markers) {
			return markers;
		}

		@Override
		public String getDescription() {
			return "A test of the new style resolution";
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getLabel() {
			return "3.2 Multi resolution";
		}

		@Override
		public void run(IMarker marker) {
			try {
				System.out.println(marker
						.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run(IMarker[] markers, IProgressMonitor monitor) {
			for (IMarker marker : markers) {
				try {
					System.out.println(marker
							.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static class CompatibilityTestMarkerResolution implements
			IMarkerResolution {

		@Override
		public String getLabel() {
			return "3.1 Compatibility Resolution";
		}

		@Override
		public void run(IMarker marker) {
			try {
				System.out.println(marker
						.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE));
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		return true;
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution[] resolutions = new IMarkerResolution[2];

		resolutions[0] = new TestMarkerResolution();
		resolutions[1] = new CompatibilityTestMarkerResolution();

		return resolutions;
	}

}
