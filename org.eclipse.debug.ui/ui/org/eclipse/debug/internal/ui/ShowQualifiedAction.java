package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugModelPresentation;import org.eclipse.jface.action.Action;import org.eclipse.jface.viewers.ILabelProvider;import org.eclipse.jface.viewers.StructuredViewer;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action that toggles the state of its viewer to
 * show/hide qualified names.
 */
public class ShowQualifiedAction extends Action {

	private static final String PREFIX= "show_qualified_action.";
	private static final String SHOW= PREFIX + TOOL_TIP_TEXT + ".show";
	private static final String HIDE= PREFIX + TOOL_TIP_TEXT + ".hide";

	protected StructuredViewer fViewer;

	public ShowQualifiedAction(StructuredViewer viewer) {
		super(DebugUIUtils.getResourceString(SHOW));
		fViewer= viewer;
		setToolTipText(DebugUIUtils.getResourceString(SHOW));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.SHOW_QUALIFIED_NAMES_ACTION });
	}

	/**
	 * @see Action
	 */
	public void run() {
		valueChanged(isChecked());
	}

	private void valueChanged(boolean on) {
		if (fViewer.getControl().isDisposed()) {
			return;
		}		
		ILabelProvider labelProvider= (ILabelProvider)fViewer.getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute(IDebugModelPresentation.DISPLAY_QUALIFIED_NAMES, (on ? Boolean.TRUE : Boolean.FALSE));
			BusyIndicator.showWhile(fViewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					fViewer.refresh();					
				}
			});
		}
		setToolTipText(on ? DebugUIUtils.getResourceString(HIDE) : DebugUIUtils.getResourceString(SHOW));

	}

	/**
	 * @see Action
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		valueChanged(value);
	}
}

