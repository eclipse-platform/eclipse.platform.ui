/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.text;

import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.editor.derived.HTMLPrinter;
import org.eclipse.ant.internal.ui.editor.derived.HTMLTextPresenter;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.AntPropertyNode;
import org.eclipse.ant.internal.ui.model.IAntModel;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;


public class XMLTextHover implements ITextHover, ITextHoverExtension {

	private AntEditor fEditor;
	
	public XMLTextHover(AntEditor editor) {
		super();
		fEditor = editor;
	}
	
	/*
	 * Formats a message as HTML text.
	 */
	private String formatMessage(String message) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/*
	 * Formats a message as HTML text.
	 */
	private String formatPathMessage(String[] list) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addSmallHeader(buffer, AntEditorTextMessages.getString("XMLTextHover.4")); //$NON-NLS-1$
		HTMLPrinter.startBulletList(buffer);
		for (int i = 0; i < list.length; i++) {
			HTMLPrinter.addBullet(buffer, list[i]);
		}
		HTMLPrinter.endBulletList(buffer);
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		
		if (!(textViewer instanceof ISourceViewer)) {
			return null;
		}
		
		ISourceViewer sourceViewer= (ISourceViewer) textViewer;
		IAnnotationModel model= sourceViewer.getAnnotationModel();
		
		if (model != null) {
			String message= getAnnotationModelHoverMessage(model, hoverRegion);
			if (message != null) {
				return message;
			}
		}
		AntModel antModel= fEditor.getAntModel();
		if (antModel == null) { //the ant model has not been created yet
			return null;
		}
		
		return getAntModelHoverMessage(antModel, hoverRegion, textViewer);
		
	}
	
	private String getAntModelHoverMessage(AntModel antModel, IRegion hoverRegion, ITextViewer textViewer){
		try {
			IDocument document= textViewer.getDocument();
			int offset= hoverRegion.getOffset();
			int length= hoverRegion.getLength();
			String text= document.get(offset, length);
			String value;
			AntElementNode node= antModel.getNode(offset, false);
			if (document.get(offset - 2, 2).equals("${") || node instanceof AntPropertyNode) { //$NON-NLS-1$
				value= antModel.getPropertyValue(text);
				if (value != null) {
					return formatMessage(value);
				}
			}
			value= antModel.getTargetDescription(text);
			if (value != null) {
				return formatMessage(value);
			}
			Object referencedObject= antModel.getReferenceObject(text);
			if (referencedObject != null) {
				if (referencedObject instanceof Path) {
					return formatPathMessage(((Path)referencedObject).list());
				} else if (referencedObject instanceof PatternSet) {
					return formatPatternSetMessage((PatternSet) referencedObject);
				} else if (referencedObject instanceof AbstractFileSet) {
					return formatFileSetMessage((AbstractFileSet)referencedObject);
				}
			}
		} catch (BadLocationException e) {
		} catch (BuildException be) {
			antModel.handleBuildException(be, null);
		}
		
		return null;
	}
	
	private String getAnnotationModelHoverMessage(IAnnotationModel model, IRegion hoverRegion) {
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			if (a instanceof XMLProblemAnnotation) {
				Position p= model.getPosition(a);
				if (p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
					String msg= a.getText();
					if (msg != null && msg.trim().length() > 0) {
						return formatMessage(msg);
					}
				}
			}
		}
		return null;
	}
	
	private String formatFileSetMessage(AbstractFileSet set) {
		FileScanner fileScanner= new FileScanner();
		IAntModel antModel= fEditor.getAntModel();
		Project project= antModel.getProjectNode().getProject();
		set.setupDirectoryScanner(fileScanner, project);
		String[] excludedPatterns= fileScanner.getExcludesPatterns();
		String[] includesPatterns= fileScanner.getIncludePatterns();
		return formatSetMessage(includesPatterns, excludedPatterns);
	}

	private String formatPatternSetMessage(PatternSet set) {
		IAntModel antModel= fEditor.getAntModel();
		Project project= antModel.getProjectNode().getProject();
		String[] includes= set.getIncludePatterns(project);
		String[] excludes= set.getExcludePatterns(project);
		return formatSetMessage(includes, excludes);
	}

	private String formatSetMessage(String[] includes, String[] excludes) {
		StringBuffer buffer= new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		if (includes != null && includes.length > 0) {
			HTMLPrinter.addSmallHeader(buffer, AntEditorTextMessages.getString("XMLTextHover.5")); //$NON-NLS-1$
			for (int i = 0; i < includes.length; i++) {
				HTMLPrinter.addBullet(buffer, includes[i]);
			}
		}
		HTMLPrinter.addParagraph(buffer, ""); //$NON-NLS-1$
		HTMLPrinter.addParagraph(buffer, ""); //$NON-NLS-1$
		if (excludes != null && excludes.length > 0) {
			HTMLPrinter.addSmallHeader(buffer, AntEditorTextMessages.getString("XMLTextHover.6")); //$NON-NLS-1$
			for (int i = 0; i < excludes.length; i++) {
				HTMLPrinter.addBullet(buffer, excludes[i]);
			}
		}
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (textViewer != null) {
			return getRegion(textViewer, offset);
		}
		return null;	
	}

	public static IRegion getRegion(ITextViewer textViewer, int offset) {
		IDocument document= textViewer.getDocument();
		
		int start= -1;
		int end= -1;
		
		try {	
			int pos= offset;
			char c;
			
			while (pos >= 0) {
				c= document.getChar(pos);
				if (c != '.' && c != '-' && c != '/' &&  c != '\\' && !Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			
			start= pos;
			
			pos= offset;
			int length= document.getLength();
			
			while (pos < length) {
				c= document.getChar(pos);
				if (c != '.' && c != '-' && !Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			
			end= pos;
			
		} catch (BadLocationException x) {
		}
		
		if (start > -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE)) {
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
	  				return new DefaultInformationControl(parent, SWT.NONE, 
	  					new HTMLTextPresenter(true),
				   		AntEditorTextMessages.getString("XMLTextHover.7")); //$NON-NLS-1$
			 	}
  			};
		}
		return null;
	}
}