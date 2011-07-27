/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.compat.tests.tweaklet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler.Save;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.SlavePageService;
import org.eclipse.ui.internal.SlavePartService;
import org.eclipse.ui.internal.SlaveSelectionService;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.tests.helpers.TestFacade;

public class TestFacadeE4Impl extends TestFacade {

	@Override
	public void assertActionSetId(IWorkbenchPage page, String id,
			boolean condition) {
		E4Util.unsupported("assertActionSetId");
	}

	@Override
	public int getActionSetCount(IWorkbenchPage page) {
		E4Util.unsupported("assertActionSetId");
		return 0;
	}

	@Override
	public void addFastView(IWorkbenchPage page, IViewReference ref) {
		E4Util.unsupported("assertActionSetId");
	}

	@Override
	public IStatus saveState(IWorkbenchPage page, IMemento memento) {
		E4Util.unsupported("assertActionSetId");
		return null;
	}

	@Override
	public IViewReference[] getFastViews(IWorkbenchPage page) {
		E4Util.unsupported("assertActionSetId");
		return null;
	}

	@Override
	public ArrayList getPerspectivePartIds(IWorkbenchPage page, String folderId) {
		E4Util.unsupported("assertActionSetId");
		return null;
	}

	@Override
	public boolean isFastView(IWorkbenchPage page, IViewReference ref) {
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

	@Override
	public void isSlavePageService(IPageService slaveService) {
		Assert.isTrue(slaveService instanceof SlavePageService);
	}

	@Override
	public IContributionItem getFVBContribution(IWorkbenchPage page) {
		E4Util.unsupported("getFVBContribution");
		return null;
	}

	@Override
	public void setFVBTarget(IContributionItem menuContribution,
			IViewReference viewRef) {
		E4Util.unsupported("setFVBTarget");
	}

	@Override
	public boolean isViewPaneVisible(IViewReference viewRef) {
		E4Util.unsupported("isViewPaneVisible");
		return false;
	}

	@Override
	public boolean isViewToolbarVisible(IViewReference viewRef) {
		E4Util.unsupported("isViewToolbarVisible");
		return false;
	}

	@Override
	public boolean isSlavePartService(IPartService slaveService) {
		return slaveService instanceof SlavePartService;
	}

	@Override
	public boolean isSlaveSelectionService(ISelectionService slaveService) {
		return slaveService instanceof SlaveSelectionService;
	}

	@Override
	public void saveableHelperSetAutomatedResponse(final int response) {
		Workbench workbench = (Workbench) PlatformUI.getWorkbench();
		MApplication application = workbench.getApplication();
		
		IEclipseContext context = application.getContext();
		saveableHelperSetAutomatedResponse(response, context);
		
		while (workbench.getDisplay().readAndDispatch());
		
		for (MWindow window : application.getChildren()) {
			saveableHelperSetAutomatedResponse(response, window.getContext());	
		}
		
		while (workbench.getDisplay().readAndDispatch());
	}

	private void saveableHelperSetAutomatedResponse(final int response,
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
	
	private static Map<IEclipseContext, ISaveHandler> originalHandlers = new HashMap<IEclipseContext, ISaveHandler>();
	
	private static TestSaveHandler testSaveHandler = new TestSaveHandler();
	
	static class TestSaveHandler implements ISaveHandler {
		
		private int response;
		
		public void setResponse(int response) {
			this.response = response;
		}

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

		public Save[] promptToSave(Collection<MPart> dirtyParts) {
			Save save = promptToSave((MPart) null);
			Save[] prompt = new Save[dirtyParts.size()];
			Arrays.fill(prompt, save);
			return prompt;
		}
		
	}

	@Override
	public boolean isClosableInPerspective(IViewReference ref) {
		E4Util.unsupported("isClosableInPerspective");
		return false;
	}

	@Override
	public boolean isMoveableInPerspective(IViewReference ref) {
		E4Util.unsupported("isMoveableInPerspective");
		return false;
	}

	@Override
	public Control getPaneControl(IWorkbenchPartSite site) {
		return (Control) ((PartSite)site).getModel().getWidget();
	}

}
