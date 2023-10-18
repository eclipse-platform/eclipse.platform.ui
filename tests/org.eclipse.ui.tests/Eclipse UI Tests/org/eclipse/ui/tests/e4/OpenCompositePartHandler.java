package org.eclipse.ui.tests.e4;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class OpenCompositePartHandler {


	@Execute
	public void execute(EModelService modelService, MApplication app, EPartService partService) {
		MUIElement compositePart = modelService.cloneSnippet(app, "org.eclipse.ui.tests.compositepart.test",
				app.getSelectedElement());

		MUIElement muiElement = modelService.find("org.eclipse.ui.editorss", app);

		MUIElement element = ((MPlaceholder) muiElement).getRef();

		List<MPartStack> primaryStack = modelService.findElements(element, "org.eclipse.e4.primaryDataStack",
				MPartStack.class);

		if (!primaryStack.isEmpty()) {
			MPartStack partStack = primaryStack.get(0);
			partStack.getChildren().add((MStackElement) compositePart);
		}

		partService.activate((MPart) compositePart);

	}
}
