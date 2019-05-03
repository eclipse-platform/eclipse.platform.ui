/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.ui.activities.CategoryEvent;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.activities.ICategoryListener;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.util.Util;

final class Category implements ICategory {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = Category.class.getName().hashCode();

	private static final Set<Category> strongReferences = new HashSet<>();

	private Set<ICategoryActivityBinding> categoryActivityBindings;

	private transient ICategoryActivityBinding[] categoryActivityBindingsAsArray;

	private List<ICategoryListener> categoryListeners;

	private boolean defined;

	private transient int hashCode = HASH_INITIAL;

	private String id;

	private String name;

	private transient String string;

	private String description;

	Category(String id) {
		if (id == null) {
			throw new NullPointerException();
		}

		this.id = id;
	}

	@Override
	public void addCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null) {
			throw new NullPointerException();
		}

		if (categoryListeners == null) {
			categoryListeners = new ArrayList<>();
		}

		if (!categoryListeners.contains(categoryListener)) {
			categoryListeners.add(categoryListener);
		}

		strongReferences.add(this);
	}

	@Override
	public int compareTo(ICategory object) {
		Category castedObject = (Category) object;
		int compareTo = Util.compare(categoryActivityBindingsAsArray, castedObject.categoryActivityBindingsAsArray);

		if (compareTo == 0) {
			compareTo = Util.compare(defined, castedObject.defined);

			if (compareTo == 0) {
				compareTo = Util.compare(id, castedObject.id);

				if (compareTo == 0) {
					compareTo = Util.compare(name, castedObject.name);
				}
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Category)) {
			return false;
		}

		final Category castedObject = (Category) object;
		return Objects.equals(categoryActivityBindings, castedObject.categoryActivityBindings)
				&& defined == castedObject.defined && Objects.equals(id, castedObject.id)
				&& Objects.equals(name, castedObject.name);
	}

	void fireCategoryChanged(CategoryEvent categoryEvent) {
		if (categoryEvent == null) {
			throw new NullPointerException();
		}

		if (categoryListeners != null) {
			for (int i = 0; i < categoryListeners.size(); i++) {
				categoryListeners.get(i).categoryChanged(categoryEvent);
			}
		}
	}

	@Override
	public Set<ICategoryActivityBinding> getCategoryActivityBindings() {
		return categoryActivityBindings;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() throws NotDefinedException {
		if (!defined) {
			throw new NotDefinedException();
		}

		return name;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(categoryActivityBindings);
			hashCode = hashCode * HASH_FACTOR + Boolean.hashCode(defined);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(name);
			if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
		}

		return hashCode;
	}

	@Override
	public boolean isDefined() {
		return defined;
	}

	@Override
	public void removeCategoryListener(ICategoryListener categoryListener) {
		if (categoryListener == null) {
			throw new NullPointerException();
		}

		if (categoryListeners != null) {
			categoryListeners.remove(categoryListener);
		}

		if (categoryListeners.isEmpty()) {
			strongReferences.remove(this);
		}
	}

	boolean setCategoryActivityBindings(Set<ICategoryActivityBinding> categoryActivityBindings) {
		categoryActivityBindings = Util.safeCopy(categoryActivityBindings, ICategoryActivityBinding.class);

		if (!Objects.equals(categoryActivityBindings, this.categoryActivityBindings)) {
			this.categoryActivityBindings = categoryActivityBindings;
			this.categoryActivityBindingsAsArray = this.categoryActivityBindings
					.toArray(new ICategoryActivityBinding[this.categoryActivityBindings.size()]);
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	boolean setDefined(boolean defined) {
		if (defined != this.defined) {
			this.defined = defined;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	boolean setName(String name) {
		if (!Objects.equals(name, this.name)) {
			this.name = name;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder();
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

	@Override
	public String getDescription() throws NotDefinedException {
		if (!defined) {
			throw new NotDefinedException();
		}

		return description;
	}

	public boolean setDescription(String description) {
		if (!Objects.equals(description, this.description)) {
			this.description = description;
			hashCode = HASH_INITIAL;
			string = null;
			return true;
		}

		return false;
	}
}
