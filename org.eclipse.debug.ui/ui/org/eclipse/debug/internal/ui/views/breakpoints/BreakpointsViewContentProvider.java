package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BreakpointsViewContentProvider implements IStructuredContentProvider {

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return ((IBreakpointManager) parent).getBreakpoints();
		}
		
		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}
	
		/**
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
