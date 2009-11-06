/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IPresentationEngine;

public class HeadlessContextPresentationEngine implements IPresentationEngine {

	private static IEclipseContext getParentContext(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		IEclipseContext context = null;
		while (parent != null) {
			if (parent instanceof MContext) {
				return ((MContext) parent).getContext();
			}
			parent = parent.getParent();
		}

		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.IPresentationEngine#createGui(org.eclipse
	 * .e4.ui.model.application.MUIElement, java.lang.Object)
	 */
	public Object createGui(MUIElement element, Object parent) {
		if (element instanceof MContext) {
			final IEclipseContext parentContext = getParentContext(element);
			MContext mcontext = (MContext) element;
			final IEclipseContext createdContext = EclipseContextFactory
					.create(parentContext, null);

			createdContext.set(IContextConstants.DEBUG_STRING, element
					.getClass().getInterfaces()[0].getName()
					+ " eclipse context"); //$NON-NLS-1$
			createdContext.set(MApplicationElement.class.getName(), element);

			createdContext.runAndTrack(new Runnable() {
				public void run() {
					MApplicationElement activePart = (MApplicationElement) createdContext
							.get(IServiceConstants.ACTIVE_PART);
					if (parentContext != null) {
						parentContext.set(IServiceConstants.ACTIVE_PART,
								activePart);
					}
				}

				@Override
				public String toString() {
					return getClass().getName() + '['
							+ IServiceConstants.ACTIVE_PART_ID + ']';
				}
			});

			createdContext.runAndTrack(new Runnable() {
				public void run() {
					IEclipseContext childContext = (IEclipseContext) createdContext
							.getLocal(IContextConstants.ACTIVE_CHILD);
					if (childContext != null) {
						parentContext.set(IContextConstants.ACTIVE_CHILD,
								createdContext);
					}
				}

				@Override
				public String toString() {
					return getClass().getName() + '['
							+ IContextConstants.ACTIVE_CHILD + ']';
				}
			});

			parentContext.runAndTrack(new Runnable() {
				public void run() {
					IEclipseContext childContext = (IEclipseContext) parentContext
							.get(IContextConstants.ACTIVE_CHILD);
					if (childContext != null) {
						parentContext.set(IServiceConstants.ACTIVE_PART,
								childContext.get(MApplicationElement.class
										.getName()));
					}
				}

				@Override
				public String toString() {
					return getClass().getName() + '['
							+ IContextConstants.ACTIVE_CHILD + ']';
				}
			});

			for (String variable : mcontext.getVariables()) {
				createdContext.declareModifiable(variable);
			}

			mcontext.setContext(createdContext);
		}

		if (element instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) element).getChildren()) {
				if (child instanceof MUIElement) {
					createGui((MUIElement) child, element);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.IPresentationEngine#createGui(org.eclipse
	 * .e4.ui.model.application.MUIElement)
	 */
	public Object createGui(MUIElement element) {
		return createGui(element, null);
	}

	public void removeGui(MUIElement element) {
		if (element instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) element).getChildren()) {
				if (child instanceof MUIElement) {
					removeGui((MUIElement) child);
				}
			}
		}

		if (element instanceof MContext) {
			MContext mcontext = (MContext) element;
			IEclipseContext context = mcontext.getContext();
			if (context instanceof IDisposable) {
				((IDisposable) context).dispose();
			}

			mcontext.setContext(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.IPresentationEngine#run(org.eclipse.e4.ui
	 * .model.application.MApplicationElement)
	 */
	public Object run(MApplicationElement uiRoot, IEclipseContext appContext) {
		return 0;
	}
}
