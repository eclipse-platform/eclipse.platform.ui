/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.expression.workingset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Manages expression working set filters for all expression views in the workbench.
 * <p>
 * It saves info about which working sets are applied to each expression view,
 * and restores the info and filter when an expression view is opened. 
 * 
 * @since 3.9		
 */
public class ExpressionWorkingSetFilterManager implements IPropertyChangeListener
{
	
	private static final String PREF_WORKINGSETS = "expressionWorkingSets"; //$NON-NLS-1$
	private static final String ELEMENT_WORKINGSETS = "expressionWorkingsets"; //$NON-NLS-1$
	private static final String ELEMENT_EXPRESSIONVIEW = "expressionView"; //$NON-NLS-1$
	private static final String ELEMENT_WORKINGSET = "workingSet"; //$NON-NLS-1$

	private static ExpressionWorkingSetFilterManager INSTANCE;
	
	private static boolean fInitialized = false;
	
	private ExpressionWorkingSetFilterManager()
	{
		
	}
	
	synchronized public static ExpressionWorkingSetFilterManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExpressionWorkingSetFilterManager();
		}
		return INSTANCE;
	}
	
	synchronized public void init() {
		if (fInitialized) return;
		fInitialized = true;
		
		initListeners();
		
		final XMLMemento fworkingSetsMemento = getMemento();
		
		new WorkbenchJob("Initializing expression view working sets") { //$NON-NLS-1$
			{ setSystem(true); }
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (int i=0; i<windows.length; i++) {
					IWorkbenchWindow window = windows[i];
					IViewReference[] viewRefs = window.getActivePage().getViewReferences();
					for (int j=0; j<viewRefs.length; j++) {
						IViewReference viewRef = viewRefs[j];
						try {
							if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(viewRef.getId()))
							{
								IViewPart expressionView = viewRef.getView(false);
								if (expressionView != null)
									applyFilter(expressionView, fworkingSetsMemento);
							}
						}
						finally {
							
						}
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	private void applyFilter(IViewPart expressionView, XMLMemento workingSetsMemento)
	{
		String mementoID = expressionView.getViewSite().getId() + 
							expressionView.getViewSite().getSecondaryId();

		List workingSets = new ArrayList();

		if (workingSetsMemento != null)
		{
			IMemento[] viewMementos = workingSetsMemento.getChildren(ELEMENT_EXPRESSIONVIEW);
			for (int i=0; i<viewMementos.length; i++) {
				IMemento viewMemento = viewMementos[i];
				if (mementoID.equals(viewMemento.getID())) {
					IMemento[] workingsetMementos = viewMemento.getChildren(ELEMENT_WORKINGSET);
					for (int j=0; j<workingsetMementos.length; j++) {
						IMemento workingSetMemento = workingsetMementos[j];
						String workingSetName = workingSetMemento.getID();
						IWorkingSet workingSet = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
						if (workingSet != null)
							workingSets.add(workingSet);
					}
					
					break;
				}
			}
		}
		
		applyWorkingSets((ExpressionView) expressionView, (IWorkingSet[]) workingSets.toArray(new IWorkingSet[0]));
	}
	
	private void initListeners() {
		IWorkbench wb = PlatformUI.getWorkbench();
		
		final IPartListener2 partListener = new IPartListener2() {

			public void partVisible(IWorkbenchPartReference partRef) {}					

			public void partInputChanged(IWorkbenchPartReference partRef) {}						

			public void partHidden(IWorkbenchPartReference partRef) {}						

			public void partDeactivated(IWorkbenchPartReference partRef) {}																		

			public void partBroughtToTop(IWorkbenchPartReference partRef) {}						

			public void partActivated(IWorkbenchPartReference partRef) {}

			public void partOpened(IWorkbenchPartReference partRef) {
				if (partRef instanceof IViewReference) {
					if (! partRef.getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW))
						return;
					IViewPart part = ((IViewReference) partRef).getView(false);
					if (part != null)
					{
						applyFilter(part, getMemento());
					}
				}
			}
			
			public void partClosed(IWorkbenchPartReference partRef) {}
		};
		// subscribe to existing workbench window listener
		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();
		for (int i=0; i<windows.length; i++) {
			IWorkbenchWindow ww = windows[i];
			ww.getPartService().addPartListener(partListener);
		}
		
		// subscribe to new workbench window listener
		wb.addWindowListener(new IWindowListener() {					

			public void windowDeactivated(IWorkbenchWindow window) {}												

			public void windowActivated(IWorkbenchWindow window) {}				

			public void windowClosed(IWorkbenchWindow window) {}
			
			public void windowOpened(IWorkbenchWindow window) {
				window.getPartService().addPartListener(partListener);
			}		
		});
		
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(this);
	}
	
	private static XMLMemento getMemento()
	{
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		String workingSetsMementoRaw = prefStore.getString(PREF_WORKINGSETS);
		workingSetsMementoRaw = workingSetsMementoRaw.trim();
		if (workingSetsMementoRaw.length() == 0)
			return null;
		XMLMemento workingSetsMemento = null;
		try {
			workingSetsMemento = XMLMemento.createReadRoot(new StringReader(workingSetsMementoRaw));
		} catch (WorkbenchException e) {
			DebugUIPlugin.log(e);
		}
		return workingSetsMemento;
	}
	
	public static void applyWorkingSets(ExpressionView exprView, IWorkingSet[] selectedWorkingSets)
	{
        ExpressionWorkingSetFilter workingSetFilter = getFilter(exprView);

        workingSetFilter.setSelectedWorkingSets(selectedWorkingSets);
        
        exprView.getViewer().refresh();
        
        saveWorkingSets(exprView, selectedWorkingSets);
	}
	
	private static ExpressionWorkingSetFilter getFilter(ExpressionView exprView)
	{
        ExpressionWorkingSetFilter workingSetFilter = null;
        
        ViewerFilter[] existingFilters = ((TreeModelViewer) exprView.getViewer()).getFilters();
        for (int i=0; i<existingFilters.length; i++) {
        	ViewerFilter existingFilter = existingFilters[i];
        	if (existingFilter instanceof ExpressionWorkingSetFilter)
        	{
        		workingSetFilter = (ExpressionWorkingSetFilter) existingFilter;
        		break;
        	}
        }
        
        if (workingSetFilter == null)
        {
        	workingSetFilter = new ExpressionWorkingSetFilter();
        	((TreeModelViewer) exprView.getViewer()).addFilter(workingSetFilter);
        }

        return workingSetFilter;
	}
	
	private static ExpressionView[] getExpressionViews()
	{
		List expressionViews = new ArrayList();
		
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i=0; i<windows.length; i++) {
			IWorkbenchWindow window = windows[i];
			IViewReference[] viewRefs = window.getActivePage().getViewReferences();
			for (int j=0; j<viewRefs.length; j++) {
				IViewReference viewRef = viewRefs[j];
				if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(viewRef.getId())) {
					IViewPart expressionView = viewRef.getView(false);
					if (expressionView != null)
						expressionViews.add(expressionView);
				}
			}
		}
		
		return (ExpressionView[]) expressionViews.toArray(new ExpressionView[0]);
	}
	
	public static IWorkingSet[] getWorkingSets(ExpressionView exprView)
	{
		ExpressionWorkingSetFilter workingSetFilter = getFilter(exprView);
		if (workingSetFilter == null)
			return null;
		
		return workingSetFilter.getSelectedWorkingSets();
	}
	
	private static void saveWorkingSets(ExpressionView exprView, IWorkingSet[] selectedWorkingSets)
	{
		String mementoID = exprView.getViewSite().getId() +
							exprView.getViewSite().getSecondaryId();
		
		XMLMemento rootMemento = XMLMemento.createWriteRoot(ELEMENT_WORKINGSETS);
		
		XMLMemento savedMemento = getMemento();
		
		if (savedMemento != null) {
			IMemento[] viewMementos = savedMemento.getChildren(ELEMENT_EXPRESSIONVIEW);
			for (int i=0; i<viewMementos.length; i++) {
				IMemento savedViewMemento = viewMementos[i];
				if (!mementoID.equals(savedViewMemento.getID()))
				{
					IMemento newViewMemento = rootMemento.createChild(ELEMENT_EXPRESSIONVIEW, savedViewMemento.getID());
					
					IMemento[] savedWorkingSetMementos = savedViewMemento.getChildren(ELEMENT_WORKINGSET);
					for (int j=0; j<savedWorkingSetMementos.length; j++) {
						IMemento savedWorkingSetMemento = savedWorkingSetMementos[j];
						newViewMemento.createChild(ELEMENT_WORKINGSET, savedWorkingSetMemento.getID());
					}
				}
			}
		}
		
		IMemento viewMemento = rootMemento.createChild(ELEMENT_EXPRESSIONVIEW, mementoID);
		
		for (int i=0; i<selectedWorkingSets.length; i++) {
			IWorkingSet workingSet = selectedWorkingSets[i];
			viewMemento.createChild(ELEMENT_WORKINGSET, workingSet.getName());
		}
		
		ByteArrayOutputStream mementoOutputStream = new ByteArrayOutputStream();
		try {
			rootMemento.save(new OutputStreamWriter(mementoOutputStream));
		} catch (IOException e) {
			DebugUIPlugin.log(e);
		}
		
		String workingSetsMementoRaw = mementoOutputStream.toString();
		
		IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		prefStore.setValue(PREF_WORKINGSETS, workingSetsMementoRaw);
	}
	
//	private static IWorkingSet[] getExpressionWorkingSets()
//	{
//		List expressionWorkingSets = new ArrayList();
//		
//		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
//		IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
//		for (int i=0; i<workingSets.length; i++) {
//			IWorkingSet workingSet = workingSets[i];
//			if (workingSet.getId().equals(IExpressionWorkingSetConstants.EXPRESSION_WORKINGSET_ID))
//			{
//				expressionWorkingSets.add(workingSet);
//			}
//		}
//		
//		return (IWorkingSet[]) expressionWorkingSets.toArray(new IWorkingSet[0]);
//	}

	public void propertyChange(PropertyChangeEvent event) {
		if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(event.getProperty()))
		{
			IWorkingSet removedWorkingSet = (IWorkingSet) event.getOldValue();
			
			ExpressionView[] views = getExpressionViews();
			for (int i=0; i<views.length; i++) {
				ExpressionView expressionView = views[i];
				IWorkingSet[] appliedWorkingSets = getWorkingSets(expressionView);
				if (appliedWorkingSets == null)
					continue;
				if (appliedWorkingSets.length == 0)
					continue;
				
				List remainingWorkingSets = new ArrayList();
				
				boolean isRemoved = false;
				for (int j=0; j<appliedWorkingSets.length; j++) {
					IWorkingSet appliedWorkingSet = appliedWorkingSets[j];
					if (removedWorkingSet.getName().equals(appliedWorkingSet.getName()))
					{
						isRemoved = true;
						continue;
					}
					remainingWorkingSets.add(appliedWorkingSet);
				}
				
				if (isRemoved)
					applyWorkingSets(expressionView, (IWorkingSet[]) remainingWorkingSets.toArray(new IWorkingSet[0]));
			}
		}
		else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(event.getProperty()))
		{
			IWorkingSet changedWorkingSet = (IWorkingSet) event.getNewValue();
			
			ExpressionView[] views = getExpressionViews();
			for (int i=0; i<views.length; i++) {
				ExpressionView expressionView = views[i];
				IWorkingSet[] appliedWorkingSets = getWorkingSets(expressionView);
				if (appliedWorkingSets == null)
					continue;
				if (appliedWorkingSets.length == 0)
					continue;
				
				List remainingWorkingSets = new ArrayList();
				
				boolean isChanged = false;
				for (int j=0; j<appliedWorkingSets.length; j++) {
					IWorkingSet appliedWorkingSet = appliedWorkingSets[j];
					if (changedWorkingSet.getName().equals(appliedWorkingSet.getName()))
					{
						isChanged = true;
					}
					remainingWorkingSets.add(appliedWorkingSet);
				}
				
				if (isChanged)
					applyWorkingSets(expressionView, (IWorkingSet[]) remainingWorkingSets.toArray(new IWorkingSet[0]));
			}
		}
		
	}
	
	
}
