/*******************************************************************************
 * Copyright (c) 2022, 2024 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;

@Component(service = IContextFunction.class)
@IContextFunction.ServiceContextKey(IExtensionTracker.class)
@EventTopics(IEclipseContext.TOPIC_DISPOSE)
public class ExtensionTrackeContextFunction extends ContextFunction implements EventHandler {

	@Reference
	private ILog log;

	private Map<IEclipseContext, IExtensionTracker> createdObjects = new ConcurrentHashMap<>();

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		return createdObjects.computeIfAbsent(context, ctx -> {
			return new UIExtensionTracker(runnable -> {
				UISynchronize synchronize = ctx.get(UISynchronize.class);
				if (synchronize != null) {
					synchronize.asyncExec(runnable);
				}
			}, log);

		});
	}

	@Deactivate
	void shutdown() {
		createdObjects.values().forEach(IExtensionTracker::close);
		createdObjects.clear();
	}

	@Override
	public void handleEvent(Event event) {
		Object property = event.getProperty(IEclipseContext.PROPERTY_CONTEXT);
		if (property instanceof IEclipseContext) {
			createdObjects.computeIfPresent((IEclipseContext) property, (k, v) -> {
				v.close();
				return null;
			});
		}
	}

}
