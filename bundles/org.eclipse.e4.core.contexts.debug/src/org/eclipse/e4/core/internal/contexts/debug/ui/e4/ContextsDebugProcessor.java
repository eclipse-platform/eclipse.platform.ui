/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui.e4;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.swt.internal.copy.WorkbenchSWTMessages;

/* To use e4-style view contribtuion, add to plugin.xml :
   <extension
         id="ContextDebug"
         name="Context Trace and Debug"
         point="org.eclipse.e4.workbench.model">
      <processor
            beforefragment="true"
            class="org.eclipse.e4.core.internal.contexts.debug.ui.e4.ContextsDebugProcessor">
      </processor>
   </extension>
 */

public class ContextsDebugProcessor {

	@Inject
	public ContextsDebugProcessor() {
		// placeholder
	}

	@Execute
	public void addDebugDescriptor(MApplication application) {
		List<MPartDescriptor> descriptors = application.getDescriptors();

		MPartDescriptor descriptor = BasicFactoryImpl.eINSTANCE.createPartDescriptor();
		descriptor.setLabel("Contexts"); // XXX translate
		descriptor.setElementId("org.eclipse.e4.core.contexts.debug.ui.view");
		descriptor.setCategory("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$

		List<String> tags = descriptor.getTags();
		tags.add("View"); //$NON-NLS-1$
		tags.add("categoryTag:" + WorkbenchSWTMessages.ICategory_general); //$NON-NLS-1$	

		descriptor.setCloseable(true);
		descriptor.setAllowMultiple(false);
		descriptor.setContributionURI("bundleclass://org.eclipse.e4.core.contexts.debug/org.eclipse.e4.core.internal.contexts.debug.ui.ContextsView");
		descriptor.setIconURI("platform:/plugin/org.eclipse.e4.core.contexts.debug/icons/full/obj16/contexts.gif");

		application.getDescriptors().add(descriptor);
	}

}
