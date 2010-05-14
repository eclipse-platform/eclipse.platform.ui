package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

class DragAndDropUtil {

	private Display display;

	public DragAndDropUtil(Display display) {
		this.display = display;
	}

	public CursorInfo getCursorInfo() {
		CursorInfo info = new CursorInfo();
		info.cursorPos = display.getCursorLocation();
		Control curControl = display.getCursorControl();
		if (curControl == null) {
			return info;
		}

		MUIElement curElement = getModelElement(curControl);
		if (curElement instanceof MPlaceholder) {
			info.curElement = ((MPlaceholder) curElement).getRef();
			info.curElementRef = (MPlaceholder) curElement;
		} else {
			info.curElement = curElement;
			info.curElementRef = null;
		}

		if (info.curElement instanceof MElementContainer<?>) {
			Point cursorPos = display.getCursorLocation();
			setItemElement(info, cursorPos);
		}
		return info;
	}

	private void setItemElement(CursorInfo info, Point cursorPos) {
		info.itemIndex = -1;
		info.itemRect = null;

		Control ctrl = (Control) info.curElement.getWidget();

		// KLUDGE!! Should delegate to curElement's renderer
		if (ctrl instanceof CTabFolder) {
			CTabFolder ctf = (CTabFolder) ctrl;
			cursorPos = display.map(null, ctf, cursorPos);
			CTabItem curItem = ctf.getItem(cursorPos);
			if (curItem != null) {
				MUIElement itemElement = (MUIElement) curItem
						.getData(AbstractPartRenderer.OWNING_ME);
				if (itemElement instanceof MPlaceholder) {
					info.itemElement = ((MPlaceholder) itemElement).getRef();
					info.itemElementRef = (MPlaceholder) itemElement;
				} else {
					info.itemElement = itemElement;
					info.itemElementRef = null;
				}

				info.itemIndex = ctf.indexOf(curItem);
				info.itemRect = curItem.getBounds();
				info.itemRect = display.map(ctf, ctf.getShell(), info.itemRect);
			}
		} else if (ctrl instanceof ToolBar) {
			ToolBar tb = (ToolBar) ctrl;
			cursorPos = display.map(null, tb, cursorPos);
			ToolItem curItem = tb.getItem(cursorPos);
			if (curItem != null) {
				info.itemElement = (MUIElement) curItem.getData(AbstractPartRenderer.OWNING_ME);
				ToolItem[] items = tb.getItems();
				for (int i = 0; i < items.length; i++) {
					if (items[i] == curItem) {
						info.itemIndex = i;
						info.itemRect = curItem.getBounds();
						info.itemRect = display.map(tb, tb.getShell(), info.itemRect);
					}
				}
			}
		}
	}

	private MUIElement getModelElement(Control ctrl) {
		if (ctrl == null)
			return null;

		MUIElement element = (MUIElement) ctrl.getData(AbstractPartRenderer.OWNING_ME);
		if (element != null)
			return element;

		return getModelElement(ctrl.getParent());
	}
}
