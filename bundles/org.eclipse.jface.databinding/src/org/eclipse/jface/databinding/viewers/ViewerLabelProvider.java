package org.eclipse.jface.databinding.viewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.swt.SWTUtil;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;

public class ViewerLabelProvider implements IViewerLabelProvider, ILabelProvider {

	private List listeners = new ArrayList();
	
	/**
	 * Subclasses should override this method. They should not call the base class implementation.
	 */
	public void updateLabel(ViewerLabel label, Object element) {
		label.setText(element.toString());
	}
	
	protected final void fireChangeEvent(Collection changes) {
		final LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, changes.toArray());
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			ILabelProviderListener l = (ILabelProviderListener) iter.next();
			
			try {
				l.labelProviderChanged(event);
			} catch (Exception e) {
				SWTUtil.logException(e);
			}
		}
	}
	
	public final Image getImage(Object element) {
		ViewerLabel label = new ViewerLabel("", null); //$NON-NLS-1$
		updateLabel(label, element);
		return label.getImage();
	}
	
	public final String getText(Object element) {
		ViewerLabel label = new ViewerLabel("", null); //$NON-NLS-1$
		updateLabel(label, element);
		return label.getText();
	}	
	
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		listeners.clear();
	}

	public final boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

}
