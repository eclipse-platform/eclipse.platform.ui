/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug 24108
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.outline.IProblem;
import org.eclipse.ant.internal.ui.editor.outline.LocationProvider;
import org.eclipse.ant.internal.ui.editor.outline.IProblemRequestor;
import org.eclipse.ant.internal.ui.editor.outline.XMLCore;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

/*
 * This file originates from an internal package of Eclipse's 
 * Manifest Editor. It has been copied by GEBIT to here in order to
 * permanently use those features. It has been renamed and edited by GEBIT 
 * after copying.
 */
public class AntEditorDocumentProvider extends FileDocumentProvider {

	protected class XMLAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemRequestor {
		
		private List fGeneratedAnnotations= new ArrayList();
		private List fCollectedProblems= new ArrayList();
		
		private ReverseMap fReverseMap= new ReverseMap();
		private List fPreviouslyOverlaid= null; 
		private List fCurrentlyOverlaid= new ArrayList();

		/**
		 * Constructor for XMLAnnotationModel.
		 * @param resource
		 */
		public XMLAnnotationModel(IFileEditorInput input) {
			super(input.getFile());
		}

		/*
		 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#createMarkerAnnotation(org.eclipse.core.resources.IMarker)
		 */
		protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
			return new XMLMarkerAnnotation(marker);
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
				
			synchronized (fAnnotations) {
					
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
						
		/*
		 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
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
			
		/*
		 * @see AnnotationModel#removeAllAnnotations(boolean)
		 */
		protected void removeAllAnnotations(boolean fireModelChanged) {
			super.removeAllAnnotations(fireModelChanged);
			fReverseMap.clear();
		}
			
		/*
		 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
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
	 * Remembers a XML document model for each element.
	 */
	protected class XMLFileInfo extends FileInfo {
		
		public AntModel fAntModel;
		
		public XMLFileInfo(IDocument document, IAnnotationModel annotationModel, FileSynchronizer fileSynchronizer, AntModel model) {
			super(document, annotationModel, fileSynchronizer);
			fAntModel= model;
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
		super();
		fCore= core;
	}

    private IDocumentPartitioner createDocumentPartitioner() {
        DefaultPartitioner partitioner =
            new DefaultPartitioner(
                new AntEditorPartitionScanner(),
                new String[] {
                    AntEditorPartitionScanner.XML_TAG,
                    AntEditorPartitionScanner.XML_COMMENT });
        return partitioner;
    }

    public IDocument createDocument(Object element) throws CoreException {
	    IDocument document;
	    if (element instanceof IEditorInput) {
		    document= new PartiallySynchronizedDocument();
		    if (setDocumentContent(document, (IEditorInput) element, getEncoding(element))) {
			    initializeDocument(document);
		    }
	    } else {
		    document= null;
	    }
	    return document;
    }
	
    protected void initializeDocument(IDocument document) {
	    IDocumentPartitioner partitioner= createDocumentPartitioner();
	    document.setDocumentPartitioner(partitioner);
	    partitioner.connect(document);
    }

    public AntModel getAntModel(Object element) {
	    ElementInfo info= getElementInfo(element);
	    if (info instanceof XMLFileInfo) {
		    XMLFileInfo xmlInfo= (XMLFileInfo) info;
		    return xmlInfo.fAntModel;
	    }
	    return null;
    }

    /*
     * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
     */
    protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
	    if (element instanceof IFileEditorInput) {
		    IFileEditorInput input= (IFileEditorInput) element;
		    return new XMLAnnotationModel(input);
	    }
	    return super.createAnnotationModel(element);
    }

    protected AntModel createAntModel(Object element, IDocument document, IAnnotationModel annotationModel) {
	    IProblemRequestor requestor= annotationModel instanceof IProblemRequestor ? (IProblemRequestor) annotationModel : null;
	    return new AntModel(fCore, document, requestor, new LocationProvider(element instanceof IFileEditorInput ? ((IFileEditorInput) element).getFile() : null));
    }

    /*
     * @see org.eclipse.ui.editors.text.FileDocumentProvider#createElementInfo(java.lang.Object)
     */
    protected ElementInfo createElementInfo(Object element) throws CoreException {
	    if (element instanceof IFileEditorInput) {
			
		    IFileEditorInput input= (IFileEditorInput) element;
			
		    try {
			    refreshFile(input.getFile());
		    } catch (CoreException x) {
			    handleCoreException(x, "XMLDocumentProvider.createElementInfo: Core exception"); //$NON-NLS-1$
		    }
			
		    IDocument d= null;
		    IStatus s= null;
			
		    try {
			    d= createDocument(element);
		    } catch (CoreException x) {
			    s= x.getStatus();
			    d= createEmptyDocument();
		    }
			
		    IAnnotationModel m= createAnnotationModel(element);
		    AntModel o= createAntModel(element, d, m);
		    o.install();
		    FileSynchronizer f= new FileSynchronizer(input);
		    f.install();
			
		    XMLFileInfo info= new XMLFileInfo(d, m, f, o);
		    info.fModificationStamp= computeModificationStamp(input.getFile());
		    info.fStatus= s;
		    info.fEncoding= getPersistedEncoding(input);
			
		    return info;
	    }
		
	    return super.createElementInfo(element);
    }
	
    /*
     * @see org.eclipse.ui.editors.text.FileDocumentProvider#disposeElementInfo(java.lang.Object, org.eclipse.ui.texteditor.AbstractDocumentProvider.ElementInfo)
     */
    protected void disposeElementInfo(Object element, ElementInfo info) {
	    if (info instanceof XMLFileInfo) {
		    XMLFileInfo xmlInfo= (XMLFileInfo) info;
		    if (xmlInfo.fAntModel != null)
			    xmlInfo.fAntModel.dispose();
	    }
	    super.disposeElementInfo(element, info);	
    }
}
