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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.activities.CategoryEvent;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.ICategoryListener;
import org.eclipse.ui.activities.NotDefinedException;

import org.eclipse.ui.internal.util.Util;

final class Category implements ICategory {
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Category.class.getName().hashCode();
	private final static Set strongReferences = new HashSet();
	private Set categoryActivityBindings;
	private transient ICategoryActivityBinding[] categoryActivityBindingsAsArray;
	private List categoryListeners;
	private boolean defined;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String id;
	private String name;
	private transient String string;
    private String description;

	Category(String id) {
		if (id == null)
			throw new NullPointerException();

		this.id = id;
	}

	public void addCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null)
			throw new NullPointerException();

		if (categoryListeners == null)
			categoryListeners = new ArrayList();

		if (!categoryListeners.contains(categoryListener))
			categoryListeners.add(categoryListener);

		strongReferences.add(this);
	}

	public int compareTo(Object object) {
		Category castedObject = (Category) object;
		int compareTo =
			Util.compare(
				(Comparable[]) categoryActivityBindingsAsArray,
				(Comparable[]) castedObject.categoryActivityBindingsAsArray);

		if (compareTo == 0) {
			compareTo = Util.compare(defined, castedObject.defined);

			if (compareTo == 0) {
				compareTo = Util.compare(id, castedObject.id);

				if (compareTo == 0)
					compareTo = Util.compare(name, castedObject.name);
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Category))
			return false;

		Category castedObject = (Category) object;
		boolean equals = true;
		equals
			&= Util.equals(
				categoryActivityBindings,
				castedObject.categoryActivityBindings);
		equals &= Util.equals(defined, castedObject.defined);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		return equals;
	}

	void fireCategoryChanged(CategoryEvent categoryEvent) {
		if (categoryEvent == null)
			throw new NullPointerException();

		if (categoryListeners != null)
			for (int i = 0; i < categoryListeners.size(); i++)
				((ICategoryListener) categoryListeners.get(i)).categoryChanged(
					categoryEvent);
	}

	public Set getCategoryActivityBindings() {
		return categoryActivityBindings;
	}

	public String getId() {
		return id;
	}

	public String getName() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return name;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode =
				hashCode * HASH_FACTOR
					+ Util.hashCode(categoryActivityBindings);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(defined);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public boolean isDefined() {
		return defined;
	}

	public void removeCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null)
			throw new NullPointerException();

		if (categoryListeners != null)
			categoryListeners.remove(categoryListener);

		if (categoryListeners.isEmpty())
			strongReferences.remove(this);
	}

	boolean setCategoryActivityBindings(Set categoryActivityBindings) {
		categoryActivityBindings =
			Util.safeCopy(
				categoryActivityBindings,
				ICategoryActivityBinding.class);

		if (!Util
			.equals(categoryActivityBindings, this.categoryActivityBindings)) {
			this.categoryActivityBindings = categoryActivityBindings;
			this.categoryActivityBindingsAsArray =
				(
					ICategoryActivityBinding[]) this
						.categoryActivityBindings
						.toArray(
					new ICategoryActivityBinding[this
						.categoryActivityBindings
						.size()]);
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setDefined(boolean defined) {
		if (defined != this.defined) {
			this.defined = defined;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	boolean setName(String name) {
		if (!Util.equals(name, this.name)) {
			this.name = name;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(categoryActivityBindings);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.ICategory#getDescription()
     */
    public String getDescription() throws NotDefinedException {
		if (!defined)
			throw new NotDefinedException();

		return description;
    }

    public boolean setDescription(String description) {
		if (!Util.equals(description, this.description)) {
			this.description = description;
			hashCodeComputed = false;
			hashCode = 0;
			string = null;
			return true;
		}

		return false;
    }
}
