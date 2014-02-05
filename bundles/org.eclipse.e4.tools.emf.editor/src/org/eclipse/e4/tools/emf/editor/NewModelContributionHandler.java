package org.eclipse.e4.tools.emf.editor;

import java.io.IOException;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class NewModelContributionHandler {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, MApplication application, EModelService modelService) {
		FileDialog dialog = new FileDialog(shell,SWT.SAVE);
		String file = dialog.open();
		if( file != null ) {
			String name = file.substring(file.lastIndexOf("/") + 1);
			String filePath = "file://" + file;

			try {
				E4XMIResource resource = new E4XMIResource();
				resource.getContents().add((EObject) MFragmentFactory.INSTANCE.createModelFragments());
				resource.setURI(URI.createFileURI(file));
				resource.save(null);

				System.err.println("Saved to: " + file);

				MPartStack stack = (MPartStack) modelService.find("modeleditorstack", application);

				MInputPart part = MBasicFactory.INSTANCE.createInputPart();
				part.setLabel(name);
				part.setTooltip(file);
				part.setContributionURI("bundleclass://org.eclipse.e4.tools.emf.ui/org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor");
				part.setIconURI("platform:/plugin/org.eclipse.e4.tools.emf.editor/icons/full/application_view_tile.png");
				part.setInputURI(filePath);

				part.setCloseable(true);
				stack.getChildren().add(part);
				stack.setSelectedElement(part);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}