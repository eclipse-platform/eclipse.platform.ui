/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.source;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.ProjectionDocumentManager;

/**
 * ProjectionAnnotationModel.java
 */
public class ProjectionAnnotationModel implements IAnnotationModel {
	
	private class ProjectionIterator implements Iterator {
		
		private Position[] fPositions; 
		private int fIndex;
		
		public ProjectionIterator(Position[] positions) {
			fPositions= positions;
			fIndex= 0;
		}
		
		/*
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return fIndex < fPositions.length;
		}

		/*
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			Position p= fPositions[fIndex++];
			return new ProjectionAnnotation(p);
		}

		/*
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};
	
	private static class EmptyIterator implements Iterator {
		/*
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return false;
		}

		/*
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			throw new NoSuchElementException();
		}

		/*
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};
	
	
	private ITextViewer fTextViewer;
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#IAnnotationModel()
	 */
	public ProjectionAnnotationModel() {
	}
	
	public void setTextViewer(ITextViewer viewer) {
		fTextViewer= viewer;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
	 */
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
//		throw new UnsupportedOperationException();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
	 */
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
//		throw new UnsupportedOperationException();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#disconnect(org.eclipse.jface.text.IDocument)
	 */
	public void disconnect(IDocument document) {
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotation(org.eclipse.jface.text.source.Annotation, org.eclipse.jface.text.Position)
	 */
	public void addAnnotation(Annotation annotation, Position position) {
//		throw new UnsupportedOperationException();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotation(org.eclipse.jface.text.source.Annotation)
	 */
	public void removeAnnotation(Annotation annotation) {
//		throw new UnsupportedOperationException();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#getAnnotationIterator()
	 */
	public Iterator getAnnotationIterator() {
		IDocument document= fTextViewer.getDocument();
		if (document != null) {
			try {
				return new ProjectionIterator(document.getPositions(ProjectionDocumentManager.PROJECTION_DOCUMENTS));
			} catch (BadPositionCategoryException x) {
			}
		}
		return new EmptyIterator();
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationModel#getPosition(org.eclipse.jface.text.source.Annotation)
	 */
	public Position getPosition(Annotation annotation) {
//		if (annotation instanceof ProjectionAnnotation) {
//			Position p= ((ProjectionAnnotation) annotation).getProjectionRange();
//			if (p.getOffset() + p.getLength() < fTextViewer.getDocument().getLength())
//				return new Position(p.getOffset() + Math.max(p.getLength() -1, 0));
//		}
		return null;
	}
}
