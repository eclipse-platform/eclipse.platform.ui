/*
 * Created on Sep 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ant.internal.ui.model;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class AntModelLabelProvider extends org.eclipse.jface.viewers.LabelProvider implements IColorProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object anElement) {
		AntElementNode node = (AntElementNode)anElement;
		return node.getImage();
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object node) {
		AntElementNode element= (AntElementNode) node;
		return element.getLabel();
	}

	public Color getForeground(Object node) {
		if (node instanceof AntTargetNode && ((AntTargetNode)node).isDefaultTarget() ) {
			return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		}
		
		return null;
	}

	public Color getBackground(Object element) {
		return null;
	}
}