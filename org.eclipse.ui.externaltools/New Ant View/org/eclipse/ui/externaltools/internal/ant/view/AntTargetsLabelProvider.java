package org.eclipse.ui.externaltools.internal.ant.view;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

public class AntTargetsLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		if (element instanceof TargetNode) {
			return ExternalToolsImages.getImage(IExternalToolsUIConstants.IMG_ANT_TARGET);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof TargetNode) {
			StringBuffer buffer= new StringBuffer(element.toString());
			buffer.append(" [").append(((TargetNode) element).getParent().getName()).append("]");
			return buffer.toString();
		}
		return element.toString();
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
