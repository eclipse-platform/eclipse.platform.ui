/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextEditChangeGroup;

/**
 * A default content provider to present a hierarchy of <code>IChange</code>
 * objects in a tree viewer.
 * 
 * TODO should remove dependency to JDT/Core 
 *      (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=61312)
 */
class ChangeElementContentProvider  implements ITreeContentProvider {
	
	private static final ChangeElement[] EMPTY_CHILDREN= new ChangeElement[0];
	
	private static class OffsetComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			TextEditChangeGroup c1= (TextEditChangeGroup)o1;
			TextEditChangeGroup c2= (TextEditChangeGroup)o2;
			int p1= getOffset(c1);
			int p2= getOffset(c2);
			if (p1 < p2)
				return -1;
			if (p1 > p2)
				return 1;
			// same offset
			return 0;	
		}
		private int getOffset(TextEditChangeGroup edit) {
			return edit.getRegion().getOffset();
		}
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#inputChanged
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#getChildren
	 */
	public Object[] getChildren(Object o) {
		ChangeElement element= (ChangeElement)o;
		ChangeElement[] children= element.getChildren();
		if (children == null) {
			children= createChildren(element);
		}
		return children;
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#getParent
	 */
	public Object getParent(Object element){
		return ((ChangeElement)element).getParent();
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#hasChildren
	 */
	public boolean hasChildren(Object element){
		Object[] children= getChildren(element);
		return children != null && children.length > 0;
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#dispose
	 */
	public void dispose(){
	}
	
	/* non Java-doc
	 * @see ITreeContentProvider#getElements
	 */
	public Object[] getElements(Object element){
		return getChildren(element);
	}
	
	private ChangeElement[] createChildren(ChangeElement object) {
		ChangeElement[] result= EMPTY_CHILDREN;
		if (!(object instanceof DefaultChangeElement))
			return result;
		
		DefaultChangeElement changeElement= (DefaultChangeElement)object;
		Change change= changeElement.getChange();
		if (change instanceof CompositeChange) {
			List children= new ArrayList();
			getFlattendedChildren(children, changeElement, (CompositeChange)change);
			result= (ChangeElement[])children.toArray(new ChangeElement[children.size()]);
		} else if (change instanceof TextChange) {
			TextChange textChange= (TextChange)change;
			ICompilationUnit cunit= (ICompilationUnit)textChange.getAdapter(ICompilationUnit.class);
			if (cunit != null) {
				List children= new ArrayList(5);
				Map map= new HashMap(20);
				TextEditChangeGroup[] changes=getSortedTextEditChanges(textChange);
				for (int i= 0; i < changes.length; i++) {
					TextEditChangeGroup tec= changes[i];
					try {
						IJavaElement element= getModifiedJavaElement(tec, cunit);
						if (element.equals(cunit)) {
							children.add(new TextEditChangeElement(changeElement, tec));
						} else {
							PseudoJavaChangeElement pjce= getChangeElement(map, element, children, changeElement);
							pjce.addChild(new TextEditChangeElement(pjce, tec));
						}
					} catch (JavaModelException e) {
						children.add(new TextEditChangeElement(changeElement, tec));
					}
				}
				result= (ChangeElement[]) children.toArray(new ChangeElement[children.size()]);
			} else {
				TextEditChangeGroup[] changes= getSortedTextEditChanges((TextChange)change);
				result= new ChangeElement[changes.length];
				for (int i= 0; i < changes.length; i++) {
					result[i]= new TextEditChangeElement(changeElement, changes[i]);
				}
			}
		}
		changeElement.setChildren(result);
		return result;
	}
	
	private TextEditChangeGroup[] getSortedTextEditChanges(TextChange change) {
		TextEditChangeGroup[] edits= change.getTextEditChangeGroups();
		List result= new ArrayList(edits.length);
		for (int i= 0; i < edits.length; i++) {
			if (!edits[i].getTextEditGroup().isEmpty())
				result.add(edits[i]);
		}
		Comparator comparator= new OffsetComparator();
		Collections.sort(result, comparator);
		return (TextEditChangeGroup[])result.toArray(new TextEditChangeGroup[result.size()]);
	}
	
	private PseudoJavaChangeElement getChangeElement(Map map, IJavaElement element, List children, ChangeElement cunitChange) {
		PseudoJavaChangeElement result= (PseudoJavaChangeElement)map.get(element);
		if (result != null)
			return result;
		IJavaElement parent= element.getParent();
		if (parent instanceof ICompilationUnit) {
			result= new PseudoJavaChangeElement(cunitChange, element);
			children.add(result);
			map.put(element, result);
		} else {
			PseudoJavaChangeElement parentChange= getChangeElement(map, parent, children, cunitChange);
			result= new PseudoJavaChangeElement(parentChange, element);
			parentChange.addChild(result);
			map.put(element, result);
		}
		return result;
	}
	
	private IJavaElement getModifiedJavaElement(TextEditChangeGroup edit, ICompilationUnit cunit) throws JavaModelException {
		IRegion range= edit.getRegion();
		if (range.getOffset() == 0 && range.getLength() == 0)
			return cunit;
		IJavaElement result= cunit.getElementAt(range.getOffset());
		if (result == null)
			return cunit;
		
		try {
			while(true) {
				ISourceReference ref= (ISourceReference)result;
				IRegion sRange= new Region(ref.getSourceRange().getOffset(), ref.getSourceRange().getLength());
				if (result.getElementType() == IJavaElement.COMPILATION_UNIT || result.getParent() == null || coveredBy(edit, sRange))
					break;
				result= result.getParent();
			}
		} catch(JavaModelException e) {
			// Do nothing, use old value.
		} catch(ClassCastException e) {
			// Do nothing, use old value.
		}
		return result;
	}
	
	private void getFlattendedChildren(List result, DefaultChangeElement parent, CompositeChange focus) {
		Change[] changes= focus.getChildren();
		for (int i= 0; i < changes.length; i++) {
			Change change= changes[i];
			if (change instanceof CompositeChange && ((CompositeChange)change).isSynthetic()) {
				getFlattendedChildren(result, parent, (CompositeChange)change);
			} else {
				result.add(new DefaultChangeElement(parent, change));
			}
		}
	}
	
	public boolean coveredBy(TextEditChangeGroup group, IRegion sourceRegion) {
		int sLength= sourceRegion.getLength();
		if (sLength == 0)
			return false;
		int sOffset= sourceRegion.getOffset();
		int sEnd= sOffset + sLength - 1;
		TextEdit[] edits= group.getTextEdits();
		for (int i= 0; i < edits.length; i++) {
			TextEdit edit= edits[i];
			if (edit.isDeleted())
				return false;
			int rOffset= edit.getOffset();
			int rLength= edit.getLength();
			int rEnd= rOffset + rLength - 1;
		    if (rLength == 0) {
				if (!(sOffset < rOffset && rOffset <= sEnd))
					return false;
			} else {
				if (!(sOffset <= rOffset && rEnd <= sEnd))
					return false;
			}
		}
		return true;
	}
}

