package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.ToolBar;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link CTabFolder}.
 * 
 */
public class ToolBarElement extends CompositeElement {
	
	public ToolBarElement(ToolBar toolbar, CSSEngine engine) {
		super(toolbar, engine);
	}

	public ToolBar getToolBar() {
		return (ToolBar)getNativeWidget();
	}
	
	public Node item(int index) {
		return getElement(getToolBar().getItem(index));
	}
	
	public int getLength() {
		return getToolBar().getItemCount();
	}
	
}
