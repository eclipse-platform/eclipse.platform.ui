/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.splitteraddon;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.event.Event;

/**
 * Support containment of split internal parts.
 * 
 * @since 1.1
 */
public class SplitHost {
	@Inject
	EModelService ms;
	@Inject
	EPartService ps;
	@Inject
	MCompositePart myPart;

	List<MPart> getSubParts() {
		List<MPart> childParts = ms.findElements(myPart, null, MPart.class, null);
		return childParts;
	}

	MPart findInnerActive(MCompositePart outer) {
		MPart innerActive = null;
		MUIElement curParent = outer;
		while (innerActive == null && curParent != null) {
			if (curParent instanceof MElementContainer<?>) {
				MElementContainer<?> container = (MElementContainer<?>) curParent;
				if (container.getSelectedElement() instanceof MPart) {
					innerActive = (MPart) container.getSelectedElement();
				} else if (container.getSelectedElement() instanceof MElementContainer<?>) {
					curParent = container.getSelectedElement();
				} else {
					curParent = null;
				}
			}
		}

		return innerActive;
	}

	@Inject
	@Optional
	void tbrHandler(@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event eventData) {
		MUIElement changedElement = (MUIElement) eventData.getProperty(UIEvents.EventTags.ELEMENT);

		if (!isOneOfMyParts(changedElement))
			return;

		boolean isDirty = false;
		List<MPart> kids = getSubParts();
		kids.remove(0);
		for (MPart subPart : kids) {
			isDirty |= subPart.isDirty();
		}
		myPart.setDirty(isDirty);
	}

	private boolean isOneOfMyParts(MUIElement changedElement) {
		MElementContainer<MUIElement> parent = changedElement.getParent();
		Object parentObj = parent;
		while (parent != null && parentObj != myPart) {
			parent = parent.getParent();
			parentObj = parent;
		}

		return parentObj == myPart;
	}

	void callingAllParts(Class<? extends Annotation> clz) {
		List<MPart> parts = ms.findElements(myPart, null, MPart.class, null);
		for (MPart part : parts) {
			if (part == myPart)
				continue;

			Control ctrl = (Control) part.getWidget();
			if (part.getObject() != null && ctrl != null && !ctrl.isDisposed())
				ContextInjectionFactory.invoke(part.getObject(), clz, part.getContext(), null);
		}
	}

	@Persist
	void persist() {
		callingAllParts(Persist.class);
	}

	@PersistState
	void persistState() {
		callingAllParts(PersistState.class);
	}

	@Focus
	void setFocus() {
		MPart ap = findInnerActive(myPart);
		if (ap == null)
			return;

		Control ctrl = (Control) ap.getWidget();
		if (ap.getObject() != null && ctrl != null && !ctrl.isDisposed())
			ContextInjectionFactory.invoke(ap.getObject(), Focus.class, ap.getContext(), null);
	}

	// @PostConstruct
	// void createWidget(Composite parent) {
	// System.out.println("New Split Host");
	// Composite newComp = new Composite(parent, SWT.NONE);
	// newComp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
	// newComp.setLayout(new FillLayout());
	// }
}
