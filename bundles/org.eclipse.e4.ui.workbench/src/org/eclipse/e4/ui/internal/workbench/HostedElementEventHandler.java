/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Event handler for the UIEvents.UIElement.TOPIC_WIDGET topic. Cleans up after
 * a hosted element is disposed.
 *
 * @see ModelServiceImpl
 */
public class HostedElementEventHandler implements EventHandler {

	@Inject
	@Optional
	UISynchronize uiSync;

	@Override
	public void handleEvent(Event event) {
		// as we change the UI application model, this code needs to be executed in the
		// UI thread
		if (uiSync != null) {
			uiSync.syncExec(() -> {
				final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
				if (!changedElement.getTags().contains(ModelServiceImpl.HOSTED_ELEMENT)) {
					return;
				}

				if (changedElement.getWidget() != null) {
					return;
				}

				EObject eObj = (EObject) changedElement;
				if (!(eObj.eContainer() instanceof MWindow)) {
					return;
				}

				MWindow hostingWindow = (MWindow) eObj.eContainer();
				hostingWindow.getSharedElements().remove(changedElement);
				changedElement.getTags().remove(ModelServiceImpl.HOSTED_ELEMENT);
			});
		}
	}
}