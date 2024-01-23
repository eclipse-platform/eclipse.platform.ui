/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text.source.projection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.text.IRegion;
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
		 */
		public Summarizer() {
			fProgressMonitor= new NullProgressMonitor(); // might be given by client in the future
			setDaemon(true);
			start();
		}

		/**
		 * Resets the thread.
		 */
		public void reset() {
			synchronized (fLock) {
				fReset= true;
				fProgressMonitor.setCanceled(true);
			}
		}

		@Override
		public void run() {
			while (true) {
				synchronized (fLock) {
					if (!fReset)
						break;
					fReset= false;
					fProgressMonitor.setCanceled(false);
				}
				internalUpdateSummaries(fProgressMonitor);
			}

			synchronized (fLock) {
				fSummarizer= null;
			}
		}
	}


	private ProjectionViewer fProjectionViewer;
	private IAnnotationAccess fAnnotationAccess;
	private List<String> fConfiguredAnnotationTypes;

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
				fConfiguredAnnotationTypes= new ArrayList<>();
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
				if (fConfiguredAnnotationTypes.isEmpty())
					fConfiguredAnnotationTypes= null;
			}
		}
	}

	/**
	 * Forces an updated of the annotation summary.
	 */
	public void updateSummaries() {
		synchronized (fLock) {
			if (fConfiguredAnnotationTypes != null) {
				if (fSummarizer == null)
					fSummarizer= new Summarizer();
				fSummarizer.reset();
			}
		}
	}

	private void internalUpdateSummaries(IProgressMonitor monitor) {
		IAnnotationModel visualAnnotationModel= fProjectionViewer.getVisualAnnotationModel();
		if (visualAnnotationModel == null)
			return;

		removeSummaries(monitor, visualAnnotationModel);

		if (isCanceled(monitor))
			return;

		createSummaries(monitor, visualAnnotationModel);
	}

	private boolean isCanceled(IProgressMonitor monitor) {
		return monitor != null && monitor.isCanceled();
	}

	private void removeSummaries(IProgressMonitor monitor, IAnnotationModel visualAnnotationModel) {
		IAnnotationModelExtension extension= null;
		List<Annotation> bags= null;

		if (visualAnnotationModel instanceof IAnnotationModelExtension) {
			extension= (IAnnotationModelExtension)visualAnnotationModel;
			bags= new ArrayList<>();
		}

		Iterator<Annotation> e= visualAnnotationModel.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation annotation= e.next();
			if (annotation instanceof AnnotationBag) {
				if (bags == null)
					visualAnnotationModel.removeAnnotation(annotation);
				else
					bags.add(annotation);
			}

			if (isCanceled(monitor))
				return;
		}

		if (bags != null && !bags.isEmpty() && extension != null) {
			Annotation[] deletions= new Annotation[bags.size()];
			bags.toArray(deletions);
			if (!isCanceled(monitor))
				extension.replaceAnnotations(deletions, null);
		}
	}

	private void createSummaries(IProgressMonitor monitor, IAnnotationModel visualAnnotationModel) {
		ProjectionAnnotationModel model= fProjectionViewer.getProjectionAnnotationModel();
		if (model == null)
			return;

		Map<Annotation, Position> additions= new HashMap<>();

		Iterator<Annotation> e= model.getAnnotationIterator();
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

		if (!additions.isEmpty()) {
			if (visualAnnotationModel instanceof IAnnotationModelExtension) {
				IAnnotationModelExtension extension= (IAnnotationModelExtension)visualAnnotationModel;
				if (!isCanceled(monitor))
					extension.replaceAnnotations(null, additions);
			} else {
				for (Entry<Annotation, Position> entry : additions.entrySet()) {
					AnnotationBag bag= (AnnotationBag) entry.getKey();
					Position position= entry.getValue();
					if (isCanceled(monitor))
						return;
					visualAnnotationModel.addAnnotation(bag, position);
				}
			}
		}
	}

	private void createSummary(Map<Annotation, Position> additions, IRegion[] summaryRegions, Position summaryAnchor) {

		int size= 0;
		Map<String, AnnotationBag> map= null;

		synchronized (fLock) {
			if (fConfiguredAnnotationTypes != null) {
				size= fConfiguredAnnotationTypes.size();
				map= new HashMap<>();
				for (int i= 0; i < size; i++) {
					String type= fConfiguredAnnotationTypes.get(i);
					map.put(type, new AnnotationBag(type));
				}
			}
		}

		if (map == null)
			return;

		IAnnotationModel model= fProjectionViewer.getAnnotationModel();
		if (model == null)
			return;
		Iterator<Annotation> e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation annotation= e.next();
			AnnotationBag bag= findBagForType(map, annotation.getType());
			if (bag != null) {
				Position position= model.getPosition(annotation);
				if (includes(summaryRegions, position))
					bag.add(annotation);
			}
		}

		for (int i= 0; i < size; i++) {
			AnnotationBag bag= map.get(fConfiguredAnnotationTypes.get(i));
			if (!bag.isEmpty())
				additions.put(bag, new Position(summaryAnchor.getOffset(), summaryAnchor.getLength()));
		}
	}

	private AnnotationBag findBagForType(Map<String, AnnotationBag> bagMap, String annotationType) {
		AnnotationBag bag= bagMap.get(annotationType);
		if (bag == null && fAnnotationAccess instanceof IAnnotationAccessExtension) {
			IAnnotationAccessExtension extension= (IAnnotationAccessExtension) fAnnotationAccess;
			Object[] superTypes= extension.getSupertypes(annotationType);
			for (int i= 0; i < superTypes.length && bag == null; i++) {
				bag= bagMap.get(superTypes[i]);
			}
		}
		return bag;
	}

	private boolean includes(IRegion[] regions, Position position) {
		for (IRegion region : regions) {
			if (position != null && !position.isDeleted()
					&& region.getOffset() <= position.getOffset() &&  position.getOffset() + position.getLength() <= region.getOffset() + region.getLength())
				return true;
		}
		return false;
	}
}
