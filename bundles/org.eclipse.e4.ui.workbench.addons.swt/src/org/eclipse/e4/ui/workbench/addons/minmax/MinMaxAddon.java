/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.minmax;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Addon supporting standard drag and drop management
 */
public class MinMaxAddon {
	private static String trimURI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack"; //$NON-NLS-1$

	static String ID_SUFFIX = "(minimized)"; //$NON-NLS-1$

	@Inject
	IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	private EventHandler installHook = new EventHandler() {
		public void handleEvent(Event event) {
			final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
			Widget widget = (Widget) event.getProperty(EventTags.NEW_VALUE);
			if (changedElement instanceof MPartStack && widget instanceof CTabFolder
					&& !changedElement.getTags().contains("EditorStack") //$NON-NLS-1$
					&& changedElement.getElementId() != null) {
				final CTabFolder folder = (CTabFolder) widget;
				folder.setMinimizeVisible(true);
				folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
					public void minimize(CTabFolderEvent event) {
						minimizeStack((MPartStack) changedElement);
					}

					public void restore(CTabFolderEvent event) {
						restoreStack((MPartStack) changedElement);
					}
				});
			}
		}
	};

	@PostConstruct
	void hookListeners() {
		String topic = UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET);
		eventBroker.subscribe(topic, null, installHook, false);
	}

	@PreDestroy
	void unhookListeners() {
		eventBroker.unsubscribe(installHook);
	}

	void minimizeStack(MPartStack stack) {
		MTrimmedWindow window = (MTrimmedWindow) modelService.getTopLevelWindowFor(stack);

		// Is there already a TrimControl there ?
		String trimId = stack.getElementId() + getMinimizedStackSuffix(stack);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);

		if (trimStack == null) {
			trimStack = MenuFactoryImpl.eINSTANCE.createToolControl();
			trimStack.setElementId(trimId);
			trimStack.setContributionURI(trimURI);

			MTrimBar bar = modelService.getTrim(window, SideValue.TOP);
			bar.getChildren().add(trimStack);
		} else {
			trimStack.setVisible(true);
		}

		stack.setVisible(false);
	}

	void restoreStack(MPartStack stack) {
		MWindow window = modelService.getTopLevelWindowFor(stack);
		String trimId = stack.getElementId() + getMinimizedStackSuffix(stack);
		MToolControl trimStack = (MToolControl) modelService.find(trimId, window);
		TrimStack ts = (TrimStack) trimStack.getObject();
		ts.restoreStack();
	}

	/**
	 * @param stack
	 * @return
	 */
	private String getMinimizedStackSuffix(MPartStack stack) {
		String id = ID_SUFFIX;
		MPerspective persp = modelService.getPerspectiveFor(stack);
		if (persp != null) {
			id = '(' + persp.getElementId() + ')';
		}
		return id;
	}
}
