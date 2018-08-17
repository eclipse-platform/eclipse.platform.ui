/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;

/**
 *
 */
public abstract class AbstractModelProcessorImpl {
	@Inject
	@Named("app.base")
	private MApplication application;

	@Inject
	@Named("my.customkey")
	private MCommand command;

	@Execute
	public void run() {
		if (command != null) {
			MCommandParameter param = MCommandsFactory.INSTANCE
					.createCommandParameter();
			param.setElementId("processor.command." + getSuffix());
			command.getParameters().add(param);
		}

		if (application != null) {
			MPartDescriptor descriptor = MBasicFactory.INSTANCE
					.createPartDescriptor();
			descriptor.setElementId("processor.descriptor." + getSuffix());
			application.getDescriptors().add(descriptor);

		}

		doRun();
	}

	protected abstract void doRun();

	protected abstract String getSuffix();
}
