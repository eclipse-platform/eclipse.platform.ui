package org.eclipse.e4.tools.emf.editor;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenModelFileHandler {
	public void execute(MApplication application, EModelService modelService) {
		MWindow window = application.getSelectedElement();

		FileDialog dialog = new FileDialog((Shell) window.getWidget());
		String file = dialog.open();
		if( file != null ) {
			String name = file.substring(file.lastIndexOf("/") + 1);

			MPartStack stack = (MPartStack) modelService.find("modeleditorstack", application);

			MPart part = MApplicationFactory.eINSTANCE.createPart();
			part.setLabel(name);
			part.setTooltip(file);
			part.setURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/org.eclipse.e4.tools.emf.editor.XMIFileEditor");
			part.setIconURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/icons/full/application_view_tile.png");
			part.setContainerData("file://" + file);
			part.setCloseable(true);
			stack.getChildren().add(part);
			stack.setSelectedElement(part);

			System.err.println(stack);
		}
	}
}
