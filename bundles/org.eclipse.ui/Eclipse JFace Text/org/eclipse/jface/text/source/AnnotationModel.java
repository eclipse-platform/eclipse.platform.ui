package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.Assert;


/**
 * Standard implementation of <code>IAnnotationModel</code>. This class can directly
 * be used by clients. Subclasses may adapt this annotation model to other
 * exsisting annotation mechanisms.
 */
public class AnnotationModel implements IAnnotationModel {

	/** The list of managed annotations */
	protected Map fAnnotations;
	/** The list of annotation model listeners */
	protected ArrayList fAnnotationModelListeners;
	/** The document conntected with this model */
	protected IDocument fDocument;
	/** The number of open connections to the same document */
	private int fOpenConnections= 0;

	/**
	 * Creates a new annotation model. The annotation is empty, i.e. does not
	 * manage any annotations and is not connected to any document.
	 */
	public AnnotationModel() {
		fAnnotations= Collections.synchronizedMap(new HashMap(10));
		fAnnotationModelListeners= new ArrayList(2);
	}

	/*
	 * @see IAnnotationModel#addAnnotation
	 */
	public void addAnnotation(Annotation annotation, Position position) {
		addAnnotation(annotation, position, true);
	}

	/**
	 * Adds the given annotation to this model. Associates the 
	 * annotation with the given position. If requested, all annotation
	 * model listeners are informed about this model change. If the annotation
	 * is already managed by this model nothing happens.
	 *
	 * @param annotation the annotation to add
	 * @param position the associate position
	 * @param fireModelChange indicates whether to notify all model listeners
	 */
	protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) {
		if (!fAnnotations.containsKey(annotation)) {
			
			fAnnotations.put(annotation, position);
			addPosition(fDocument, position);

			if (fireModelChanged)
				fireModelChanged();
		}
	}

	/*
	 * @see IAnnotationModel#addAnnotationModelListener
	 */
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		if (!fAnnotationModelListeners.contains(listener)) {
			fAnnotationModelListeners.add(listener);
			listener.modelChanged(this);
		}
	}

	/**
	 * Adds the given position to the default position category of the
	 * given document.
	 *
	 * @param document the document to which to add the position
	 * @param position the position to add
	 */
	protected void addPosition(IDocument document, Position position) {
		if (document != null) {
			try {
				document.addPosition(position);
			} catch (BadLocationException x) {
			}
		}
	}
	
	/**
	 * @see IAnnotationModel#connect
	 */
	public void connect(IDocument document) {
		Assert.isTrue(fDocument == null || fDocument == document);
		
		if (fDocument == null) {
			fDocument= document;
			Iterator e= fAnnotations.values().iterator();
			while (e.hasNext())
				addPosition(fDocument, (Position) e.next());
		}
		
		++ fOpenConnections;
		if (fOpenConnections == 1)
			connected();
	}
	
	/**
	 * Hook method. Is called as soon as this model becomes connected to a document.
	 */
	protected void connected() {
	}
	
	/**
	 * Hook method. Is called as soon as this model becomes diconnected from its document.
	 */
	protected void disconnected() {
	}
	
	/*
	 * @see IAnnotationModel#disconnect
	 */
	public void disconnect(IDocument document) {
		
		Assert.isTrue(fDocument == document);
		
		-- fOpenConnections;
		if (fOpenConnections == 0) {
			
			disconnected();
		
			if (fDocument != null) {
				Iterator e= fAnnotations.values().iterator();
				while (e.hasNext()) {
					Position p= (Position) e.next();
					fDocument.removePosition(p);
				}
				fDocument= null;
			}
		}
	}
	
	/**
	 * Informs all annotation model listeners that this model has been changed.
	 */
	protected void fireModelChanged() {
		ArrayList v= new ArrayList(fAnnotationModelListeners);
		Iterator e= v.iterator();
		while (e.hasNext()) {
			IAnnotationModelListener l= (IAnnotationModelListener) e.next();
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
	protected void removeAnnotations(List annotations, boolean fireModelChanged, boolean modelInitiated) {
		if (annotations.size() > 0) {
			Iterator e= annotations.iterator();
			while (e.hasNext())
				removeAnnotation((Annotation) e.next(), false);
				
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
		ArrayList deleted= new ArrayList();
		Iterator e= fAnnotations.keySet().iterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			Position p= (Position) fAnnotations.get(a);
			if (p == null || p.isDeleted())
				deleted.add(a);
		}
		
		removeAnnotations(deleted, fireModelChanged, false);
	}
	
	/*
	 * @see IAnnotationModel#getAnnotationsIterator
	 */
	public Iterator getAnnotationIterator() {
		return getAnnotationIterator(true);
	}
	
	/**
	 * Returns all annotations managed by this model. <code>cleanup</code>
	 * indicates whether all annotations whose associated positions are 
	 * deleted should previously be removed from the model.
	 *
	 * @param cleanup indicates whether annotations with deleted associated positions are removed
	 * @return all annotations managed by this model
	 */
	protected Iterator getAnnotationIterator(boolean cleanup) {
		if (cleanup)
			cleanup(false);
			
		synchronized (fAnnotations) {
			return new ArrayList(fAnnotations.keySet()).iterator();
		}
	}
	
	/*
	 * @see IAnnotationModel#getPosition
	 */
	public Position getPosition(Annotation annotation) {
		return (Position) fAnnotations.get(annotation);
	}
	
	/**
	 * Removes all annotations from the annotation model and
	 * informs all model listeners about this change.
	 */
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
			Iterator e= fAnnotations.values().iterator();
			while (e.hasNext()) {
				Position p= (Position) e.next();
				fDocument.removePosition(p);
			}
		}
		
		fAnnotations.clear();
		
		if (fireModelChanged)
			fireModelChanged();
	}
	
	/*
	 * @see IAnnotationModel#removeAnnotation
	 */
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
			
			if (fDocument != null) {
				Position p= (Position) fAnnotations.get(annotation);
				fDocument.removePosition(p);
			}
				
			fAnnotations.remove(annotation);
			
			if (fireModelChanged)
				fireModelChanged();
		}
	}
	
	/*
	 * @see IAnnotationModel#removeAnnotationModelListener
	 */
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		fAnnotationModelListeners.remove(listener);
	}
}
