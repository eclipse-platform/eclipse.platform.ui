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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An view filter action that filters showing breakpoints based on the model identifier
 * of the selected debug element in the launch view.
 * 
 */
public class ShowBreakpointsForModelAction extends ToggleFilterAction implements ISelectionListener {

	/**
	 * The view associated with this action
	 */
	private IViewPart fView;
	
	/**
	 * The list of identifiers for the current state
	 */
	private List fIdentifiers= new ArrayList(2);
	
	/**
	 * A viewer filter that selects breakpoints that have
	 * the same model identifier as the selected debug element
	 */
	class BreakpointFilter extends ViewerFilter {
		
		/**
		 * @see ViewerFilter#select(Viewer, Object, Object)
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			IBreakpoint breakpoint= (IBreakpoint)element;
			return fIdentifiers.contains(breakpoint.getModelIdentifier());
		}

	}

	public ShowBreakpointsForModelAction(StructuredViewer viewer, IViewPart view) {
		super();
		setText(ActionMessages.getString("ShowBreakpointsForModelAction.Show_For_Selected")); //$NON-NLS-1$
		setToolTipText(getHideText());
		setViewerFilter(new BreakpointFilter());
		setViewer(viewer);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET));
		setChecked(false);
		setId(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".ShowBreakpointsForModelAction"); //$NON-NLS-1$
		
		// listen to selection changes in the debug view
		view.getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		setView(view);
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.SHOW_BREAKPOINTS_FOR_MODEL_ACTION);
		
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
	
	public void dispose() {
		getView().getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
	}
	
	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection)selection;
			List identifiers= getIdentifiers(ss);
			if (!isChecked()) {
				fIdentifiers= identifiers;
				return;
			}
			if (identifiers.isEmpty()) {
				 if(fIdentifiers.isEmpty()) {
					return;
				 } else {
				 	reapplyFilters(identifiers);
				 	return;
				 }
			}
			if (fIdentifiers.isEmpty()) {
				reapplyFilters(identifiers);
				return;
			}
			
			if (identifiers.size() == fIdentifiers.size()) {
				List copy= new ArrayList(identifiers.size());
				Iterator iter= fIdentifiers.iterator();
				while (iter.hasNext()) {
					String element = (String) iter.next();
					Iterator newIdentifiers= identifiers.iterator();
					while (newIdentifiers.hasNext()) {
						String newId= (String)newIdentifiers.next();
						copy.add(newId);
						if (element.equals(newId)) {
							newIdentifiers.remove();
						}
					}
				}
				//check for real change
				if (identifiers.isEmpty()) {
					return;
				}
				reapplyFilters(copy);
			} 
		}
	}

	
	/**
	 * Selection has changed in the debug view
	 * need to reapply the filter.
	 */
	protected void reapplyFilters(List identifiers) {
		fIdentifiers= identifiers;		
		getViewer().refresh();
	}
	
	protected IViewPart getView() {
		return fView;
	}

	protected void setView(IViewPart view) {
		fView = view;
	}
	
	protected List getIdentifiers(IStructuredSelection ss) {
		List identifiers= new ArrayList(2);
		Iterator i= ss.iterator();
		while (i.hasNext()) {
			Object next= i.next();
			if (next instanceof IDebugElement) {
				identifiers.add(((IDebugElement)next).getModelIdentifier());
			} else if (next instanceof ILaunch) {
				IDebugTarget[] targets= ((ILaunch)next).getDebugTargets();
				for (int j = 0; j < targets.length; j++) {
					identifiers.add(targets[j].getModelIdentifier());
				}
			} else if (next instanceof IProcess) {
				IDebugTarget target= (IDebugTarget)((IProcess)next).getAdapter(IDebugTarget.class);
				if (target != null) {
					identifiers.add(target.getModelIdentifier());
				}
			}	
		}
		return identifiers;
	}
	
}
