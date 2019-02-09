package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;

public class VPartDescriptorMenuEditor extends VMenuEditor<MPartDescriptor> {
	@Inject
	public VPartDescriptorMenuEditor() {
		super(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.Literals.PART_DESCRIPTOR__MENUS);
	}

}
