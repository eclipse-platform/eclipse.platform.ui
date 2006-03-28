package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class TestLabelDecorator implements ILabelDecorator {
	
	/**
	 * 
	 */
	public TestLabelDecorator() {
		
	}

	public Image decorateImage(Image image, Object element) { 
		return null;
	}

	public String decorateText(String text, Object element) { 
		if(element instanceof TestExtensionTreeData) {
			
			if(text != null && text.endsWith("3")) {
				return ">>" + text;
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
