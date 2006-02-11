package org.eclipse.jface.internal.databinding.api.observable.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class WritableList extends ObservableList {

	public WritableList() {
		super(new ArrayList());
	}

	public Object set(int index, Object element) {
		getterCalled();
		Object oldElement = wrappedList.set(index, element);
		fireListChange(new ListDiff(
				new ListDiffEntry(index, false, oldElement), new ListDiffEntry(
						index, true, element)));
		return oldElement;
	}

	public Object remove(int index) {
		getterCalled();
		Object oldElement = wrappedList.remove(index);
		fireListChange(new ListDiff(new ListDiffEntry(index, false, oldElement)));
		return oldElement;
	}

	public boolean add(Object element) {
		getterCalled();
		boolean added = wrappedList.add(element);
		if (added) {
			fireListChange(new ListDiff(new ListDiffEntry(
					wrappedList.size() - 1, true, element)));
		}
		return added;
	}

	public void add(int index, Object element) {
		wrappedList.add(index, element);
		fireListChange(new ListDiff(new ListDiffEntry(index, true, element)));
	}

	public boolean addAll(Collection c) {
		ListDiffEntry[] entries = new ListDiffEntry[c.size()];
		int i = 0;
		int addIndex = c.size();
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			entries[i++] = new ListDiffEntry(addIndex++, true, element);
		}
		boolean added = wrappedList.addAll(c);
		fireListChange(new ListDiff(entries));
		return added;
	}

	public boolean addAll(int index, Collection c) {
		ListDiffEntry[] entries = new ListDiffEntry[c.size()];
		int i = 0;
		int addIndex = index;
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			entries[i++] = new ListDiffEntry(addIndex++, true, element);
		}
		boolean added = wrappedList.addAll(index, c);
		fireListChange(new ListDiff(entries));
		return added;
	}

	public boolean remove(Object o) {
		int index = wrappedList.indexOf(o);
		if (index == -1) {
			return false;
		}
		wrappedList.remove(index);
		fireListChange(new ListDiff(new ListDiffEntry(index, false, o)));
		return true;
	}

	public boolean removeAll(Collection c) {
		List entries = new ArrayList();
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			int removeIndex = wrappedList.indexOf(element);
			if (removeIndex != -1) {
				wrappedList.remove(removeIndex);
				entries.add(new ListDiffEntry(removeIndex, true, element));
			}
		}
		fireListChange(new ListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()])));
		return entries.size() > 0;
	}

	public boolean retainAll(Collection c) {
		List entries = new ArrayList();
		int removeIndex = 0;
		for (Iterator it = wrappedList.iterator(); it.hasNext();) {
			Object element = it.next();
			if (!c.contains(element)) {
				entries.add(new ListDiffEntry(removeIndex, false, element));
				it.remove();
			} else {
				// only increment if we haven't removed the current element
				removeIndex++;
			}
		}
		fireListChange(new ListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()])));
		return entries.size() > 0;
	}

	public void clear() {
		List entries = new ArrayList();
		for (Iterator it = wrappedList.iterator(); it.hasNext();) {
			Object element = it.next();
			// always report 0 as the remove index
			entries.add(new ListDiffEntry(0, false, element));
		}
		fireListChange(new ListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()])));
	}
}
