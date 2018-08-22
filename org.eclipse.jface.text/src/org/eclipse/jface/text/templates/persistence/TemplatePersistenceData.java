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
package org.eclipse.jface.text.templates.persistence;

import org.eclipse.jface.text.templates.Template;


/**
 * TemplatePersistenceData stores information about a template. It uniquely
 * references contributed templates via their id. Contributed templates may be
 * deleted or modified. All template may be enabled or not.
 * <p>
 * Clients may use this class, although this is not usually needed except when
 * implementing a custom template preference page or template store. This class
 * is not intended to be subclassed.
 * </p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated See {@link org.eclipse.text.templates.TemplatePersistenceData}
 */
@Deprecated
public class TemplatePersistenceData extends org.eclipse.text.templates.TemplatePersistenceData {

	org.eclipse.text.templates.TemplatePersistenceData ref;

	/**
	 * In some cases, we must continue to respect the deprecated TemplatePresistenceData
	 * even though we are given {@link org.eclipse.text.templates.TemplatePersistenceData}.
	 *
	 * @param data The {@link org.eclipse.text.templates.TemplatePersistenceData} that will
	 * underlie this object.
	 * @since 3.14
	 */
	public TemplatePersistenceData(org.eclipse.text.templates.TemplatePersistenceData data) {
		super(data.getTemplate(), data.isEnabled(), data.getId()); // these are ignored
		this.ref= data;
	}

	public TemplatePersistenceData(Template template, boolean enabled) {
		super(template, enabled);
	}

	public TemplatePersistenceData(Template template, boolean enabled, String id) {
		super(template, enabled, id);
	}

	@Override
	public String getId() {
		return (ref != null) ? ref.getId() : super.getId();
	}

	@Override
	public boolean isDeleted() {
		return (ref != null) ? ref.isDeleted() : super.isDeleted();
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		if (ref != null) {
			ref.setDeleted(isDeleted);
		} else {
			super.setDeleted(isDeleted);
		}
	}

	@Override
	public Template getTemplate() {
		return (ref != null) ? ref.getTemplate() : super.getTemplate();
	}

	@Override
	public void setTemplate(Template template) {
		if (ref != null) {
			ref.setTemplate(template);
		} else {
			super.setTemplate(template);
		}
	}

	@Override
	public boolean isCustom() {
		return (ref != null) ? ref.isCustom() : super.isCustom();
	}

	@Override
	public boolean isModified() {
		return (ref != null) ? ref.isModified() : super.isModified();
	}

	@Override
	public boolean isUserAdded() {
		return (ref != null) ? ref.isUserAdded() : super.isUserAdded();
	}

	@Override
	public void revert() {
		if (ref != null) {
			ref.revert();
		} else {
			super.revert();
		}
	}

	@Override
	public boolean isEnabled() {
		return (ref != null) ? ref.isEnabled() : super.isEnabled();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (ref != null) {
			ref.setEnabled(isEnabled);
		} else {
			super.setEnabled(isEnabled);
		}
	}

	@Override
	public boolean equals(Object other) {
		return (ref != null) ? ref.equals(other) : super.equals(other);
	}

	@Override
	public int hashCode() {
		return (ref != null) ? ref.hashCode() : super.hashCode();
	}

}
