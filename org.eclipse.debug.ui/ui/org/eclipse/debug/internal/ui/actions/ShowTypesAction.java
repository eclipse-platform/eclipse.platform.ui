package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action that toggles the state of a viewer to
 * show/hide type names of variables.
 * Only viewers that use a <code>VariableLabelProvider</code> to render its
 * elements are effected.
 */
public class ShowTypesAction extends Action {

	private StructuredViewer fViewer;

	public ShowTypesAction(StructuredViewer viewer) {
		super(ActionMessages.getString("ShowTypesAction.Show_&Type_Names_1")); //$NON-NLS-1$
		setViewer(viewer);
		setToolTipText(ActionMessages.getString("ShowTypesAction.Show_Type_Names")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_TYPE_NAMES));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TYPE_NAMES));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TYPE_NAMES));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".ShowTypesAction"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SHOW_TYPES_ACTION);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		valueChanged(isChecked());
	}

	private void valueChanged(boolean on) {
		if (getViewer().getControl().isDisposed()) {
			return;
		}
		ILabelProvider labelProvider= (ILabelProvider)getViewer().getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, (on ? Boolean.TRUE : Boolean.FALSE));			
			BusyIndicator.showWhile(getViewer().getControl().getDisplay(), new Runnable() {
				public void run() {
					getViewer().refresh();					
				}
			});
		}
	}

	/**
	 * @see Action#setChecked(boolean)
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		valueChanged(value);
	}
	
	protected StructuredViewer getViewer() {
		return fViewer;
	}

	protected void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}
}


