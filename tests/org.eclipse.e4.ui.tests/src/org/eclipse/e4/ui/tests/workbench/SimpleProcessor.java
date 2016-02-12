/*******************************************************************************
 * Copyright (c) 2016 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alexandra Buzila - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;

abstract class SimpleProcessor {
	@Inject
	private MApplication application;

	@Execute
	public void run() {
		if (application != null) {
			MPartDescriptor descriptor = MBasicFactory.INSTANCE.createPartDescriptor();
			descriptor.setElementId(getDescriptorId());
			application.getDescriptors().add(descriptor);
		}
	}

	public abstract String getDescriptorId();
}
