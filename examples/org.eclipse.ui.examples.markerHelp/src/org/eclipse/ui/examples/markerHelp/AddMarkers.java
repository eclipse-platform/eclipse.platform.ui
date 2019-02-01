package org.eclipse.ui.examples.markerHelp;




import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Handler to add the errors for demonstration to the workspace.
 */
public class AddMarkers extends AbstractHandler{

	private static final String MARKER_TYPE = "org.eclipse.ui.examples.markerHelp.custom_marker"; //$NON-NLS-1$
	private static final String ATT_CODE = "problemCode"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			createMarkers();
		} catch (CoreException e) {
			throw new ExecutionException("Failed to create markers.", e); //$NON-NLS-1$
		}
		return null;
	}

	private void createMarkers() throws CoreException {
		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();

		IMarker custom1 = wsRoot.createMarker(MARKER_TYPE);
		IMarker custom2 = wsRoot.createMarker(MARKER_TYPE);
		IMarker custom3 = wsRoot.createMarker(MARKER_TYPE);
		IMarker custom4 = wsRoot.createMarker(MARKER_TYPE);

		custom1.setAttribute(ATT_CODE, "1"); //$NON-NLS-1$
		custom2.setAttribute(ATT_CODE, "2"); //$NON-NLS-1$
		custom3.setAttribute(ATT_CODE, "3"); //$NON-NLS-1$
		custom4.setAttribute(ATT_CODE, "4"); //$NON-NLS-1$
	}
}