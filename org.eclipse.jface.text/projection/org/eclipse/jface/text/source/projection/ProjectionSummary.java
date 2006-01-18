/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source.projection;

import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

/**
 * Strategy for managing annotation summaries for collapsed ranges.
 *
 * @since 3.0
 */
class ProjectionSummary {

	private class Summarizer extends Thread {

		private boolean fReset= true;

		/**
		 * Creates a new thread.
		 *
		 * @param monitor the progress monitor
		 */
		public Summarizer(IProgressMonitor monitor) {
			fProgressMonitor= monitor;
			setDaemon(true);
			start();
		}

		/**
		 * Resets the thread.
		 */
		public void reset() {
			fReset= true;
		}

		/*
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while (true) {
				synchronized (fLock) {
					if (!fReset)
						break;
					fReset= false;
				}
				internalUpdateSummaries(fProgressMonitor);
			}

			synchronized (fLock) {
				fSummarizer= null;
			}
		}
	}


	private ProjectionViewer fProjectionViewer;
	private IAnnotationModel fAnnotationModel;
	private IAnnotationAccess fAnnotationAccess;
	private List fConfiguredAnnotationTypes;

	private Object fLock= new Object();
	private IProgressMonitor fProgressMonitor;
	private volatile Summarizer fSummarizer;

	/**
	 * Creates a new projection summary.
	 *
	 * @param projectionViewer the projection viewer
	 * @param annotationAccess the annotation access
	 */
	public ProjectionSummary(ProjectionViewer projectionViewer, IAnnotationAccess annotationAccess) {
		super();
		fProjectionViewer= projectionViewer;
		fAnnotationAccess= annotationAccess;
	}

	/**
	 * Adds the given annotation type. For now on, annotations of that type are
	 * also reflected in their enclosing collapsed regions.
	 *
	 * @param annotationType the annotation type to add
	 */
	public void addAnnotationType(String annotationType) {
		synchronized(fLock) {
			if (fConfiguredAnnotationTypes == null) {
				fConfiguredAnnotationTypes= new ArrayList();
				fConfiguredAnnotationTypes.add(annotationType);
			} else if (!fConfiguredAnnotationTypes.contains(annotationType))
				fConfiguredAnnotationTypes.add(annotationType);
		}
	}

	/**
	 * Removes the given annotation. Annotation of that type are no
	 * longer reflected in their enclosing collapsed region.
	 *
	 * @param annotationType the annotation type to remove
	 */
	public void removeAnnotationType(String annotationType) {
		synchronized (fLock) {
			if (fConfiguredAnnotationTypes != null) {
				fConfiguredAnnotationTypes.remove(annotationType);
				if (fConfiguredAnnotationTypes.size() == 0)
					fConfiguredAnnotationTypes= null;
			}
		}
	}

	/**
	 * Forces an updated of the annotation summary.
	 *
	 * @param monitor the progress monitor
	 */
	public void updateSummaries(IProgressMonitor monitor) {
		synchronized (fLock) {
			if (fConfiguredAnnotationTypes != null) {
				if (fSummarizer == null)
					fSummarizer= new Summarizer(monitor);
				fSummarizer.reset();
			}
		}
	}

	private void internalUpdateSummaries(IProgressMonitor monitor) {

		Object previousLockObject= null;
		fAnnotationModel= fProjectionViewer.getVisualAnnotationModel();
		if (fAnnotationModel == null)
			return;

		try {


			IDocument document= fProjectionViewer.getDocument();
			if (document instanceof ISynchronizable && fAnnotationModel instanceof ISynchronizable) {
				ISynchronizable sync= (ISynchronizable) fAnnotationModel;
				previousLockObject= sync.getLockObject();
				sync.setLockObject(((ISynchronizable) document).getLockObject());
			}


			removeSummaries(monitor);
			createSummaries(monitor);

		} finally {

			if (fAnnotationModel instanceof ISynchronizable) {
				ISynchronizable sync= (ISynchronizable) fAnnotationModel;
				sync.setLockObject(previousLockObject);
			}
			fAnnotationModel= null;

		}
	}

	private boolean isCanceled(IProgressMonitor monitor) {
		return monitor != null && monitor.isCanceled();
	}

	private void removeSummaries(IProgressMonitor monitor) {
		IAnnotationModelExtension extension= null;
		List bags= null;

		if (fAnnotationModel instanceof IAnnotationModelExtension) {
			extension= (IAnnotationModelExtension) fAnnotationModel;
			bags= new ArrayList();
		}

		Iterator e= fAnnotationModel.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation annotation= (Annotation) e.next();
			if (annotation instanceof AnnotationBag) {
				if (bags == null)
					fAnnotationModel.removeAnnotation(annotation);
				else
					bags.add(annotation);
			}

			if (isCanceled(monitor))
				return;
		}

		if (bags != null && bags.size() > 0) {
			Annotation[] deletions= new Annotation[bags.size()];
			bags.toArray(deletions);
			if (!isCanceled(monitor))
				extension.replaceAnnotations(deletions, null);
		}
	}

	private void createSummaries(IProgressMonitor monitor) {
		ProjectionAnnotationModel model= fProjectionViewer.getProjectionAnnotationModel();
		if (model == null)
			return;

		Map additions= new HashMap();

		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			ProjectionAnnotation projection= (ProjectionAnnotation) e.next();
			if (projection.isCollapsed()) {
				Position position= model.getPosition(projection);
				if (position != null) {
					IRegion[] summaryRegions= fProjectionViewer.computeCollapsedRegions(position);
					if (summaryRegions != null) {
						Position summaryAnchor= fProjectionViewer.computeCollapsedRegionAnchor(position);
						if (summaryAnchor != null)
							createSummary(additions, summaryRegions, summaryAnchor);
					}
				}
			}

			if (isCanceled(monitor))
				return;
		}

		if (additions.size() > 0) {
			if (fAnnotationModel instanceof IAnnotationModelExtension) {
				IAnnotationModelExtension extension= (IAnnotationModelExtension) fAnnotationModel;
				if (!isCanceled(monitor))
					extension.replaceAnnotations(null, additions);
			} else {
				Iterator e1= additions.keySet().iterator();
				while (e1.hasNext()) {
					AnnotationBag bag= (AnnotationBag) e1.next();
					Position position= (Position) additions.get(bag);
					if (isCanceled(monitor))
						return;
					fAnnotationModel.addAnnotation(bag, position);
				}
			}
		}
	}

	private void createSummary(Map additions, IRegion[] summaryRegions, Position summaryAnchor) {

		int size= 0;
		Map map= null;

		synchronized (fLock) {
			if (fConfiguredAnnotationTypes != null) {
				size= fConfiguredAnnotationTypes.size();
				map= new HashMap();
				for (int i= 0; i < size; i++) {
					String type= (String) fConfiguredAnnotationTypes.get(i);
					map.put(type, new AnnotationBag(type));
				}
			}
		}

		if (map == null)
			return;

		IAnnotationModel model= fProjectionViewer.getAnnotationModel();
		if (model == null)
			return;
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation annotation= (Annotation) e.next();
			AnnotationBag bag= findBagForType(map, annotation.getType());
			if (bag != null) {
				Position position= model.getPosition(annotation);
				if (includes(summaryRegions, position))
					bag.add(annotation);
			}
		}

		for (int i= 0; i < size; i++) {
			AnnotationBag bag= (AnnotationBag) map.get(fConfiguredAnnotationTypes.get(i));
			if (!bag.isEmpty())
				additions.put(bag, new Position(summaryAnchor.getOffset(), summaryAnchor.getLength()));
		}
	}

	private AnnotationBag findBagForType(Map bagMap, String annotationType) {
		AnnotationBag bag= (AnnotationBag) bagMap.get(annotationType);
		if (bag == null && fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] superTypes= extension.getSupertypes(annotationType);
			for (int i= 0; i < superTypes.length && bag == null; i++) {
				bag= (AnnotationBag) bagMap.get(superTypes[i]);
			}
		}
		return bag;
	}

	private boolean includes(IRegion[] regions, Position position) {
		for (int i= 0; i < regions.length; i++) {
			IRegion region= regions[i];
			if (position != null && !position.isDeleted())
				return region.getOffset() <= position.getOffset() &&  position.getOffset() + position.getLength() <= region.getOffset() + region.getLength();
		}
		return false;
	}
}
