/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
public class TestResolutionGenerator implements IMarkerResolutionGenerator2 {

	private class TestMarkerResolution extends WorkbenchMarkerResolution {

		@Override
		public IMarker[] findOtherMarkers(IMarker[] markers) {
			return markers;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
		 */
		@Override
		public String getDescription() {
			return "A test of the new style resolution";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution2#getImage()
		 */
		@Override
		public Image getImage() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution#getLabel()
		 */
		@Override
		public String getLabel() {
			return "3.2 Multi resolution";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
		 */
		@Override
		public void run(IMarker marker) {
			try {
				System.out.println(marker
						.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.views.markers.WorkbenchMarkerResolution#run(org.eclipse.core.resources.IMarker[],
		 *      org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		public void run(IMarker[] markers, IProgressMonitor monitor) {
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];

				try {
					System.out.println(marker
							.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class CompatibilityTestMarkerResolution implements
			IMarkerResolution {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution#getLabel()
		 */
		@Override
		public String getLabel() {
			return "3.1 Compatibility Resolution";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
		 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public boolean hasResolutions(IMarker marker) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		IMarkerResolution[] resolutions = new IMarkerResolution[2];

		resolutions[0] = new TestMarkerResolution();
		resolutions[1] = new CompatibilityTestMarkerResolution();

		return resolutions;
	}

}
