package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action that toggles the state of its viewer to
 * show/hide qualified names.
 */
public class ShowQualifiedAction extends Action {

	private StructuredViewer fViewer;

	public ShowQualifiedAction(StructuredViewer viewer) {
		super(DebugUIMessages.getString("ShowQualifiedAction.Show_&Qualified_Names_1")); //$NON-NLS-1$
		setViewer(viewer);
		setToolTipText(DebugUIMessages.getString("ShowQualifiedAction.Show_&Qualified_Names_1")); //$NON-NLS-1$
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_QUALIFIED_NAMES));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_QUALIFIED_NAMES));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_QUALIFIED_NAMES));
		setId(DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".ShowQualifedAction"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.SHOW_QUALIFIED_NAMES_ACTION });
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
			debugLabelProvider.setAttribute(IDebugModelPresentation.DISPLAY_QUALIFIED_NAMES, (on ? Boolean.TRUE : Boolean.FALSE));
			BusyIndicator.showWhile(getViewer().getControl().getDisplay(), new Runnable() {
				public void run() {
					getViewer().refresh();					
				}
			});
		}
		setToolTipText(on ? DebugUIMessages.getString("ShowQualifiedAction.Hide_&Qualified_Names_3") : DebugUIMessages.getString("ShowQualifiedAction.Show_&Qualified_Names_1")); //$NON-NLS-2$ //$NON-NLS-1$

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

