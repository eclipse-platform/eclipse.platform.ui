package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.internal.WorkbenchMessages;

public class PerspLabelProvider extends LabelProvider {
public String getText(Object element) {
	if (element instanceof IPerspectiveDescriptor) {
		IPerspectiveDescriptor desc = (IPerspectiveDescriptor)element;
		String label = desc.getLabel();
		String def = PlatformUI.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
		if (desc.getId().equals(def))
			label = WorkbenchMessages.format("PerspectivesPreference.defaultLabel", new Object[] {label}); //$NON-NLS-1$
		return label;
	}
	return WorkbenchMessages.getString("PerspectiveLabelProvider.unknown"); //$NON-NLS-1$
}
}
