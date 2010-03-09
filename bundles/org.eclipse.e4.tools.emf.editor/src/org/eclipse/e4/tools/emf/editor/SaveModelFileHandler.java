package org.eclipse.e4.tools.emf.editor;

import javax.inject.Named;

import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.modeling.EPartService;

public class SaveModelFileHandler {
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService) {
		partService.savePart(part, false);
	}
}
