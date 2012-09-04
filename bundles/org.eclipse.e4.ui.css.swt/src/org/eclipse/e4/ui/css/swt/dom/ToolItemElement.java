package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link TableItem}.
 * 
 */
public class ToolItemElement extends ItemElement {

	public ToolItemElement(ToolItem toolItem, CSSEngine engine) {
		super(toolItem, engine);
	}

	public ToolItem getToolItem() {
		return (ToolItem) getNativeWidget();
	}

	public Node getParentNode() {
		ToolItem item = getToolItem();
		ToolBar parent = item.getParent();
		if (parent != null) {
			Element element = getElement(parent);
			return element;
		}
		return null;
	}

	public Node item(int index) {
		ToolItem item = getToolItem();
		if ((item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR) {
			Object control = item.getControl();
			if (control != null) {
				return getElement(control);
			}
		}
		return null;
	}

	public int getLength() {
		ToolItem item = getToolItem();
		return (item.getStyle() & SWT.SEPARATOR) == SWT.SEPARATOR
				&& item.getControl() != null ? 1 : 0;
	}
}
