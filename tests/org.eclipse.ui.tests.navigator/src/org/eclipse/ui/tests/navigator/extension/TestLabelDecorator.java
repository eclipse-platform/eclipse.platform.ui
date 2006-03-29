package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class TestLabelDecorator implements ILabelDecorator {
	
	/**
	 * 
	 */
	public TestLabelDecorator() {
		
	}

	public Image decorateImage(Image image, Object element) { 
		if(element != null && element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			if(data.getName().endsWith("3")) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJS_INFO_TSK);
			}
		}
		return null;
	}

	public String decorateText(String text, Object element) { 
		if(element instanceof TestExtensionTreeData) {
			
			if(text != null && text.endsWith("3")) {
				return "x " + text + " x";
			}
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// no-op

	}

	public void dispose() {
		// no-op

	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
		// no-op

	}

}
