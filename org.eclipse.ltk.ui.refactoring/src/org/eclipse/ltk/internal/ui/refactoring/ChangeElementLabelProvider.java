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
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IFile;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;

class ChangeElementLabelProvider extends LabelProvider {

	private Map fDescriptorImageMap= new HashMap();
	private boolean fShowQualification= true;

	public ChangeElementLabelProvider() {
	}
		
	public void setShowQualification(boolean showQualification) {
		fShowQualification= showQualification;
		LabelProviderChangedEvent event= new LabelProviderChangedEvent(this, null);
		fireLabelProviderChanged(event);
	}
	
	public Image getImage(Object object) {
		if (object instanceof DefaultChangeElement) {
			Object element= ((DefaultChangeElement)object).getChange();
			return doGetImage(element);
		} else if (object instanceof TextEditChangeElement) {
			Object element= ((TextEditChangeElement)object).getChangeGroup();
			return doGetImage(element);
		} else if (object instanceof ChangeElement) {
			Object modifiedElement= ((ChangeElement)object).getModifiedElement();
			if (modifiedElement instanceof IAdaptable) {
				IWorkbenchAdapter adapter= (IWorkbenchAdapter)((IAdaptable)modifiedElement).getAdapter(IWorkbenchAdapter.class);
				if (adapter != null) {
					return manageImageDescriptor(adapter.getImageDescriptor(modifiedElement));
				}
			}
		}
		return super.getImage(object);
	}
	
	public String getText(Object object) {
		if (object instanceof DefaultChangeElement) {
			Change change= ((DefaultChangeElement)object).getChange();
			if (!fShowQualification)
				return change.getName();
			
			if (change instanceof TextFileChange) {
				IFile file= ((TextFileChange)change).getFile();
				return Messages.format(
					RefactoringUIMessages.PreviewWizardPage_changeElementLabelProvider_textFormat,
					new String[] {file.getName(), getPath(file)});
			} else {
				return change.getName();
			}
		} else if (object instanceof TextEditChangeElement) {
			TextEditChangeElement element= (TextEditChangeElement)object;
			String result= element.getChangeGroup().getName();
			return result;
		}  else if (object instanceof ChangeElement) {
			Object modifiedElement= ((ChangeElement)object).getModifiedElement();
			if (modifiedElement instanceof IAdaptable) {
				IWorkbenchAdapter adapter= (IWorkbenchAdapter)((IAdaptable)modifiedElement).getAdapter(IWorkbenchAdapter.class);
				if (adapter != null) {
					return adapter.getLabel(modifiedElement);
				}
			}
		}
		return super.getText(object);
	}
	
	public void dispose() {
		for (Iterator iter= fDescriptorImageMap.values().iterator(); iter.hasNext(); ) {
			Image image= (Image)iter.next();
			image.dispose();
		}
		super.dispose();
	}
	
	private Image doGetImage(Object element) {
		ImageDescriptor descriptor= null;
		if (descriptor == null) {
			if (element instanceof TextEditChangeElement) {
				descriptor= RefactoringPluginImages.DESC_OBJS_TEXT_EDIT;
			} else if (element instanceof CompositeChange) {
				descriptor= RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE;	
			} else if (element instanceof TextFileChange) {
				descriptor= RefactoringPluginImages.DESC_OBJS_FILE_CHANGE;
			} else {
				descriptor= RefactoringPluginImages.DESC_OBJS_DEFAULT_CHANGE;
			}
		}
		return manageImageDescriptor(descriptor);
	}
	
	private Image manageImageDescriptor(ImageDescriptor descriptor) {
		Image image= (Image)fDescriptorImageMap.get(descriptor);
		if (image == null) {
			image= descriptor.createImage();
			fDescriptorImageMap.put(descriptor, image);
		}
		return image;
	}

	private String getPath(IFile file) {
		StringBuffer result= new StringBuffer(file.getProject().getName());
		String projectRelativePath= file.getParent().getProjectRelativePath().toString();
		if (projectRelativePath.length() > 0) {
			result.append('/');
			result.append(projectRelativePath);
		}
		return result.toString();
	}	
}
