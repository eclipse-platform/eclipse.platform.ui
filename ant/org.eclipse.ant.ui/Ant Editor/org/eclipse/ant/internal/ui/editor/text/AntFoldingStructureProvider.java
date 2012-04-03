/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ant.internal.ui.editor.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.AntProjectNode;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

public class AntFoldingStructureProvider {
	
	private AntEditor fEditor;
	private IDocument fDocument;
	
	/**
	 * A mapping of the foldable position to the <code>AntElementNode<code> that represent that region
	 */
	private Map fPositionToElement= new HashMap();
	
	public AntFoldingStructureProvider(AntEditor editor) {
		fEditor = editor;
	}
	
	private void updateFoldingRegions(ProjectionAnnotationModel model, Set currentRegions) {
		Annotation[] deletions = computeDifferences(model, currentRegions);

		Map additionsMap = new HashMap();
		for (Iterator iter = currentRegions.iterator(); iter.hasNext();) {
			Object position= iter.next();
			AntElementNode node= (AntElementNode)fPositionToElement.get(position);
			additionsMap.put(new ProjectionAnnotation(node.collapseProjection()), position);
		}

		if ((deletions.length != 0 || additionsMap.size() != 0)) {
			model.modifyAnnotations(deletions, additionsMap, new Annotation[] {});
		}
	}

	private Annotation[] computeDifferences(ProjectionAnnotationModel model, Set additions) {
		List deletions = new ArrayList();
		for (Iterator iter = model.getAnnotationIterator(); iter.hasNext();) {
			Object annotation = iter.next();
			if (annotation instanceof ProjectionAnnotation) {
				Position position = model.getPosition((Annotation) annotation);
				if (additions.contains(position)) {
					additions.remove(position);
				} else {
					deletions.add(annotation);
				}
			}
		}
		return (Annotation[]) deletions.toArray(new Annotation[deletions.size()]);
	}

	public void updateFoldingRegions(AntModel antModel) {
		fPositionToElement= new HashMap();
		try {
			ProjectionAnnotationModel model = (ProjectionAnnotationModel) fEditor.getAdapter(ProjectionAnnotationModel.class);
			if (model == null) {
				return;
			}
	
			Set currentRegions= new HashSet();
			List root= new ArrayList();
			AntProjectNode node= antModel.getProjectNode();
			if (node != null && node.getOffset() != -1) {
				root.add(node);
				List nodes= antModel.getNonStructuralNodes();
				root.addAll(nodes);
			}
			addFoldingRegions(currentRegions, root);
			updateFoldingRegions(model, currentRegions);
		} catch (BadLocationException be) {
			//ignore as document has changed
		}
	}
	
	private void addFoldingRegions(Set regions, List children) throws BadLocationException  {
		// add a Position to 'regions' for each foldable region
		Iterator iter= children.iterator();
		while (iter.hasNext()) {
			AntElementNode element = (AntElementNode) iter.next();
			if (element.getImportNode() !=  null || element.isExternal()) {
				continue; //elements are not really in this document and therefore are not foldable
			}
			int startLine= fDocument.getLineOfOffset(element.getOffset());
			int endLine= fDocument.getLineOfOffset(element.getOffset() + element.getLength());
			if (startLine < endLine) {
				int start= fDocument.getLineOffset(startLine);
				int end= fDocument.getLineOffset(endLine) + fDocument.getLineLength(endLine);
				Position position= new Position(start, end - start);
				regions.add(position);
				fPositionToElement.put(position, element);
			}
			
			List childNodes= element.getChildNodes();
			if (childNodes != null) {
				addFoldingRegions(regions, childNodes);
			}
		}
	}

	public void setDocument(IDocument document) {
		fDocument= document;
	}
}
