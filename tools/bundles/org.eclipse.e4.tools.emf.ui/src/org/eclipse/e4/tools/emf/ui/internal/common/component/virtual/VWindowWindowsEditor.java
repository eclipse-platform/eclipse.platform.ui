package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;

import jakarta.inject.Inject;

public class VWindowWindowsEditor extends VWindowEditor<MWindow> {

	@Inject
	public VWindowWindowsEditor() {
		super(BasicPackageImpl.Literals.WINDOW__WINDOWS);
	}

}