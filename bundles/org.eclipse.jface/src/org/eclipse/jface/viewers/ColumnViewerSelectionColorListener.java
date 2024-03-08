package org.eclipse.jface.viewers;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;

/**
 * EraseItem event listener that takes care of coloring the selection color of
 * column viewers. The coloring is only applied if no other erase item listener
 * is registered for the viewer. If other erase item listeners are registers,
 * most probably a other customer coloring is applied and should not be
 * overwritten.
 *
 * @since 3.4
 * @see FocusCellOwnerDrawHighlighter
 */
public class ColumnViewerSelectionColorListener implements Listener {

	/**
	 * Registers an erase item event listener that takes care of coloring the
	 * selection color of the given viewer.
	 *
	 * @param viewer The viewer that should be colored
	 */
	public static void addListenerToViewer(StructuredViewer viewer) {
		viewer.getControl().addListener(SWT.EraseItem, new ColumnViewerSelectionColorListener());
	}

	@Override
	public void handleEvent(Event event) {
		if ((event.detail & SWT.SELECTED) == 0) {
			return; /* item not selected */
		}

		Listener[] eraseItemListeners = event.widget.getListeners(SWT.EraseItem);
		if (eraseItemListeners.length != 1) {
			return; /* other eraseItemListener exists, do not apply coloring */
		}

		GC gc = event.gc;
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		if (event.widget instanceof Control control && control.isFocusControl()) {
			gc.setBackground(colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND")); //$NON-NLS-1$
			gc.setForeground(colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND")); //$NON-NLS-1$
		} else {
			gc.setBackground(colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND_NO_FOCUS")); //$NON-NLS-1$
			gc.setForeground(colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND_NO_FOCUS")); //$NON-NLS-1$
		}

		int width = event.width;
		if (event.widget instanceof Scrollable scrollable) {
			width = scrollable.getClientArea().width;
		}

		gc.fillRectangle(0, event.y, width, event.height);

		event.detail &= ~SWT.SELECTED;
	}

}
