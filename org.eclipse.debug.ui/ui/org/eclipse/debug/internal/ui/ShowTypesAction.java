package org.eclipse.debug.internal.ui;


/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * An action that toggles the state of a viewer to
 * show/hide type names of variables.
 * Only viewers that use a <code>VariableLabelProvider</code> to render its
 * elements are effected.
 */
public class ShowTypesAction extends Action {

	private static final String PREFIX= "show_types_action.";
	private static final String SHOW= PREFIX + TOOL_TIP_TEXT + ".show";
	private static final String HIDE= PREFIX + TOOL_TIP_TEXT + ".hide";

	protected StructuredViewer fViewer;

	public ShowTypesAction(StructuredViewer viewer) {
		super(DebugUIUtils.getResourceString(SHOW));
		fViewer= viewer;
		setToolTipText(SHOW);
	}

	/**
	 * @see Action
	 */
	public void run() {
		valueChanged(isChecked());
	}

	private void valueChanged(boolean on) {
		ILabelProvider labelProvider= (ILabelProvider)fViewer.getLabelProvider();
		if (labelProvider instanceof IDebugModelPresentation) {
			IDebugModelPresentation debugLabelProvider= (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, (on ? Boolean.TRUE : Boolean.FALSE));
			fViewer.refresh();
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


