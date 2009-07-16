/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.List;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MStack;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * This class encapsulates the functionality necessary to manage stacks of parts
 * in a 'lazy loading' manner. For these stacks only the currently 'active'
 * child <b>most</b> be rendered so in this class we over ride that default
 * behavior for processing the stack's contents to prevent all of the contents
 * from being rendered, calling 'childAdded' instead. This not only saves time
 * and SWT resources but is necessary in an IDE world where we must not
 * arbitrarily cause plug-in loading.
 * 
 */
public abstract class LazyStackRenderer extends SWTPartRenderer {
	public LazyStackRenderer() {
		super();
	}

	public void postProcess(MPart<?> part) {
		if (!(part instanceof MStack))
			return;

		MStack stack = (MStack) part;
		MPart<?> selPart = stack.getActiveChild();

		// If there's no 'active' part defined then pick the first
		if (selPart == null && stack.getChildren().size() > 0) {
			// NOTE: no need to render first because the listener for
			// the active child changing will do it
			stack.setActiveChild((MItemPart<?>) part.getChildren().get(0));
		} else if (selPart != null && selPart.getWidget() == null) {
			renderer.createGui(selPart);
		}
	}

	@Override
	public <P extends MPart<?>> void processContents(MPart<P> me) {
		Widget parentWidget = getParentWidget(me);
		if (parentWidget == null)
			return;

		// Lazy Loading: here we only process the contents through childAdded,
		// we specifically do not render them
		List<P> parts = me.getChildren();
		if (parts != null) {
			for (MPart<?> childME : parts) {
				if (childME.isVisible())
					internalChildAdded(me, childME);
			}
		}
	}

	/**
	 * This method is necessary to allow the parent container to show
	 * affordances (i.e. tabs) for child elements -without- creating them (as
	 * simply calling 'createChild' would)
	 * 
	 * @param parentME
	 *            The parent model element
	 * @param childME
	 *            The child to show the affordance for
	 */
	protected void internalChildAdded(MPart parentME, MPart childME) {
		// NO-OP
	}

	@Override
	public void hookControllerLogic(final MPart<?> me) {
		super.hookControllerLogic(me);

		if (!(me instanceof MStack))
			return;

		final MStack sm = (MStack) me;

		// Detect activation...picks up cases where the user clicks on the
		// (already active) part
		if (sm.getWidget() instanceof Control) {
			Control ctrl = (Control) sm.getWidget();
			ctrl.addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					CTabFolder ctf = (CTabFolder) event.widget;
					MStack stack = (MStack) ctf.getData(OWNING_ME);
					MItemPart<?> selPart = stack.getActiveChild();
					if (selPart != null)
						activate(selPart);
				}
			});
		}

		// Listen for changes to the 'activeChild'. If necessary render the
		// contents of the newly activated child before making it active
		((EObject) me).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (ApplicationPackage.Literals.MPART__ACTIVE_CHILD.equals(msg
						.getFeature())) {
					MStack stack = (MStack) msg.getNotifier();
					MPart<?> selPart = stack.getActiveChild();
					if (selPart != null && selPart.getWidget() == null)
						renderer.createGui(selPart);
					// activate(selPart);
				}
			}
		});
	}
}
