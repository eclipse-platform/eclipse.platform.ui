package org.eclipse.e4.tools.emf.ui.internal.common.objectdata;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFValueProperty;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;

public class ObjectViewer {
	public TreeViewer createViewer(Composite parent, EStructuralFeature feature, IObservableValue master, IResourcePool resourcePool) {
		final TreeViewer viewer = new TreeViewer(parent);
		viewer.setContentProvider(new ContentProviderImpl());
		viewer.setLabelProvider(new LabelProviderImpl(resourcePool));
		viewer.setComparator(new ViewerComparatorImpl());
		IEMFValueProperty property = EMFProperties.value(feature);
		IObservableValue value = property.observeDetail(master);
		value.addValueChangeListener(new IValueChangeListener() {

			public void handleValueChange(ValueChangeEvent event) {
				if (event.diff.getNewValue() != null) {
					viewer.setInput(Collections.singleton(new JavaObject(event.diff.getNewValue())));
				} else {
					viewer.setInput(Collections.emptyList());
				}
			}
		});
		return viewer;
	}

	class ViewerComparatorImpl extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof JavaAttribute) {
				if (e2 instanceof JavaAttribute) {
					JavaAttribute a1 = (JavaAttribute) e1;
					JavaAttribute a2 = (JavaAttribute) e2;

					if (a1.isStatic() && !a2.isStatic()) {
						return -1;
					} else if (!a1.isStatic() && a2.isStatic()) {
						return 1;
					}

					int rv = Integer.valueOf(a1.getAccessLevel().value).compareTo(a2.getAccessLevel().value);
					if (rv == 0) {
						return a1.getName().compareTo(a2.getName());
					} else {
						return rv;
					}
				} else {
					return -1;
				}
			}
			return super.compare(viewer, e1, e2);
		}
	}

	class LabelProviderImpl extends StyledCellLabelProvider {

		private IResourcePool resourcePool;

		public LabelProviderImpl(IResourcePool resourcePool) {
			this.resourcePool = resourcePool;
		}

		@Override
		public void update(ViewerCell cell) {
			if (cell.getElement() instanceof JavaObject) {
				JavaObject o = (JavaObject) cell.getElement();
				cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_class_obj));
				cell.setText(o.getName());
			} else if (cell.getElement() instanceof JavaAttribute) {
				JavaAttribute o = (JavaAttribute) cell.getElement();
				StyledString string = new StyledString();
				if (o.isInjected()) {
					string.append("<injected> ", StyledString.COUNTER_STYLER);
				}
				string.append(o.getName());
				string.append(" : " + o.getType() + " - " + o.getValue(), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$ //$NON-NLS-2$
				cell.setText(string.getString());
				cell.setStyleRanges(string.getStyleRanges());
				switch (o.getAccessLevel()) {
				case PUBLIC:
					cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_field_public_obj));
					break;
				case PRIVATE:
					cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_field_private_obj));
					break;
				case DEFAULT:
					cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_field_default_obj));
					break;
				default:
					cell.setImage(resourcePool.getImageUnchecked(ResourceProvider.IMG_Obj16_field_protected_obj));
					break;
				}
			}

			super.update(cell);
		}
	}

	class ContentProviderImpl implements ITreeContentProvider {

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			return ((Collection<?>) inputElement).toArray();

		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof JavaObject) {
				return ((JavaObject) parentElement).getAttributes().toArray();
			} else if (parentElement instanceof JavaAttribute) {
				return ((JavaAttribute) parentElement).getAttributes().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

	}
}
