/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.definition;

public interface IThemeElementDefinitionOverridable<T> {
	String getId();

	void setValue(T data);

	T getValue();

	boolean isOverridden();

	void setCategoryId(String categoryId);

	void setName(String name);

	void setDescription(String description);
}
