/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
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
				areaItem.setText(areaModel.getLocalizedLabel());
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				areaItem.setImage(getImage(areaModel));
			} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
				areaItem.setToolTipText(areaModel.getLocalizedTooltip());
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

		Composite parentComp = (Composite) parent;

		Composite areaComp = new Composite(parentComp, SWT.NONE);
		areaComp.setLayout(new FillLayout());

		return areaComp;
	}

	private void ensureCTF(MArea areaModel) {
		if (areaModel.getWidget() instanceof CTabFolder)
			return;

		Composite curComp = (Composite) areaModel.getWidget();
		Composite parentComp = curComp.getParent();
		CTabFolder ctf = new CTabFolder(parentComp, SWT.BORDER | SWT.SINGLE);

		CTabItem cti = new CTabItem(ctf, SWT.NONE);
		if (areaModel.getLabel() != null)
			cti.setText(areaModel.getLocalizedLabel());
		if (areaModel.getTooltip() != null)
			cti.setToolTipText(areaModel.getLocalizedTooltip());
		if (areaModel.getIconURI() != null)
			cti.setImage(getImage(areaModel));

		curComp.setParent(ctf);
		cti.setControl(curComp);
		ctf.setSelection(cti);

		curComp.setData(AbstractPartRenderer.OWNING_ME, null);
		bindWidget(areaModel, ctf);
	}

	private void ensureComposite(MArea areaModel) {
		if (areaModel.getWidget() instanceof CTabFolder) {
			CTabFolder ctf = (CTabFolder) areaModel.getWidget();
			CTabItem cti = ctf.getItem(0);
			Composite innerComp = (Composite) cti.getControl();
			innerComp.setParent(ctf.getParent());
			cti.setControl(null);

			ctf.setData(AbstractPartRenderer.OWNING_ME, null);
			ctf.dispose();

			bindWidget(areaModel, innerComp);
			innerComp.setVisible(true);
		}
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

		if (!(parentElement instanceof MArea))
			return null;

		// If we're hosting an MPSC then we should show the CTF
		if (element instanceof MPartSashContainer) {
			ensureCTF((MArea) parentElement);
			CTabFolder ctf = (CTabFolder) parentElement.getWidget();
			return ctf.getItem(0).getControl();
		}

		// Otherwise we should show only the composite
		ensureComposite((MArea) parentElement);
		return parentElement.getWidget();
	}
}
