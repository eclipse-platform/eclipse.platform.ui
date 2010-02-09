/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.IWorkbenchPartReference;

public class WorkbenchPartReference implements IWorkbenchPartReference {

	private IWorkbenchPage page;
	private MPart part;

	private ListenerList propertyListeners = new ListenerList();
	private ListenerList partPropertyListeners = new ListenerList();

	WorkbenchPartReference(IWorkbenchPage page, MPart part) {
		this.page = page;
		this.part = part;

		IWorkbenchPart workbenchPart = getPart(false);
		if (workbenchPart != null) {
			workbenchPart.addPropertyListener(new IPropertyListener() {
				public void propertyChanged(Object source, int propId) {
					firePropertyListeners(source, propId);
				}
			});

			if (workbenchPart instanceof IWorkbenchPart3) {
				((IWorkbenchPart3) workbenchPart)
						.addPartPropertyListener(new IPropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent event) {
								firePartPropertyListeners(event);
					}
				});

			}
		}
	}

	private void firePropertyListeners(Object source, int propId) {
		for (Object listener : propertyListeners.getListeners()) {
			((IPropertyListener) listener).propertyChanged(source, propId);
		}
	}

	private void firePartPropertyListeners(PropertyChangeEvent event) {
		for (Object listener : partPropertyListeners.getListeners()) {
			((IPropertyChangeListener) listener).propertyChange(event);
		}
	}

	MPart getModel() {
		return part;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getPart(boolean)
	 */
	public IWorkbenchPart getPart(boolean restore) {
		CompatibilityPart compatibilityPart = (CompatibilityPart) part.getObject();
		return compatibilityPart == null ? null : compatibilityPart.getPart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getTitle()
	 */
	public String getTitle() {
		return part.getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getTitleImage()
	 */
	public Image getTitleImage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getTitleToolTip()
	 */
	public String getTitleToolTip() {
		String toolTip = part.getTooltip();
		return toolTip == null ? "" : toolTip; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getId()
	 */
	public String getId() {
		return part.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		propertyListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#removePropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		propertyListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getPage()
	 */
	public IWorkbenchPage getPage() {
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getPartName()
	 */
	public String getPartName() {
		return part.getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getContentDescription()
	 */
	public String getContentDescription() {
		CompatibilityPart compatibilityPart = (CompatibilityPart) part.getObject();
		IWorkbenchPart workbenchPart = compatibilityPart.getPart();
		if (workbenchPart instanceof IWorkbenchPart2) {
			return ((IWorkbenchPart2) workbenchPart).getContentDescription();
		}
		return workbenchPart.getTitle();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#isDirty()
	 */
	public boolean isDirty() {
		IWorkbenchPart part = getPart(false);
		if (part instanceof ISaveablePart) {
			return ((ISaveablePart) part).isDirty();
		}
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#getPartProperty(java.lang.String)
	 */
	public String getPartProperty(String key) {
		IWorkbenchPart part = getPart(false);
		if (part instanceof IWorkbenchPart3) {
			return ((IWorkbenchPart3) part).getPartProperty(key);
		}
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#addPartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPartPropertyListener(IPropertyChangeListener listener) {
		partPropertyListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartReference#removePartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePartPropertyListener(IPropertyChangeListener listener) {
		partPropertyListeners.remove(listener);
	}

}
