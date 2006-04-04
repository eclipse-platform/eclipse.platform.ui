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
package org.eclipse.core.tests.resources;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * This class works by recording the current state of given markers,
 * then verifying that the marker deltas accurately reflect the old
 * marker state.
 */
public class MarkerAttributeChangeListener extends Assert implements IResourceChangeListener {
	//Map of (Long(id) -> Map of (String(attribute key) -> Object(attribute value)))
	private Map attributeMap = new HashMap();

	//cache the exception because it can't be thrown from a listener
	private AssertionFailedError error;

	public void expectChanges(IMarker marker) throws CoreException {
		expectChanges(new IMarker[] {marker});
	}

	public void expectChanges(IMarker[] markers) throws CoreException {
		error = null;
		attributeMap.clear();

		for (int i = 0; i < markers.length; i++) {
			attributeMap.put(new Long(markers[i].getId()), markers[i].getAttributes());
		}
	}

	/**
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IMarkerDelta[] deltas = event.findMarkerDeltas(null, true);
		try {
			checkDelta(deltas);
		} catch (AssertionFailedError e) {
			error = e;
		}
	}

	private void checkDelta(IMarkerDelta[] deltas) throws AssertionFailedError {
		assertEquals("wrong number of changes", attributeMap.size(), deltas.length);
		for (int i = 0; i < deltas.length; i++) {
			Map values = (Map) attributeMap.get(new Long(deltas[i].getId()));
			assertEquals("Changes different from expecations", deltas[i].getAttributes(), values);
		}
	}

	/**
	 * Verifies that the delta was as expected.  Throws an appropriate
	 * assertion failure if the delta did not meet expectations.
	 */
	public void verifyChanges() {
		if (error != null) {
			throw error;
		}
	}
}
