/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * TestResolutionGenerator is a marker resolution generator for 
 * testing {@link org.eclipse.ui.views.markers.WorkbenchMarkerResolution}
 * @since 3.2
 *
 */
public class TestResolutionGenerator implements IMarkerResolutionGenerator2 {

	private class TestMarkerResolution extends WorkbenchMarkerResolution{

		String name;
		
		public IMarker[] findOtherMarkers(IMarker[] markers) {
			return markers;
		}
		
		public TestMarkerResolution(String string) {
			name = string;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
		 */
		public String getDescription() {
			return name;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IMarkerResolution2#getImage()
		 */
		public Image getImage() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IMarkerResolution#getLabel()
		 */
		public String getLabel() {
			return name;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
		 */
		public void run(IMarker marker) {
			try {
				System.out.println(marker
						.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE));
			} catch (CoreException e) {
				e.printStackTrace();
			};			
		}
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
	 */
	public boolean hasResolutions(IMarker marker) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		WorkbenchMarkerResolution[] resolutions = new WorkbenchMarkerResolution[2];
		
		resolutions[0] = new TestMarkerResolution("Resolution 1");
		resolutions[1] = new TestMarkerResolution("Resolution 2");
		
		return resolutions;
	}

}
