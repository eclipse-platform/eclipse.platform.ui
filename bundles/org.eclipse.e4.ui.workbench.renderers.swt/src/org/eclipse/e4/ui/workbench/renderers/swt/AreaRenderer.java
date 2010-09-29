/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class AreaRenderer extends SWTPartRenderer {

	@Inject
	Logger logger;
	@Inject
	IEventBroker eventBroker;

	private EventHandler itemUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MArea
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MArea))
				return;

			MArea areaModel = (MArea) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			CTabFolder ctf = (CTabFolder) areaModel.getWidget();
			CTabItem areaItem = ctf.getItem(0);

			// No widget == nothing to update
			if (areaItem == null)
				return;

			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (UIEvents.UILabel.LABEL.equals(attName)) {
				areaItem.setText(areaModel.getLabel());
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				areaItem.setImage(getImage(areaModel));
			} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
				areaItem.setToolTipText(areaModel.getTooltip());
			}
		}
	};

	@PostConstruct
	void init() {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UILabel.TOPIC),
				itemUpdater);
	}

	@PreDestroy
	void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
	}

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MArea) || !(parent instanceof Composite))
			return null;

		MArea areaModel = (MArea) element;
		Composite parentComp = (Composite) parent;

		CTabFolder ctf = new CTabFolder(parentComp, SWT.BORDER | SWT.SINGLE);
		ctf.setMaximizeVisible(true);
		ctf.setMinimizeVisible(true);
		CTabItem cti = new CTabItem(ctf, SWT.NONE);
		if (areaModel.getLabel() != null)
			cti.setText(areaModel.getLabel());
		if (areaModel.getTooltip() != null)
			cti.setToolTipText(areaModel.getTooltip());
		if (areaModel.getIconURI() != null)
			cti.setImage(getImage(areaModel));

		Composite areaComp = new Composite(ctf, SWT.NONE);
		SashLayout sl = new SashLayout(areaComp, null);
		areaComp.setLayout(sl);
		cti.setControl(areaComp);

		ctf.setSelection(cti);

		return ctf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#childRendered
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer,
	 * org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		if (!(parentElement.getWidget() instanceof CTabFolder))
			return;

		super.childRendered(parentElement, element);

		// Reset the 'root' element to the new child (last one in wins)
		CTabFolder ctf = (CTabFolder) parentElement.getWidget();
		Composite layoutComp = (Composite) ctf.getItem(0).getControl();
		SashLayout sl = (SashLayout) layoutComp.getLayout();
		sl.setRootElemenr(element);
		layoutComp.layout(true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#getUIContainer
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public Object getUIContainer(MUIElement element) {
		MUIElement parentElement = element.getParent();

		if (!(parentElement instanceof MArea)
				|| !(parentElement.getWidget() instanceof CTabFolder))
			return null;

		CTabFolder ctf = (CTabFolder) parentElement.getWidget();
		return ctf.getItem(0).getControl();
	}
}
