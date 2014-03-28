/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.UIEvents.UIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
		@Override
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
			if (UIEvents.UILabel.LABEL.equals(attName)
					|| UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
				areaItem.setText(areaModel.getLocalizedLabel());
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				areaItem.setImage(getImage(areaModel));
			} else if (UIEvents.UILabel.TOOLTIP.equals(attName)
					|| UIEvents.UILabel.LOCALIZED_TOOLTIP.equals(attName)) {
				areaItem.setToolTipText(areaModel.getLocalizedTooltip());
			}
		}
	};

	private EventHandler widgetListener = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event
					.getProperty(EventTags.ELEMENT);
			if (!(changedElement instanceof MPartStack))
				return;

			MArea areaModel = findArea(changedElement);
			if (areaModel != null)
				synchCTFState(areaModel);
		}

		private MArea findArea(MUIElement element) {
			MUIElement parent = element.getParent();
			while (parent != null) {
				if (parent instanceof MArea)
					return (MArea) parent;
				parent = parent.getParent();
			}
			return null;
		}
	};

	@PostConstruct
	void init() {
		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, itemUpdater);
		eventBroker.subscribe(UIElement.TOPIC_WIDGET, widgetListener);
	}

	@PreDestroy
	void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(widgetListener);
	}

	@Override
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

		// Find the stack in the area that used to have the min/max state
		List<MPartStack> stacks = modelService.findElements(areaModel, null,
				MPartStack.class, null);
		MPartStack curStack = null;
		for (MPartStack stack : stacks) {
			if (stack.isToBeRendered()
					&& stack.getWidget() instanceof CTabFolder) {
				CTabFolder stackCTF = (CTabFolder) stack.getWidget();
				if (stackCTF.getMinimizeVisible()
						|| stackCTF.getMaximizeVisible()) {
					curStack = stack;
					break;
				}
			}
		}

		// ...and copy over its min/max state
		if (curStack != null) {
			CTabFolder curCTF = (CTabFolder) curStack.getWidget();
			ctf.setMinimizeVisible(curCTF.getMinimizeVisible());
			ctf.setMaximizeVisible(curCTF.getMaximizeVisible());
			ctf.setMinimized(curCTF.getMinimized());
			ctf.setMaximized(curCTF.getMaximized());

			curCTF.setMinimizeVisible(false);
			curCTF.setMaximizeVisible(false);
		}

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
		ctf.getParent().layout(null, SWT.ALL | SWT.DEFER | SWT.CHANGED);
	}

	private void ensureComposite(MArea areaModel) {
		if (areaModel.getWidget() instanceof CTabFolder) {
			CTabFolder ctf = (CTabFolder) areaModel.getWidget();
			CTabItem cti = ctf.getItem(0);
			Composite innerComp = (Composite) cti.getControl();
			innerComp.setParent(ctf.getParent());
			cti.setControl(null);

			// OK now copy over the min/max state of the area stack to the
			// remaining part stack
			List<MPartStack> stacks = modelService.findElements(areaModel,
					null, MPartStack.class, null);
			for (MPartStack stack : stacks) {
				if (stack.isToBeRendered()
						&& stack.getWidget() instanceof CTabFolder) {
					CTabFolder stackCTF = (CTabFolder) stack.getWidget();
					stackCTF.setMinimizeVisible(ctf.getMinimizeVisible());
					stackCTF.setMaximizeVisible(ctf.getMaximizeVisible());
					stackCTF.setMinimized(ctf.getMinimized());
					stackCTF.setMaximized(ctf.getMaximized());
				}
			}

			ctf.setData(AbstractPartRenderer.OWNING_ME, null);
			ctf.dispose();

			bindWidget(areaModel, innerComp);
			innerComp.setVisible(true);
			innerComp.getParent().layout(true, true);
		}
	}

	private void synchCTFState(MArea areaModel) {
		List<MPartStack> stacks = modelService.findElements(areaModel, null,
				MPartStack.class, null);
		int count = 0;
		for (MPartStack stack : stacks) {
			if (stack.isToBeRendered())
				count++;
		}

		// If there's more than one stack visible we use a CTF
		if (count > 1)
			ensureCTF(areaModel);
		else
			ensureComposite(areaModel);
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

		MArea areaModel = (MArea) parentElement;
		synchCTFState(areaModel);

		if (areaModel.getWidget() instanceof CTabFolder) {
			CTabFolder ctf = (CTabFolder) areaModel.getWidget();
			return ctf.getItem(0).getControl();
		}

		return parentElement.getWidget();
	}
}
