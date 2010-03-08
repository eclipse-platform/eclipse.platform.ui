package org.eclipse.e4.tools.emf.editor;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.tools.emf.ui.common.XMIModelResource;
import org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.swt.widgets.Composite;

public class XMIFileEditor {
	@Inject
	public XMIFileEditor(Composite composite, MPart part) {
		XMIModelResource resource = new XMIModelResource(part.getContainerData());
		ApplicationModelEditor editor = new ApplicationModelEditor(composite, resource);
	}
}
