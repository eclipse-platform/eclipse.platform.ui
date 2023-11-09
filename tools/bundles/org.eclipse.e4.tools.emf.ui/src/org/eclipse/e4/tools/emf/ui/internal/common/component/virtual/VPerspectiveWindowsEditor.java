package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;

import jakarta.inject.Inject;

public class VPerspectiveWindowsEditor extends VWindowEditor<MPerspective> {

	@Inject
	public VPerspectiveWindowsEditor() {
		super(AdvancedPackageImpl.Literals.PERSPECTIVE__WINDOWS);
	}

}