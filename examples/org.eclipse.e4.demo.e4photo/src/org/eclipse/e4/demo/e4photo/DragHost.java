package org.eclipse.e4.demo.e4photo;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DragHost {
	MUIElement dragElement;
	MElementContainer<MUIElement> curParent;
	MWindow baseWindow;
	
	int curIndex;
	private Shell dndShell;
	private MElementContainer<MUIElement> oldParent;
	
	public DragHost(MUIElement element) {
		assert(dragElement != null);
		
		dragElement = element;
		curParent = dragElement.getParent();
		curIndex = curParent.getChildren().indexOf(element);
		
		baseWindow = getWindow();
		assert(baseWindow != null && baseWindow.getWidget() != null);
		
		attach();
	}

	public Shell getShell() {
		return dndShell;
	}
	
	private MWindow getWindow() {
		MUIElement pe = curParent;
		while (pe != null && !(pe instanceof MApplication)) {
			if (((Object)pe) instanceof MWindow)
				return (MWindow) pe;
			pe = pe.getParent();
		}
		
		return null;
	}

	private void attach() {
		oldParent = dragElement.getParent();
	
		dragElement.getParent().getChildren().remove(dragElement);
		
		Control baseControl = (Control) baseWindow.getWidget();
		dndShell = new Shell(baseControl.getShell(), SWT.BORDER);
		dndShell.setAlpha(100);
		dndShell.setLayout(new FillLayout());

		dndShell.setSize(400, 300);
		if (dragElement.getWidget() instanceof Control) {
			Control ctrl = (Control) dragElement.getWidget();
			dndShell.setSize(ctrl.getSize());
		}
		
		Point cp = dndShell.getDisplay().getCursorLocation();
		dndShell.setLocation(cp.x+20, cp.y+20);
		
		IPresentationEngine renderer = (IPresentationEngine) baseWindow.getContext().get(IPresentationEngine.class.getName());
		renderer.createGui(getRenderingModel(), dndShell);
		
		dndShell.layout(dndShell.getChildren(), SWT.DEFER);
		dndShell.setVisible(true);
	}

	private MUIElement getRenderingModel() {
		if (dragElement instanceof MToolItem) {
			MToolBar mtb = MApplicationFactory.eINSTANCE.createToolBar();
			mtb.getChildren().add((MToolItem) dragElement);
			mtb.setToBeRendered(false);
			oldParent.getChildren().add(mtb);
			oldParent.getChildren().remove(mtb);
			mtb.setToBeRendered(true);
			
			return mtb;
		}
		
		return dragElement;
	}

	public void drop(MElementContainer<MUIElement> newContainer, int itemIndex) {
		if (dragElement.getParent() != null)
			dragElement.getParent().getChildren().remove(dragElement);
		if (itemIndex >= 0)
			newContainer.getChildren().add(itemIndex, dragElement);
		else
			newContainer.getChildren().add(dragElement);
		
		newContainer.setSelectedElement(dragElement);
		dndShell.dispose();
	}
}
