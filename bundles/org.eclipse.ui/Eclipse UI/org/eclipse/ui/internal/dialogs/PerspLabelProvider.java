package org.eclipse.ui.internal.dialogs;

import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;

public class PerspLabelProvider extends LabelProvider {
public String getText(Object element) {
	if (element instanceof IPerspectiveDescriptor) {
		IPerspectiveDescriptor desc = (IPerspectiveDescriptor)element;
		String label = desc.getLabel();
		String def = PlatformUI.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
		if (desc.getId().equals(def))
			label += " (default)";
		return label;
	}
	return "Unknown Element Type";
}
}
