package org.eclipse.e4.tools.emf.ui.common;

import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.swt.widgets.Shell;

public interface IModelExtractor {

	boolean extract(Shell shell, IProject project, List<MApplicationElement> maes);

}
