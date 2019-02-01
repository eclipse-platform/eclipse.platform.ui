package org.eclipse.ui.examples.markerHelp;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerHelpContextProvider;

/**
 * A example help context provider
 */
public class ExampleHelpContextProvider implements IMarkerHelpContextProvider {

	private static final String ATT_CODE = "problemCode"; //$NON-NLS-1$
	private static final String HELP_CONTEXT_PREFIX = "org.eclipse.ui.examples.markerHelp.markerHelpProblem"; //$NON-NLS-1$

	@Override
	public String getHelpContextForMarker(IMarker marker) {
		try {
			return HELP_CONTEXT_PREFIX + marker.getAttribute(ATT_CODE).toString();
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean hasHelpContextForMarker(IMarker marker) {
		return true;
	}

}
