package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;

public class VPartDescriptorMenuEditor extends VMenuEditor {
	@Inject
	public VPartDescriptorMenuEditor() {
		super(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR__MENUS);
	}

}
