package org.eclipse.e4.tools.emf.ui.internal.common.objectdata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class JavaObject {
	private Object instance;
	private List<JavaAttribute> attributes;

	public JavaObject(Object instance) {
		this.instance = instance;
	}

	public Object getInstance() {
		return instance;
	}

	public List<JavaAttribute> getAttributes() {
		if (attributes == null) {
			initAttributes();
		}
		return attributes;
	}

	private void initAttributes() {
		attributes = new ArrayList<JavaAttribute>();

		if (instance == null) {
			return;
		}

		Class<?> clazz = instance.getClass();
		addDeclaredFields(clazz);
		clazz = clazz.getSuperclass();
		while (clazz != null) {
			addDeclaredFields(clazz);
			clazz = clazz.getSuperclass();
		}
	}

	private void addDeclaredFields(Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {
			attributes.add(new JavaAttribute(this, f));
		}
	}

	public String getName() {
		return instance.getClass().getSimpleName();
	}
}
