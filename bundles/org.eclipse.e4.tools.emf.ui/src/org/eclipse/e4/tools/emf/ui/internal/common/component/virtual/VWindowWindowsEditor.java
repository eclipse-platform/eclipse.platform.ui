package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;

public class VWindowWindowsEditor extends VWindowEditor<MWindow> {

	@Inject
	public VWindowWindowsEditor() {
		super(BasicPackageImpl.Literals.WINDOW__WINDOWS);
	}

}