/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewActionDelegate;

public class ActionDelegateManager {
		
		private List delegates = new ArrayList(2);
		private IViewPart part;
		private StructuredViewer viewer;
		
		public static class WrappedActionDelegate extends Action {
			private IActionDelegate delegate;
			private Viewer viewer;

			public WrappedActionDelegate(IActionDelegate delegate, IViewPart part, Viewer viewer) {
				this.delegate = delegate;
				this.viewer = viewer;
				// Associate delegate with the synchronize view, this will allow
				if(delegate instanceof IViewActionDelegate) {
					((IViewActionDelegate)delegate).init(part);
				}
			}

			public void run() {
				if (viewer != null) {
					ISelection selection = new StructuredSelection(viewer.getInput());		
					if (!selection.isEmpty()) {
						delegate.selectionChanged(this, selection);
						delegate.run(this);
					}
				}
			}

			public IActionDelegate getDelegate() {
				return delegate;
			}
		}

		public ActionDelegateManager() {
		}
		
		/*
		 * Update the enablement of any action delegates 
		 */
		public void updateActionEnablement(Object input) {
			ISelection selection = new StructuredSelection(input);
			for (Iterator it = delegates.iterator(); it.hasNext(); ) {
				WrappedActionDelegate delegate = (WrappedActionDelegate) it.next();
				delegate.getDelegate().selectionChanged(delegate, selection);
			}
		}

		public void addDelegate(WrappedActionDelegate delagate) {
			delegates.add(delagate);
		}
}
