/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPartReference;

public class APITestUtils {
	private static Map<IEclipseContext, ISaveHandler> originalHandlers = new HashMap<IEclipseContext, ISaveHandler>();
	private static TestSaveHandler testSaveHandler = new TestSaveHandler();

	static class TestSaveHandler extends PartServiceSaveHandler {
		private int response;

		public void setResponse(int response) {
			this.response = response;
		}

		@Override
		public Save promptToSave(MPart dirtyPart) {
			switch (response) {
			case 0: return Save.YES;
			case 1: return Save.NO;
			case 2: return Save.CANCEL;
			case ISaveablePart2.DEFAULT:
				return Save.YES;
			}
			throw new RuntimeException();
		}

		@Override
		public Save[] promptToSave(Collection<MPart> dirtyParts) {
			Save save = promptToSave((MPart) null);
			Save[] prompt = new Save[dirtyParts.size()];
			Arrays.fill(prompt, save);
			return prompt;
		}

	}

	public static boolean isFastView(IViewReference ref) {
		MPart part = ((WorkbenchPartReference) ref).getModel();
		MUIElement parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			if (placeholder != null) {
				parent = placeholder.getParent();
			}
		}

		if (parent != null) {
			List<String> tags = parent.getTags();
			return tags.contains("Minimized") //$NON-NLS-1$
					|| tags.contains("MinimizedByZoom"); //$NON-NLS-1$
		}
		return false;
	}

	public static void saveableHelperSetAutomatedResponse(final int response) {
		SaveableHelper.testSetAutomatedResponse(response);
		Workbench workbench = (Workbench) PlatformUI.getWorkbench();
		MApplication application = workbench.getApplication();

		IEclipseContext context = application.getContext();
		saveableHelperSetAutomatedResponse(response, context);

		while (workbench.getDisplay().readAndDispatch()) {
			;
		}

		for (MWindow window : application.getChildren()) {
			saveableHelperSetAutomatedResponse(response, window.getContext());
		}

		while (workbench.getDisplay().readAndDispatch()) {
			;
		}
	}

	private static void saveableHelperSetAutomatedResponse(final int response,
			IEclipseContext context) {
		ISaveHandler saveHandler = (ISaveHandler) context.get(ISaveHandler.class.getName());
		if (response == -1) {
			context.set(ISaveHandler.class.getName(), originalHandlers.remove(context));
		} else {
			if (saveHandler != testSaveHandler) {
				originalHandlers.put(context, saveHandler);
			}
			testSaveHandler.setResponse(response);
			context.set(ISaveHandler.class.getName(), testSaveHandler);
		}
	}
}
