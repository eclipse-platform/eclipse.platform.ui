package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * An view filter action that filters showing breakpoints based on the model identifier
 * of the selected debug element in the launch view.
 * 
 */
public class ShowBreakpointsForModelAction extends ToggleFilterAction implements IUpdate {

	/**
	 * The filter this action applies to the viewer
	 */
	private BreakpointFilter fBreakpointFilter;

	/**
	 * A viewer filter that selects breakpoints that have
	 * the same model identifier as the selected debug element
	 */
	class BreakpointFilter extends ViewerFilter {
		
		/**
		 * @see ViewerFilter#select(Viewer, Object, Object)
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			List identifiers= new ArrayList(2);
			if (window != null) {
				IViewPart view= window.getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
				if (view != null) {
					IDebugViewAdapter adapter= (IDebugViewAdapter)view.getAdapter(IDebugViewAdapter.class);
					if (adapter != null) {
						Viewer lViewer= adapter.getViewer();
						ISelection selection= lViewer.getSelection();
						if (selection instanceof IStructuredSelection) {
							IStructuredSelection ss= (IStructuredSelection)selection;
							Iterator i= ss.iterator();
							while (i.hasNext()) {
								Object next= i.next();
								if (next instanceof IDebugElement) {
									identifiers.add(((IDebugElement)next).getModelIdentifier());
								} else if (next instanceof ILaunch) {
									IDebugTarget target= ((ILaunch)next).getDebugTarget();
									if (target != null) {
										identifiers.add(target.getModelIdentifier());
									}
								} else if (next instanceof IProcess) {
									IDebugTarget target= ((IProcess)next).getLaunch().getDebugTarget();
									if (target != null) {
										identifiers.add(target.getModelIdentifier());
									}
								}	
							}
						}
					}
				}
			}
			IBreakpoint breakpoint= (IBreakpoint)element;
			return identifiers.contains(breakpoint.getModelIdentifier());
		}

	}

	public ShowBreakpointsForModelAction(StructuredViewer viewer) {
		super();
		setText(ActionMessages.getString("ShowBreakpointsForModelAction.Show_For_Selected")); //$NON-NLS-1$
		setViewerFilter(new BreakpointFilter());
		setViewer(viewer);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET));
		setChecked(false);
		setId(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".ShowBreakpointsForModelAction"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.SHOW_BREAKPOINTS_FOR_MODEL_ACTION });
		IViewPart part= DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part != null) {
			IDebugViewAdapter adapter= (IDebugViewAdapter)part.getAdapter(IDebugViewAdapter.class);
			adapter.setAction(getClass().getName(), this);
		}
	}

	/** 
	 * This action listens for selection changes in the <code>LaunchView</code>
	 * 
	 * @see AbstractDebugView#updateActions()
	 */
	public void update() {
		//selection has changed in the debug view
		//need to reapply the filter.
		getViewer().refresh();
	}
	/**
	 * @see ToggleFilterAction#getViewerFilter()
	 */
	protected ViewerFilter getViewerFilter() {
		return fBreakpointFilter;
	}

	protected void setViewerFilter(BreakpointFilter filter) {
		fBreakpointFilter= filter;
	}
	/**
	 * @see ToggleFilterAction#getShowText()
	 */
	protected String getShowText() {
		return ActionMessages.getString("ShowBreakpointsForModelAction.Show_All_Breakpoints_2"); //$NON-NLS-1$
	}

	/**
	 * @see ToggleFilterAction#getHideText()
	 */
	protected String getHideText() {
		return ActionMessages.getString("ShowBreakpointsForModelAction.Only_Show_Breakpoints_Applicable_to_Selected_Debug_Element_3"); //$NON-NLS-1$
	}
}
