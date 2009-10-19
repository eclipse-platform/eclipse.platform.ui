/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.internal.MarkerGroup;

/**
 * The CachedMarkerBuilder is the object that generates the list of markers from
 * a generator.
 * 
 * @since 3.4
 * 
 */
public class CachedMarkerBuilder {

	private static final String TAG_CATEGORY_GROUP = "categoryGroup"; //$NON-NLS-1$
	private static final String VALUE_NONE = "none"; //$NON-NLS-1$

	
	// The MarkerContentGenerator we are using for building
	private MarkerContentGenerator generator; 
	private MarkerUpdateJob updateJob;
	private MarkersChangeListener markerListener;
	private Markers markers;
	private Markers markersClone;
	
	final Object CACHE_UPDATE_FAMILY = new Object();
	final Object MARKERSVIEW_UPDATE_JOB_FAMILY;
	
	private IWorkbenchSiteProgressService progressService;

	private MarkerGroup categoryGroup;
	
	private MarkerComparator comparator;
	
	private boolean[] changeFlags;

	private IPropertyChangeListener workingSetListener;

	private boolean active;
	
	private IMemento memento;

	/**
	 * Create a new instance of the receiver. Update using the updateJob.
	 * @param view 
	 */
	public CachedMarkerBuilder(ExtendedMarkersView
			view) {
		active=false;
		MARKERSVIEW_UPDATE_JOB_FAMILY=view.MARKERSVIEW_UPDATE_JOB_FAMILY;
		markers = new Markers(this);
		markerListener= new MarkersChangeListener(view,this);
		changeFlags=new boolean[]{true,false,false};
	}
	
	void restoreState(IMemento memento) {
		if (memento == null)
			setDefaultCategoryGroup(getGenerator());
		else {
			// Set up the category group if it has been set or set a default.
			String categoryGroupID = memento.getString(TAG_CATEGORY_GROUP);
			if (categoryGroupID == null)
				setDefaultCategoryGroup(getGenerator());
			else {
				if (categoryGroupID.equals(VALUE_NONE))
					this.categoryGroup = null;
				else {
					MarkerGroup newGroup = getGenerator().getMarkerGroup(
							categoryGroupID);
					if (newGroup == null)
						setDefaultCategoryGroup(getGenerator());
					else
						this.categoryGroup = newGroup;
				}
			}
		}
		this.memento=memento;
	}
	/**
	 * 
	 */
	void start() {
		active=true;
		PlatformUI.getWorkbench().getWorkingSetManager()
				.addPropertyChangeListener(getWorkingSetListener());
		scheduleUpdate();
	}

	/**
	 * Dispose any listeners in the receiver.
	 */
	void dispose() {
		markerListener.stop();
		active=false;
		if(workingSetListener!=null){
			PlatformUI.getWorkbench().getWorkingSetManager()
			.removePropertyChangeListener(getWorkingSetListener());
		}
	}

	/**
	 * Return the group used to generate categories.
	 * 
	 * @return MarkerGroup or <code>null</code>.
	 */
	MarkerGroup getCategoryGroup() {
		return categoryGroup;
	}

	/**
	 * Return a new instance of the receiver with the field
	 * 
	 * @return MarkerComparator
	 */
	MarkerComparator getComparator() {
		if(comparator==null){
			MarkerField field = null;
			if (getCategoryGroup() != null)
				field = getCategoryGroup().getMarkerField();
			comparator = new MarkerComparator(field, generator.getAllFields());
			if (memento != null) {
				comparator.restore(memento);
			}
		}
		return comparator;
	}

	/**
	 * Return the generator for the receiver.
	 * 
	 * @return MarkerContentGenerator
	 */
	MarkerContentGenerator getGenerator() {
		return generator;
	}

	/**
	 * Return the primary sort field
	 * 
	 * @return MarkerField
	 */
	MarkerField getPrimarySortField() {
		return getComparator().getPrimarySortField();
	}

	/**
	 * Get the sort direction of field
	 * 
	 * @param field
	 * @return int one of {@link MarkerComparator#ASCENDING} or
	 *         {@link MarkerComparator#DESCENDING}
	 */
	int getSortDirection(MarkerField field) {
		if (getComparator().descendingFields.contains(field))
			return MarkerComparator.DESCENDING;
		return MarkerComparator.ASCENDING;
	}

	/**
	 * Return the total number of markers.
	 * 
	 * @return int
	 */
	int getTotalMarkerCount() {
		return getTotalMarkerCount(getMarkers());
	}

	/**
	 * Return the total number of markers.
	 * 
	 * @return int
	 */
	int getTotalMarkerCount(Markers markers) {
		MarkerSupportItem[] elements = markers.getElements();
		if (elements.length == 0 || elements[0].isConcrete())
			return elements.length;
		int length = 0;
		for (int i = 0; i < elements.length; i++) {
			length += elements[i].getChildren().length;
		}

		return length;
	}

	/**
	 * Return whether or not the receiver is building.
	 * 
	 * @return boolean
	 */
	boolean isBuilding() {
		return updateJob != null ? updateJob.isBuilding()
				|| updateJob.getState() != Job.NONE : false;
	}

	/**
	 * Return whether or not we are showing a hierarchy,.
	 * 
	 * @return <code>true</code> if a hierarchy is being shown.
	 */
	boolean isShowingHierarchy() {
		return categoryGroup != null;
	}

	/**
	 * Refresh the sort order and categories of the receiver.
	 * 
	 */
	void refreshContents(IWorkbenchSiteProgressService service) {
		try {
			service.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					SortingJob job=new SortingJob(CachedMarkerBuilder.this);
					job.run(monitor);
				}
			});
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e));
		} catch (InterruptedException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e));
		}
	}
	/**
	 * Refresh the sort order and categories of the receiver.
	 * 
	 */
	void refreshContents() {
		SortingJob job=new SortingJob(CachedMarkerBuilder.this);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(true);
		if (progressService != null) {
			progressService.schedule(job, MarkersChangeListener.SHORT_DELAY);
		} else {
			job.schedule(MarkersChangeListener.SHORT_DELAY);
		}
	}
	
	
	/**
	 * Save the state of the receiver to memento
	 * 
	 * @param memento
	 */
	void saveState(IMemento memento) {
		getComparator().saveState(memento);
		if (categoryGroup == null)
			memento.putString(TAG_CATEGORY_GROUP, VALUE_NONE);
		else
			memento.putString(TAG_CATEGORY_GROUP, getCategoryGroup().getId());
	}

	/**
	 * Schedule an update of the markers with a delay.
	 * 
	 */
	void scheduleUpdate() {
		if (active) {
			markerListener.scheduleUpdate(MarkersChangeListener.SHORT_DELAY,changeFlags);
		}
	}
	/**
	 * Schedule an update of the markers with a delay.
	 * 
	 */
	void scheduleUpdate(long delay) {
		if (active) {
			markerListener.scheduleUpdate(delay,changeFlags);
		}
	}


	/**
	 * Set the category group.
	 * 
	 * @param group
	 *            {@link MarkerGroup} or <code>null</code>.
	 */
	void setCategoryGroup(MarkerGroup group) {
		this.categoryGroup = group;
		if (group == null)
			getComparator().setCategory(null);
		else
			getComparator().setCategory(group.getMarkerField());
		scheduleUpdate();

	}

	/**
	 * Categorise by the default setting for contentGenerator.
	 * 
	 * @param contentGenerator
	 */
	private void setDefaultCategoryGroup(MarkerContentGenerator contentGenerator) {
		String categoryName = contentGenerator.getCategoryName();
		if (categoryName != null) {
			MarkerGroup group = contentGenerator.getMarkerGroup(categoryName);
			if (group != null)
				categoryGroup = group;
		}

	}

	/**
	 * Set the generator and update the contents.
	 * 
	 * @param newGenerator
	 */
	void setGenerator(MarkerContentGenerator newGenerator) {
		generator = newGenerator;
		if (generator.getBuilder() != this) {
			generator.setBuilder(this);
		}
		setDefaultCategoryGroup(generator);
		scheduleUpdate();
	}

	/**
	 * Set the primary sort field for the receiver.
	 * 
	 * @param field
	 */
	void setPrimarySortField(MarkerField field) {

		getComparator().setPrimarySortField(field);

	}

	/**
	 * Set the progress service for the receiver.
	 * 
	 * @param service
	 */
	void setProgressService(IWorkbenchSiteProgressService service) {
		progressService = service;
		if (service != null) {
			service
					.showBusyForFamily(ResourcesPlugin.FAMILY_MANUAL_BUILD);
			service
					.showBusyForFamily(ResourcesPlugin.FAMILY_AUTO_BUILD);
			service
					.showBusyForFamily(CACHE_UPDATE_FAMILY);
			service
					.showBusyForFamily(MARKERSVIEW_UPDATE_JOB_FAMILY);
		}
	}
	/**
	 * @return Returns the progressService.
	 */
	IWorkbenchSiteProgressService getProgressService() {
		return progressService;
	}
	
	/**
	 * schedules marker update job
	 */
	MarkerUpdateJob scheduleUpdateJob(long delay) {
		return scheduleUpdateJob(delay, false, new boolean[] { true, false,
				false });
	}
	/**
	 * schedules marker update job
	 */
	 MarkerUpdateJob scheduleUpdateJob(long delay, boolean clean) {
		return scheduleUpdateJob(delay, clean,new boolean[] { true, false,
				false });
	}

	/**
	 * schedules marker update job
	 */
	synchronized MarkerUpdateJob scheduleUpdateJob(long delay,
			boolean clean, boolean[] changeFlags) {
		if (generator == null || !active) {
			return null;
		}
		if (updateJob != null) {
			//ensure cancellation before calling the method
			//updateJob.cancel();
		} else {
			/*
			 * unfortunately marker operations cannot be locked so locking
			 * between gathering of markers and marker deltas is not possible we
			 * have the very simple code (the 3 handle* methods in
			 * MarkersChangeListener) addition to take care of this anyway
			 */
			// updateJob = new MarkerUpdateJob(this);
			updateJob = new MarkerUpdateJob(this, false);
			updateJob.setPriority(Job.LONG);
			updateJob.setSystem(true);
		}
		for (int i = 0; i < changeFlags.length; i++) {
			this.changeFlags[i]=this.changeFlags[i]|changeFlags[i];
		}
		if (clean) {
			updateJob.setClean();
		}
		if (progressService != null) {
			progressService.schedule(updateJob, delay);
		} else {
			updateJob.schedule(delay);
		}
		return updateJob;
	}
	
	synchronized void cancelUpdate(boolean block) {
		if (updateJob != null) {
			updateJob.cancel();
			if (block) {
				try {
					Display display = Display.getCurrent();
					if (display == null) {
						//the update job depends on UI to finish
						display = Display.getDefault();
						if (display.getSyncThread() != Thread.currentThread()) {
							updateJob.join();
						}
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}
	/**
	 * @return Returns the {@link MarkersChangeListener}.
	 */
	MarkersChangeListener getMarkerListener() {
		return markerListener;
	}
	/**
	 * @param {@link MarkersChangeListener} The MarkersChangeListener to set.
	 */
	void setMarkerListener(MarkersChangeListener markerListener) {
		this.markerListener = markerListener;
	}

	/**
	 * @return Returns the markers.
	 */
	Markers getMarkers() {
		return markers;
	}
	/**
	 * Create a listener for working set changes.
	 * 
	 * @return IPropertyChangeListener
	 */
	private IPropertyChangeListener getWorkingSetListener() {
		if (workingSetListener == null) {
			workingSetListener = new WorkingSetListener();
		}
		return workingSetListener;
	}
	
	/**
	 * The WorkingSet listener, since marker filters can be scoped to
	 * workingsets, listenen for changes to them
	 *
	 */
	private class WorkingSetListener implements IPropertyChangeListener{
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange
		 * (org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			boolean needsUpdate=false;
			if (event
					.getProperty()
					.equals(
							IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
				Iterator iterator = generator.getEnabledFilters()
						.iterator();
				while (iterator.hasNext()) {
					MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) iterator
							.next();
					if (group.getScope() == MarkerFieldFilterGroup.ON_WORKING_SET) {
						IWorkingSet wSet = group.getWorkingSet();
						if (wSet!=null&&wSet.equals(event.getNewValue())) {
							group.refresh();
							needsUpdate=true;
						}
					}
				}
			}
			if (event.getProperty().equals(
					IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
				Iterator iterator = generator.getAllFilters().iterator();
				while (iterator.hasNext()) {
					MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) iterator
							.next();
					if (group.getScope() == MarkerFieldFilterGroup.ON_WORKING_SET) {
						IWorkingSet wSet = group.getWorkingSet();
						if (wSet!=null && wSet.equals(event.getOldValue())) {
							group.setWorkingSet(null);// working set
							group.refresh();
							needsUpdate=true;
						}
					}
				}
			}
			if (needsUpdate) {
				scheduleUpdate();
			}
		}
	}
	
	/**
	 * Get the name for the preferences for the receiver.
	 * 
	 * @return String
	 */
	static String getMementoPreferenceName(String viewId) {
		return CachedMarkerBuilder.class.getName() + viewId;
	}

	/**
	 * @return Returns true if active.
	 */
	boolean isActive() {
		return active;
	}

	/**
	 * @return Returns the changeFlags {added,removed,changed}.
	 */
	synchronized boolean[] readChangeFlags() {
		boolean [] changes=new boolean[changeFlags.length];
		for (int i = 0; i < changes.length; i++) {
			changes[i]=changeFlags[i];
			changeFlags[i]=false;
		}
		return changes;
	}
	/**
	 * @return lastUiRefreshTime
	 * 
	 */
	long getLastUpdateTime() {
		if (updateJob != null) {
			return updateJob.getLastUpdateTime();
		}
		return -1;
	}

	/**
	 * Always work with a clone where thread safety is concerned
	 * @return the active clone of markers
	 */
	public Markers getClonedMarkers() {
		if(markersClone==null){
			//this should not happen ideally,
			//lets ensure safety anyways
			markersClone=markers.getClone();
		}
		return markersClone;
	}

	/**
	 * Create a new clone of Markers
	 */
	 Markers createMarkersClone() {
		markersClone =markers.getClone();
		return markersClone;
	}
}
