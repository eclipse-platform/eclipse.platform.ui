/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer <anton.leherbauer@windriver.com> - [implementation] AnnotationModel.fModificationStamp leaks annotations - http://bugs.eclipse.org/345715
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;


/**
 * Standard implementation of {@link IAnnotationModel} and its extension
 * interfaces. This class can directly be used by clients. Subclasses may adapt
 * this annotation model to other existing annotation mechanisms. This class
 * also implements {@link org.eclipse.jface.text.ISynchronizable}. All
 * modifications of the model's internal annotation map are synchronized using
 * the model's lock object.
 */
public class AnnotationModel implements IAnnotationModel, IAnnotationModelExtension, IAnnotationModelExtension2, ISynchronizable {


	/**
	 * Iterator that returns the annotations for a given region.
	 *
	 * @since 3.4
	 * @see AnnotationModel.RegionIterator#RegionIterator(Iterator, IAnnotationModel, int, int, boolean, boolean)
	 */
	private static final class RegionIterator implements Iterator<Annotation> {

		private final Iterator<Annotation> fParentIterator;
		private final boolean fCanEndAfter;
		private final boolean fCanStartBefore;
		private final IAnnotationModel fModel;
		private Annotation fNext;
		private Position fRegion;

		/**
		 * Iterator that returns all annotations from the parent iterator which
		 * have a position in the given model inside the given region.
		 * <p>
		 * See {@link IAnnotationModelExtension2} for a definition of inside.
		 * </p>
		 *
		 * @param parentIterator iterator containing all annotations
		 * @param model the model to use to retrieve positions from for each
		 *            annotation
		 * @param offset start position of the region
		 * @param length length of the region
		 * @param canStartBefore include annotations starting before region
		 * @param canEndAfter include annotations ending after region
		 * @see IAnnotationModelExtension2
		 */
		public RegionIterator(Iterator<Annotation> parentIterator, IAnnotationModel model, int offset, int length, boolean canStartBefore, boolean canEndAfter) {
			fParentIterator= parentIterator;
			fModel= model;
			fRegion= new Position(offset, length);
			fCanEndAfter= canEndAfter;
			fCanStartBefore= canStartBefore;
			fNext= findNext();
		}

		@Override
		public boolean hasNext() {
			return fNext != null;
		}

		@Override
		public Annotation next() {
			if (!hasNext())
				throw new NoSuchElementException();

			Annotation result= fNext;
			fNext= findNext();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private Annotation findNext() {
			while (fParentIterator.hasNext()) {
				Annotation next= fParentIterator.next();
				Position position= fModel.getPosition(next);
				if (position != null) {
					int offset= position.getOffset();
					if (isWithinRegion(offset, position.getLength()))
						return next;
				}
			}
			return null;
		}

		private boolean isWithinRegion(int start, int length) {
			if (fCanStartBefore && fCanEndAfter)
				return fRegion.overlapsWith(start, length);
			else if (fCanStartBefore)
				return fRegion.includes(start + length - (length > 0 ? 1 : 0));
			else if (fCanEndAfter)
				return fRegion.includes(start);
			else
				return fRegion.includes(start) && fRegion.includes(start + length - (length > 0 ? 1 : 0));
		}
	}

	/**
	 * An iterator iteration over a Positions and mapping positions to
	 * annotations using a provided map if the provided map contains the element.
	 *
	 * @since 3.4
	 */
	private static final class AnnotationsInterator implements Iterator<Annotation> {

		private Annotation fNext;
		private final Position[] fPositions;
		private int fIndex;
		private final Map<Position, Annotation> fMap;

		/**
		 * @param positions positions to iterate over
		 * @param map a map to map positions to annotations
		 */
		public AnnotationsInterator(Position[] positions, Map<Position, Annotation> map) {
			fPositions= positions;
			fIndex= 0;
			fMap= map;
			fNext= findNext();
		}

		@Override
		public boolean hasNext() {
			return fNext != null;
		}

		@Override
		public Annotation next() {
			Annotation result= fNext;
			fNext= findNext();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private Annotation findNext() {
			while (fIndex < fPositions.length) {
				Position position= fPositions[fIndex];
				fIndex++;
				if (fMap.containsKey(position))
					return fMap.get(position);
			}

			return null;
		}
	}

	/**
	 * A single iterator builds its behavior based on a sequence of iterators.
	 *
	 * @param <E> the type of elements returned by this iterator
	 * @since 3.1
	 */
	private static class MetaIterator<E> implements Iterator<E> {

		/** The iterator over a list of iterators. */
		private Iterator<? extends Iterator<? extends E>> fSuperIterator;
		/** The current iterator. */
		private Iterator<? extends E> fCurrent;
		/** The current element. */
		private E fCurrentElement;


		public MetaIterator(Iterator<? extends Iterator<? extends E>> iterator) {
			fSuperIterator= iterator;
			fCurrent= fSuperIterator.next(); // there is at least one.
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext() {
			if (fCurrentElement != null)
				return true;

			if (fCurrent.hasNext()) {
				fCurrentElement= fCurrent.next();
				return true;
			} else if (fSuperIterator.hasNext()) {
				fCurrent= fSuperIterator.next();
				return hasNext();
			} else
				return false;
		}

		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();

			E element= fCurrentElement;
			fCurrentElement= null;
			return element;
		}
	}

	/**
	 * Internal annotation model listener for forwarding annotation model changes from the attached models to the
	 * registered listeners of the outer most annotation model.
	 *
	 * @since 3.0
	 */
	private class InternalModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

		@Override
		public void modelChanged(IAnnotationModel model) {
			AnnotationModel.this.fireModelChanged(new AnnotationModelEvent(model, true));
		}

		@Override
		public void modelChanged(AnnotationModelEvent event) {
			AnnotationModel.this.fireModelChanged(event);
		}
	}

	/**
	 * The list of managed annotations
	 * @deprecated since 3.0 use <code>getAnnotationMap</code> instead
	 */
	@Deprecated
	protected Map<Annotation, Position> fAnnotations;
	/**
	 * The map which maps {@link Position} to {@link Annotation}.
	 * @since 3.4
	 **/
	private IdentityHashMap<Position, Annotation> fPositions;
	/** The list of annotation model listeners */
	protected ArrayList<IAnnotationModelListener> fAnnotationModelListeners;
	/** The document connected with this model */
	protected IDocument fDocument;
	/** The number of open connections to the same document */
	private int fOpenConnections= 0;
	/** The document listener for tracking whether document positions might have been changed. */
	private IDocumentListener fDocumentListener;
	/** The flag indicating whether the document positions might have been changed. */
	private boolean fDocumentChanged= true;
	/**
	 * The model's attachment.
	 * @since 3.0
	 */
	private Map<Object, IAnnotationModel> fAttachments= new HashMap<>();
	/**
	 * The annotation model listener on attached sub-models.
	 * @since 3.0
	 */
	private IAnnotationModelListener fModelListener= new InternalModelListener();
	/**
	 * The current annotation model event.
	 * @since 3.0
	 */
	private AnnotationModelEvent fModelEvent;
	/**
	 * The modification stamp.
	 * @since 3.0
	 */
	private Object fModificationStamp= new Object();

	/**
	 * Creates a new annotation model. The annotation is empty, i.e. does not
	 * manage any annotations and is not connected to any document.
	 */
	public AnnotationModel() {
		fAnnotations= new AnnotationMap(10);
		fPositions= new IdentityHashMap<>(10);
		fAnnotationModelListeners= new ArrayList<>(2);

		fDocumentListener= new IDocumentListener() {

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
			}

			@Override
			public void documentChanged(DocumentEvent event) {
				fDocumentChanged= true;
			}
		};
	}

	/**
	 * Returns the annotation map internally used by this annotation model.
	 *
	 * @return the annotation map internally used by this annotation model
	 * @since 3.0
	 */
	protected IAnnotationMap getAnnotationMap() {
		return (IAnnotationMap) fAnnotations;
	}

    @Override
	public Object getLockObject() {
        return getAnnotationMap().getLockObject();
    }

    @Override
	public void setLockObject(Object lockObject) {
        getAnnotationMap().setLockObject(lockObject);
    }

    /**
     * Returns the current annotation model event. This is the event that will be sent out
     * when calling <code>fireModelChanged</code>.
     *
     * @return the current annotation model event
     * @since 3.0
     */
    protected final AnnotationModelEvent getAnnotationModelEvent() {
    	synchronized (getLockObject()) {
    		if (fModelEvent == null) {
    			fModelEvent= createAnnotationModelEvent();
    			fModelEvent.markWorldChange(false);
    			fModificationStamp= new Object();
    		}
    		return fModelEvent;
    	}
    }

	@Override
	public void addAnnotation(Annotation annotation, Position position) {
		try {
			addAnnotation(annotation, position, true);
		} catch (BadLocationException e) {
			// ignore invalid position
		}
	}

	@Override
	public void replaceAnnotations(Annotation[] annotationsToRemove, Map<? extends Annotation, ? extends Position> annotationsToAdd) {
		try {
			replaceAnnotations(annotationsToRemove, annotationsToAdd, true);
		} catch (BadLocationException x) {
		}
	}

	/**
	 * Replaces the given annotations in this model and if advised fires a
	 * model change event.
	 *
	 * @param annotationsToRemove the annotations to be removed
	 * @param annotationsToAdd the annotations to be added
	 * @param fireModelChanged <code>true</code> if a model change event
	 *            should be fired, <code>false</code> otherwise
	 * @throws BadLocationException in case an annotation should be added at an
	 *             invalid position
	 * @since 3.0
	 */
	protected void replaceAnnotations(Annotation[] annotationsToRemove, Map<? extends Annotation, ? extends Position> annotationsToAdd, boolean fireModelChanged) throws BadLocationException {

		if (annotationsToRemove != null) {
			for (Annotation element : annotationsToRemove)
				removeAnnotation(element, false);
		}

		if (annotationsToAdd != null) {
			Iterator<? extends Entry<? extends Annotation, ? extends Position>> iter= annotationsToAdd.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<? extends Annotation, ? extends Position> mapEntry= iter.next();
				Annotation annotation= mapEntry.getKey();
				Position position= mapEntry.getValue();
				addAnnotation(annotation, position, false);
			}
		}

		if (fireModelChanged)
			fireModelChanged();
	}

	/**
	 * Adds the given annotation to this model. Associates the
	 * annotation with the given position. If requested, all annotation
	 * model listeners are informed about this model change. If the annotation
	 * is already managed by this model nothing happens.
	 *
	 * @param annotation the annotation to add
	 * @param position the associate position
	 * @param fireModelChanged indicates whether to notify all model listeners
	 * @throws BadLocationException if the position is not a valid document position
	 */
	protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {
		if (!fAnnotations.containsKey(annotation)) {

			addPosition(fDocument, position);
			fAnnotations.put(annotation, position);
			fPositions.put(position, annotation);
			synchronized (getLockObject()) {
				getAnnotationModelEvent().annotationAdded(annotation);
			}

			if (fireModelChanged)
				fireModelChanged();
		}
	}

	@Override
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		if (!fAnnotationModelListeners.contains(listener)) {
			fAnnotationModelListeners.add(listener);
			if (listener instanceof IAnnotationModelListenerExtension) {
				IAnnotationModelListenerExtension extension= (IAnnotationModelListenerExtension) listener;
				AnnotationModelEvent event= createAnnotationModelEvent();
				event.markSealed();
			    extension.modelChanged(event);
			} else
			    listener.modelChanged(this);
		}
	}

	/**
	 * Adds the given position to the default position category of the
	 * given document.
	 *
	 * @param document the document to which to add the position
	 * @param position the position to add
	 * @throws BadLocationException if the position is not a valid document position
	 */
	protected void addPosition(IDocument document, Position position) throws BadLocationException {
		if (document != null)
			document.addPosition(position);
	}

	/**
	 * Removes the given position from the default position category of the
	 * given document.
	 *
	 * @param document the document to which to add the position
	 * @param position the position to add
	 *
	 * @since 3.0
	 */
	protected void removePosition(IDocument document, Position position) {
		if (document != null)
			document.removePosition(position);
	}

	@Override
	public void connect(IDocument document) {
		Assert.isTrue(fDocument == null || fDocument == document);

		if (fDocument == null) {
			fDocument= document;
			Iterator<Position> e= getAnnotationMap().valuesIterator();
			while (e.hasNext())
				try {
					addPosition(document, e.next());
				} catch (BadLocationException x) {
					// ignore invalid position
				}
		}

		++ fOpenConnections;
		if (fOpenConnections == 1) {
			document.addDocumentListener(fDocumentListener);
			connected();
		}

		for (Object object : fAttachments.keySet()) {
			IAnnotationModel model= fAttachments.get(object);
			model.connect(document);
		}
	}

	/**
	 * Hook method. Is called as soon as this model becomes connected to a document.
	 * Subclasses may re-implement.
	 */
	protected void connected() {
	}

	/**
	 * Hook method. Is called as soon as this model becomes disconnected from its document.
	 * Subclasses may re-implement.
	 */
	protected void disconnected() {
	}

	@Override
	public void disconnect(IDocument document) {

		Assert.isTrue(fDocument == document);

		for (Object object : fAttachments.keySet()) {
			IAnnotationModel model= fAttachments.get(object);
			model.disconnect(document);
		}

		-- fOpenConnections;
		if (fOpenConnections == 0) {

			disconnected();
			document.removeDocumentListener(fDocumentListener);

			Iterator<Position> e= getAnnotationMap().valuesIterator();
			while (e.hasNext()) {
				Position p= e.next();
				removePosition(document, p);
			}
			fDocument= null;
		}
	}

	/**
	 * Informs all annotation model listeners that this model has been changed.
	 */
	protected void fireModelChanged() {
		AnnotationModelEvent modelEvent= null;

		synchronized(getLockObject()) {
			if (fModelEvent != null) {
				modelEvent= fModelEvent;
				fModelEvent= null;
			}
		}

		if (modelEvent != null)
			fireModelChanged(modelEvent);
	}

	/**
	 * Creates and returns a new annotation model event. Subclasses may override.
	 *
	 * @return a new and empty annotation model event
	 * @since 3.0
	 */
	protected AnnotationModelEvent createAnnotationModelEvent() {
		return new AnnotationModelEvent(this);
	}

	/**
	 * Informs all annotation model listeners that this model has been changed
	 * as described in the annotation model event. The event is sent out
	 * to all listeners implementing <code>IAnnotationModelListenerExtension</code>.
	 * All other listeners are notified by just calling <code>modelChanged(IAnnotationModel)</code>.
	 *
	 * @param event the event to be sent out to the listeners
	 * @since 2.0
	 */
	protected void fireModelChanged(AnnotationModelEvent event) {

		event.markSealed();

		if (event.isEmpty())
			return;

		ArrayList<IAnnotationModelListener> v= new ArrayList<>(fAnnotationModelListeners);
		Iterator<IAnnotationModelListener> e= v.iterator();
		while (e.hasNext()) {
			IAnnotationModelListener l= e.next();
			if (l instanceof IAnnotationModelListenerExtension)
				((IAnnotationModelListenerExtension) l).modelChanged(event);
			else if (l != null)
				l.modelChanged(this);
		}
	}

	/**
	 * Removes the given annotations from this model. If requested all
	 * annotation model listeners will be informed about this change.
	 * <code>modelInitiated</code> indicates whether the deletion has
	 * been initiated by this model or by one of its clients.
	 *
	 * @param annotations the annotations to be removed
	 * @param fireModelChanged indicates whether to notify all model listeners
	 * @param modelInitiated indicates whether this changes has been initiated by this model
	 */
	protected void removeAnnotations(List<? extends Annotation> annotations, boolean fireModelChanged, boolean modelInitiated) {
		if (annotations.size() > 0) {
			Iterator<? extends Annotation> e= annotations.iterator();
			while (e.hasNext())
				removeAnnotation(e.next(), false);

			if (fireModelChanged)
				fireModelChanged();
		}
	}

	/**
	 * Removes all annotations from the model whose associated positions have been
	 * deleted. If requested inform all model listeners about the change.
	 *
	 * @param fireModelChanged indicates whether to notify all model listeners
	 */
	protected void cleanup(boolean fireModelChanged) {
		cleanup(fireModelChanged, true);
	}

	/**
	 * Removes all annotations from the model whose associated positions have been
	 * deleted. If requested inform all model listeners about the change. If requested
	 * a new thread is created for the notification of the model listeners.
	 *
	 * @param fireModelChanged indicates whether to notify all model listeners
	 * @param forkNotification <code>true</code> iff notification should be done in a new thread
	 * @since 3.0
	 */
	private void cleanup(boolean fireModelChanged, boolean forkNotification) {
		if (fDocumentChanged) {
			fDocumentChanged= false;

			ArrayList<Annotation> deleted= new ArrayList<>();
			Iterator<Annotation> e= getAnnotationMap().keySetIterator();
			while (e.hasNext()) {
				Annotation a= e.next();
				Position p= fAnnotations.get(a);
				if (p == null || p.isDeleted())
					deleted.add(a);
			}

			if (fireModelChanged && forkNotification) {
				removeAnnotations(deleted, false, false);
				synchronized (getLockObject()) {
					if (fModelEvent != null)
						new Thread() {
							@Override
							public void run() {
								fireModelChanged();
							}
						}.start();
				}
			} else
				removeAnnotations(deleted, fireModelChanged, false);
		}
	}

	@Override
	public Iterator<Annotation> getAnnotationIterator() {
		return getAnnotationIterator(true, true);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	@Override
	public Iterator<Annotation> getAnnotationIterator(int offset, int length, boolean canStartBefore, boolean canEndAfter) {
		Iterator<Annotation> regionIterator= getRegionAnnotationIterator(offset, length, canStartBefore, canEndAfter);

		if (fAttachments.isEmpty())
			return regionIterator;

		List<Iterator<Annotation>> iterators= new ArrayList<>(fAttachments.size() + 1);
		iterators.add(regionIterator);
		Iterator<Object> it= fAttachments.keySet().iterator();
		while (it.hasNext()) {
			IAnnotationModel attachment= fAttachments.get(it.next());
			if (attachment instanceof IAnnotationModelExtension2)
				iterators.add(((IAnnotationModelExtension2) attachment).getAnnotationIterator(offset, length, canStartBefore, canEndAfter));
			else
				iterators.add(new RegionIterator(attachment.getAnnotationIterator(), attachment, offset, length, canStartBefore, canEndAfter));
		}

		return new MetaIterator<>(iterators.iterator());
	}

	/**
	 * Returns an iterator as specified in {@link IAnnotationModelExtension2#getAnnotationIterator(int, int, boolean, boolean)}
	 *
	 * @param offset region start
	 * @param length region length
	 * @param canStartBefore position can start before region
	 * @param canEndAfter position can end after region
	 * @return an iterator to iterate over annotations in region
	 * @see IAnnotationModelExtension2#getAnnotationIterator(int, int, boolean, boolean)
	 * @since 3.4
	 */
	private Iterator<Annotation> getRegionAnnotationIterator(int offset, int length, boolean canStartBefore, boolean canEndAfter) {
		if (!(fDocument instanceof AbstractDocument))
			return new RegionIterator(getAnnotationIterator(true), this, offset, length, canStartBefore, canEndAfter);

		AbstractDocument document= (AbstractDocument) fDocument;
		cleanup(true);

		try {
			Position[] positions= document.getPositions(IDocument.DEFAULT_CATEGORY, offset, length, canStartBefore, canEndAfter);
			return new AnnotationsInterator(positions, fPositions);
		} catch (BadPositionCategoryException e) {
			// can happen if e.g. the document doesn't contain such a category, or when removed in a different thread
			return Collections.<Annotation>emptyList().iterator();
		}
	}

	/**
	 * Returns all annotations managed by this model. <code>cleanup</code>
	 * indicates whether all annotations whose associated positions are
	 * deleted should previously be removed from the model. <code>recurse</code> indicates
	 * whether annotations of attached sub-models should also be returned.
	 *
	 * @param cleanup indicates whether annotations with deleted associated positions are removed
	 * @param recurse whether to return annotations managed by sub-models.
	 * @return all annotations managed by this model
	 * @since 3.0
	 */
	private Iterator<Annotation> getAnnotationIterator(boolean cleanup, boolean recurse) {
		Iterator<Annotation> iter= getAnnotationIterator(cleanup);
		if (!recurse || fAttachments.isEmpty())
			return iter;

		List<Iterator<Annotation>> iterators= new ArrayList<>(fAttachments.size() + 1);
		iterators.add(iter);
		Iterator<Object> it= fAttachments.keySet().iterator();
		while (it.hasNext())
			iterators.add(fAttachments.get(it.next()).getAnnotationIterator());

		return new MetaIterator<>(iterators.iterator());
	}

	/**
	 * Returns all annotations managed by this model. <code>cleanup</code>
	 * indicates whether all annotations whose associated positions are
	 * deleted should previously be removed from the model.
	 *
	 * @param cleanup indicates whether annotations with deleted associated positions are removed
	 * @return all annotations managed by this model
	 */
	protected Iterator<Annotation> getAnnotationIterator(boolean cleanup) {
		if (cleanup)
			cleanup(true);

		return getAnnotationMap().keySetIterator();
	}

	@Override
	public Position getPosition(Annotation annotation) {
		Position position= fAnnotations.get(annotation);
		if (position != null)
			return position;

		Iterator<IAnnotationModel> it= fAttachments.values().iterator();
		while (position == null && it.hasNext())
			position= it.next().getPosition(annotation);
		return position;
	}

	@Override
	public void removeAllAnnotations() {
		removeAllAnnotations(true);
	}

	/**
	 * Removes all annotations from the annotation model. If requested
	 * inform all model change listeners about this change.
	 *
	 * @param fireModelChanged indicates whether to notify all model listeners
	 */
	protected void removeAllAnnotations(boolean fireModelChanged) {

		if (fDocument != null) {
			Iterator<Annotation> e= getAnnotationMap().keySetIterator();
			while (e.hasNext()) {
				Annotation a= e.next();
				Position p= fAnnotations.get(a);
				removePosition(fDocument, p);
//				p.delete();
				synchronized (getLockObject()) {
					getAnnotationModelEvent().annotationRemoved(a, p);
				}
			}
		}

		fAnnotations.clear();
		fPositions.clear();

		if (fireModelChanged)
			fireModelChanged();
	}

	@Override
	public void removeAnnotation(Annotation annotation) {
		removeAnnotation(annotation, true);
	}

	/**
	 * Removes the given annotation from the annotation model.
	 * If requested inform all model change listeners about this change.
	 *
	 * @param annotation the annotation to be removed
	 * @param fireModelChanged indicates whether to notify all model listeners
	 */
	protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
		if (fAnnotations.containsKey(annotation)) {

			Position p= null;
			p= fAnnotations.get(annotation);
			if (fDocument != null) {
				removePosition(fDocument, p);
//				p.delete();
			}

			fAnnotations.remove(annotation);
			fPositions.remove(p);
			synchronized (getLockObject()) {
				getAnnotationModelEvent().annotationRemoved(annotation, p);
			}

			if (fireModelChanged)
				fireModelChanged();
		}
	}

	@Override
	public void modifyAnnotationPosition(Annotation annotation, Position position) {
		modifyAnnotationPosition(annotation, position, true);
	}

	/**
	 * Modifies the associated position of the given annotation to the given
	 * position. If the annotation is not yet managed by this annotation model,
	 * the annotation is added. When the position is <code>null</code>, the
	 * annotation is removed from the model.
	 * <p>
	 * If requested, all annotation model change listeners will be informed
	 * about the change.
	 *
	 * @param annotation the annotation whose associated position should be
	 *            modified
	 * @param position the position to whose values the associated position
	 *            should be changed
	 * @param fireModelChanged indicates whether to notify all model listeners
	 * @since 3.0
	 */
	protected void modifyAnnotationPosition(Annotation annotation, Position position, boolean fireModelChanged) {
		if (position == null) {
			removeAnnotation(annotation, fireModelChanged);
		} else {
			Position p= fAnnotations.get(annotation);
			if (p != null) {

				if (position.getOffset() != p.getOffset() || position.getLength() != p.getLength()) {
					fDocument.removePosition(p);
					p.setOffset(position.getOffset());
					p.setLength(position.getLength());
					try {
						fDocument.addPosition(p);
					} catch (BadLocationException e) {
						// ignore invalid position
					}
				}
				synchronized (getLockObject()) {
					getAnnotationModelEvent().annotationChanged(annotation);
				}
				if (fireModelChanged)
					fireModelChanged();

			} else {
				try {
					addAnnotation(annotation, position, fireModelChanged);
				} catch (BadLocationException x) {
					// ignore invalid position
				}
			}
		}
	}

	/**
	 * Modifies the given annotation if the annotation is managed by this
	 * annotation model.
	 * <p>
	 * If requested, all annotation model change listeners will be informed
	 * about the change.
	 *
	 * @param annotation the annotation to be modified
	 * @param fireModelChanged indicates whether to notify all model listeners
	 * @since 3.0
	 */
	protected void modifyAnnotation(Annotation annotation, boolean fireModelChanged) {
		if (fAnnotations.containsKey(annotation)) {
			synchronized (getLockObject()) {
				getAnnotationModelEvent().annotationChanged(annotation);
			}
			if (fireModelChanged)
				fireModelChanged();
		}
	}

	@Override
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		fAnnotationModelListeners.remove(listener);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#attach(java.lang.Object, java.lang.Object)
	 * @since 3.0
	 */
	@Override
	public void addAnnotationModel(Object key, IAnnotationModel attachment) {
		Assert.isNotNull(attachment);
		if (!fAttachments.containsValue(attachment)) {
			fAttachments.put(key, attachment);
			for (int i= 0; i < fOpenConnections; i++)
				attachment.connect(fDocument);
			attachment.addAnnotationModelListener(fModelListener);
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#get(java.lang.Object)
	 * @since 3.0
	 */
	@Override
	public IAnnotationModel getAnnotationModel(Object key) {
		return fAttachments.get(key);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#detach(java.lang.Object)
	 * @since 3.0
	 */
	@Override
	public IAnnotationModel removeAnnotationModel(Object key) {
		IAnnotationModel ret= fAttachments.remove(key);
		if (ret != null) {
			for (int i= 0; i < fOpenConnections; i++)
				ret.disconnect(fDocument);
			ret.removeAnnotationModelListener(fModelListener);
		}
		return ret;
	}

	@Override
	public Object getModificationStamp() {
		return fModificationStamp;
	}
}
