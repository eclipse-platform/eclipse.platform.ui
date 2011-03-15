package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;

public class VPerspectiveWindowsEditor extends VWindowEditor {

	@Inject
	public VPerspectiveWindowsEditor() {
		super(AdvancedPackageImpl.Literals.PERSPECTIVE__WINDOWS);
	}

}