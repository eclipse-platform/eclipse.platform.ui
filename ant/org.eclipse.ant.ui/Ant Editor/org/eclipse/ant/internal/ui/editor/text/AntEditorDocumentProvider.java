/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.ui.editor.outline.AntEditorMarkerUpdater;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.outline.IProblem;
import org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor;
import org.eclipse.ant.internal.ui.editor.outline.LocationProvider;
import org.eclipse.ant.internal.ui.editor.outline.XMLCore;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class AntEditorDocumentProvider extends TextFileDocumentProvider {

	protected class AntAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemRequestor {
		
		private List fGeneratedAnnotations= new ArrayList();
		private List fCollectedProblems= new ArrayList();
		
		private ReverseMap fReverseMap= new ReverseMap();
		private List fPreviouslyOverlaid= null; 
		private List fCurrentlyOverlaid= new ArrayList();

		public AntAnnotationModel(IFile file) {
			super(file);
		}

		/*
		 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createMarkerAnnotation(org.eclipse.core.resources.IMarker)
		 */
		protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
			String markerType= MarkerUtilities.getMarkerType(marker);
			if (AntEditorMarkerUpdater.BUILDFILE_PROBLEM_MARKER.equals(markerType)) {
				return null;
			}
			return new XMLMarkerAnnotation(EditorsUI.getAnnotationTypeLookup().getAnnotationType(marker), marker);
		}

		protected Position createPositionFromProblem(IProblem problem) {
			int start= problem.getOffset();
			if (start >= 0) {
				int length= problem.getLength();
					
				if (length >= 0)
					return new Position(start, length);
			}

			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor#acceptProblem(org.eclipse.ant.internal.ui.editor.outline.IProblem)
		 */
		public void acceptProblem(IProblem problem) {
			fCollectedProblems.add(problem);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor#acceptProblem(org.eclipse.ant.internal.ui.editor.outline.IProblem)
		 */
		public void beginReporting() {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor#acceptProblem(org.eclipse.ant.internal.ui.editor.outline.IProblem)
		 */
		public void endReporting() {
			boolean temporaryProblemsChanged= false;
			fPreviouslyOverlaid= fCurrentlyOverlaid;
			fCurrentlyOverlaid= new ArrayList();
				
			synchronized (getAnnotationMap()) {
					
				if (fGeneratedAnnotations.size() > 0) {
					temporaryProblemsChanged= true;	
					removeAnnotations(fGeneratedAnnotations, false, true);
					fGeneratedAnnotations.clear();
				}
					
				if (fCollectedProblems != null && fCollectedProblems.size() > 0) {
					Iterator e= fCollectedProblems.iterator();
					while (e.hasNext()) {
							
						IProblem problem= (IProblem) e.next();
							
						Position position= createPositionFromProblem(problem);
						if (position != null) {
								
							XMLProblemAnnotation annotation= new XMLProblemAnnotation(problem);
							overlayMarkers(position, annotation);								
							fGeneratedAnnotations.add(annotation);
							try {
								addAnnotation(annotation, position, false);
							} catch (BadLocationException ex) {
								AntUIPlugin.log(ex);
							}
								
							temporaryProblemsChanged= true;
						}
					}
						
					fCollectedProblems.clear();
				}
					
				removeMarkerOverlays();
				fPreviouslyOverlaid.clear();
				fPreviouslyOverlaid= null;
			}
					
			if (temporaryProblemsChanged)
				fireModelChanged(new AnnotationModelEvent(this));
		}

		private void removeMarkerOverlays() {
			Iterator e= fPreviouslyOverlaid.iterator();
			while (e.hasNext()) {
				XMLMarkerAnnotation annotation= (XMLMarkerAnnotation) e.next();
				annotation.setOverlay(null);
			}			
		}
			
		/**
		 * Overlays value with problem annotation.
		 * @param problemAnnotation
		 */
		private void setOverlay(Object value, XMLProblemAnnotation problemAnnotation) {
			if (value instanceof  XMLMarkerAnnotation) {
				XMLMarkerAnnotation annotation= (XMLMarkerAnnotation) value;
				if (annotation.isProblem()) {
					annotation.setOverlay(problemAnnotation);
					fPreviouslyOverlaid.remove(annotation);
					fCurrentlyOverlaid.add(annotation);
				}
			}
		}
			
		private void  overlayMarkers(Position position, XMLProblemAnnotation problemAnnotation) {
			Object value= getAnnotations(position);
			if (value instanceof List) {
				List list= (List) value;
				for (Iterator e = list.iterator(); e.hasNext();)
					setOverlay(e.next(), problemAnnotation);
			} else {
				setOverlay(value, problemAnnotation);
			}
		}

		private Object getAnnotations(Position position) {
			return fReverseMap.get(position);
		}
						
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.source.AnnotationModel#addAnnotation(org.eclipse.jface.text.source.Annotation, org.eclipse.jface.text.Position, boolean)
		 */
		protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {
			super.addAnnotation(annotation, position, fireModelChanged);
				
			Object cached= fReverseMap.get(position);
			if (cached == null)
				fReverseMap.put(position, annotation);
			else if (cached instanceof List) {
				List list= (List) cached;
				list.add(annotation);
			} else if (cached instanceof Annotation) {
				List list= new ArrayList(2);
				list.add(cached);
				list.add(annotation);
				fReverseMap.put(position, list);
			}
		}
			
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.source.AnnotationModel#removeAllAnnotations(boolean)
		 */
		protected void removeAllAnnotations(boolean fireModelChanged) {
			super.removeAllAnnotations(fireModelChanged);
			fReverseMap.clear();
		}
			
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.source.AnnotationModel#removeAnnotation(org.eclipse.jface.text.source.Annotation, boolean)
		 */
		protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
			Position position= getPosition(annotation);
			Object cached= fReverseMap.get(position);
			if (cached instanceof List) {
				List list= (List) cached;
				list.remove(annotation);
				if (list.size() == 1) {
					fReverseMap.put(position, list.get(0));
					list.clear();
				}
			} else if (cached instanceof Annotation) {
				fReverseMap.remove(position);
			}
				
			super.removeAnnotation(annotation, fireModelChanged);
		}
	}
	
	/**
	 * Remembers a Ant document model for each element.
	 */
	protected class AntFileInfo extends FileInfo {
		
		public AntModel fAntModel;
		
		public AntFileInfo() {
		}
	}
	
	/**
	 * Internal structure for mapping positions to some value. 
	 * The reason for this specific structure is that positions can
	 * change over time. Thus a lookup is based on value and not
	 * on hash value.
	 */
	protected static class ReverseMap {
			
		static class Entry {
			Position fPosition;
			Object fValue;
		}
			
		private List fList= new ArrayList(2);
		private int fAnchor= 0;
			
		public ReverseMap() {
		}
			
		public Object get(Position position) {
				
			Entry entry;
				
			// behind anchor
			int length= fList.size();
			for (int i= fAnchor; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
				
			// before anchor
			for (int i= 0; i < fAnchor; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
				
			return null;
		}
			
		private int getIndex(Position position) {
			Entry entry;
			int length= fList.size();
			for (int i= 0; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position))
					return i;
			}
			return -1;
		}
			
		public void put(Position position,  Object value) {
			int index= getIndex(position);
			if (index == -1) {
				Entry entry= new Entry();
				entry.fPosition= position;
				entry.fValue= value;
				fList.add(entry);
			} else {
				Entry entry= (Entry) fList.get(index);
				entry.fValue= value;
			}
		}
			
		public void remove(Position position) {
			int index= getIndex(position);
			if (index > -1)
				fList.remove(index);
		}
			
		public void clear() {
			fList.clear();
		}
	}
	
	
	private XMLCore fCore;

	public AntEditorDocumentProvider(XMLCore core) {
		IDocumentProvider provider= new TextFileDocumentProvider(new AntStorageDocumentProvider());
		provider= new ForwardingDocumentProvider(AntDocumentSetupParticipant.ANT_PARTITIONING, new AntDocumentSetupParticipant(), provider);
		setParentDocumentProvider(provider);
		
		fCore= core;
	}

    public AntModel getAntModel(Object element) {
	    FileInfo info= getFileInfo(element);
	    if (info instanceof AntFileInfo) {
		    AntFileInfo xmlInfo= (AntFileInfo) info;
		    return xmlInfo.fAntModel;
	    }
	    return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
     */
    protected IAnnotationModel createAnnotationModel(IFile file) {
		   return new AntAnnotationModel(file);
    }

    protected AntModel createAntModel(Object element, IDocument document, IAnnotationModel annotationModel) {
	    IProblemRequestor requestor= annotationModel instanceof IProblemRequestor ? (IProblemRequestor) annotationModel : null;
	    return new AntModel(fCore, document, requestor, new LocationProvider(element instanceof IEditorInput ? (IEditorInput) element : null));
    }
    
    /*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info= super.createFileInfo(element);
		if (!(info instanceof AntFileInfo)) {
			return null;
		}
	
		AntFileInfo xmlInfo= (AntFileInfo) info;
		IAnnotationModel model= xmlInfo.fModel;
//		if (model == null) {
//			model= createAnnotationModel(null);
//			model.connect(xmlInfo.fTextFileBuffer.getDocument());
//		}
		AntModel antModel= createAntModel(element, xmlInfo.fTextFileBuffer.getDocument(), model);
		antModel.install();
		xmlInfo.fAntModel= antModel;
		setUpSynchronization(xmlInfo);
		
		return xmlInfo;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#disposeFileInfo(java.lang.Object, org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)
     */
    protected void disposeFileInfo(Object element, FileInfo info) {
	    if (info instanceof AntFileInfo) {
		    AntFileInfo xmlInfo= (AntFileInfo) info;
		    if (xmlInfo.fAntModel != null)
			    xmlInfo.fAntModel.dispose();
	    }
	    super.disposeFileInfo(element, info);	
    }
    
    /*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createEmptyFileInfo()
	 */
	protected FileInfo createEmptyFileInfo() {
		return new AntFileInfo();
	}
	
	 private void setUpSynchronization(AntFileInfo antInfo) {
        IDocument document= antInfo.fTextFileBuffer.getDocument();
        IAnnotationModel model= antInfo.fModel;
        
        if (document instanceof ISynchronizable && model instanceof ISynchronizable) {
            Object lock= ((ISynchronizable) document).getLockObject();
            ((ISynchronizable) model).setLockObject(lock);
        }
    }
}
