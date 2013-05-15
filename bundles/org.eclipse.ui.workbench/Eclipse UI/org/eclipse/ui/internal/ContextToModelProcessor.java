/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.ui.internal.contexts.ContextPersistence;

/**
 * @since 3.5
 *
 */
public class ContextToModelProcessor {
	private Map<String, MBindingContext> contexts = new HashMap<String, MBindingContext>();


	@Execute
	void process(MApplication application, IEclipseContext context) {
		gatherContexts(application.getRootContext());
		ContextManager contextManager = context.get(ContextManager.class);
		if (contextManager == null) {
			contextManager = new ContextManager();
			context.set(ContextManager.class, contextManager);
		}
		ContextPersistence cp = new ContextPersistence(contextManager);
		cp.reRead();
		generateContexts(application, contextManager);
		cp.dispose();
	}

	/**
	 * @param application
	 * @param contextManager
	 */
	private void generateContexts(MApplication application, ContextManager contextManager) {
		for (Context ctx : contextManager.getDefinedContexts()) {
			try {
				MBindingContext contextModel = contexts.get(ctx.getId());
				if (contextModel == null) {
					contextModel = CommandsFactoryImpl.eINSTANCE.createBindingContext();
					contexts.put(ctx.getId(), contextModel);
				}
				contextModel.setElementId(ctx.getId());
				contextModel.setName(ctx.getName());
				contextModel.setDescription(ctx.getDescription());

			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (Context ctx : contextManager.getDefinedContexts()) {
			try {
				MBindingContext contextModel = contexts.get(ctx.getId());
				String parentId = ctx.getParentId();
				if (parentId == null) {
					if (!application.getRootContext().contains(contextModel)) {
						application.getRootContext().add(contextModel);
					}
				} else {
					MBindingContext parentContextModel = contexts.get(parentId);
					if (parentContextModel == null) {
						System.err.println("Could not find parent " + parentId + " for child " //$NON-NLS-1$ //$NON-NLS-2$
								+ ctx.getId());
					} else {
						if (!parentContextModel.getChildren().contains(contextModel)) {
							parentContextModel.getChildren().add(contextModel);
						}
					}
				}
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param contextList
	 */
	private void gatherContexts(List<MBindingContext> contextList) {
		for (MBindingContext ctx : contextList) {
			gatherContexts(ctx);
		}
	}

	/**
	 * @param ctx
	 */
	private void gatherContexts(MBindingContext ctx) {
		if (ctx == null) {
			return;
		}
		contexts.put(ctx.getElementId(), ctx);
		gatherContexts(ctx.getChildren());
	}

}
