package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LaunchesViewerFilter extends ViewerFilter {
	private boolean fShowDebug;

	public LaunchesViewerFilter(boolean showDebug) {
		fShowDebug= showDebug;
	}

	/**
	 * Helper method for #select - returns whether the given
	 * launch should be in the viewer.
	 */
	protected boolean showLaunch(ILaunch launch) {
		if (launch == null) {
			return false;
		}
		if (fShowDebug) {
			return launch.getDebugTarget() != null;
		} else {
			IProcess[] processes= launch.getProcesses();
			return processes.length > 0;
		}
	}
	
	/**
	 * @see ViewerFilter
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ILaunch launch= null;
		if (element instanceof ILaunch) {
			return showLaunch((ILaunch)element);

		} else
			if (element instanceof IProcess) {
				launch= ((IProcess)element).getLaunch();
				if (launch == null) {
					return false;
				}
				if (fShowDebug) {
					IDebugTarget target = launch.getDebugTarget();
					return target != null && !element.equals(target.getProcess());
				} else {
					return showLaunch(launch);
				}
			} else
				if (element instanceof IDebugElement) {
					return fShowDebug;
				}
		return false;
	}
}

