package org.eclipse.team.internal.ui.target;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class SiteExplorerViewLabelProvider extends WorkbenchLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return super.getImage(element);
		}
		return null;
	}
	public String getColumnText(Object element, int columnIndex) {		
		try {
			switch (columnIndex) {
				case 0 :
					return super.getText(element);
				case 1 :
					if(element instanceof RemoteResourceElement) {
						return new Integer(((RemoteResourceElement)element).getRemoteResource().getSize()).toString() + " Bytes";						
					}
				case 2 :
					if(element instanceof RemoteResourceElement) {
						return ((RemoteResourceElement)element).getRemoteResource().getLastModified();
					}
				case 3 :
					if(element instanceof RemoteResourceElement) {
						return ((RemoteResourceElement)element).getRemoteResource().getURL().toExternalForm();
					}
			}
			return "";
		} catch(TeamException e) {
			return "";
		}
	}
}
