package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class AntTargetContentProvider implements IStructuredContentProvider {

	protected List elements = new ArrayList();
	protected TableViewer viewer;

	public void add(Object o) {
		elements.add(o);
		viewer.add(o);
	}
	
	public void addAll(List list) {
		elements.addAll(list);
		viewer.add(list.toArray());
	}

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		return (Object[]) elements.toArray(new Object[elements.size()]);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		elements.clear();
		if (newInput != null) {
			
			elements.addAll(Arrays.asList((Object[])newInput));
		}
	}

	public void remove(Object o) {
		elements.remove(o);
		viewer.remove(o);
	}
	
	public void removeAll(List list) {
		elements.removeAll(list);
		viewer.remove(list.toArray());
	}
	
	public void remove(IStructuredSelection selection) {
		Object[] array= selection.toArray();
		elements.remove(array);
		viewer.remove(array);
	}
}
