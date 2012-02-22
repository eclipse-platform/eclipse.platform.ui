/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui.legacy;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.debug.ui.ContextsView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * 

   <extension point="org.eclipse.ui.views">
      <view name="%traceView" icon="$nl$/icons/full/obj16/contexts.gif" category="org.eclipse.debug.ui" class="org.eclipse.e4.core.internal.contexts.debug.ui.legacy.ContextTraceLegacyView" id="org.eclipse.e4.core.contexts.debug.TraceView">
      </view>
   </extension>
   
   

 * The 3.x style wrapper for the context tracing view.
 */
public class ContextTraceLegacyView extends ViewPart {

	private ContextsView viewer;

	public ContextTraceLegacyView() {
		// placeholder
	}

	public void createPartControl(Composite parent) {
		IEclipseContext context = (IEclipseContext) getSite().getService(IEclipseContext.class);
		viewer = ContextInjectionFactory.make(ContextsView.class, context);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.eclipse.e4.core.contexts.debug.view");
	}

	public void setFocus() {
		if (viewer != null)
			viewer.setFocus();
	}
}