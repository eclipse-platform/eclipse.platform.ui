package org.eclipse.e4.tools.emf.ui.internal.common.objectdata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

public class JavaAttribute {
	private JavaObject object;
	private JavaAttribute attribute;

	private Field field;

	public JavaAttribute(JavaObject object, Field field) {
		this.object = object;
		this.field = field;
		this.field.setAccessible(true);
	}

	public JavaAttribute(JavaObject object, JavaAttribute attribute, Field field) {
		this.object = object;
		this.field = field;
		this.field.setAccessible(true);
		this.attribute = attribute;
	}

	public boolean isInjected() {
		return this.field.getAnnotation(Inject.class) != null;
	}

	public boolean isStatic() {
		return (this.field.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
	}

	public String getValue() {
		if (field.getType().isPrimitive()) {
			try {
				return field.get(object.getInstance()) + "";
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				Object o = field.get(object.getInstance());
				if (o == null) {
					return "<null>";
				} else {
					return "id=" + Integer.toHexString(System.identityHashCode(o));
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return "";
	}

	public List<JavaAttribute> getAttributes() {
		try {
			if (field.getType().isPrimitive()) {
				return Collections.emptyList();
			} else {
				return new JavaObject(field.get(object.getInstance())).getAttributes();
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public String getName() {
		return field.getName();
	}

	public String getType() {
		return field.getType().getSimpleName();
	}

	public AccessLevel getAccessLevel() {
		int m = field.getModifiers();
		if ((m & Modifier.PUBLIC) == Modifier.PUBLIC) {
			return AccessLevel.PUBLIC;
		} else if ((m & Modifier.PRIVATE) == Modifier.PRIVATE) {
			return AccessLevel.PRIVATE;
		} else if ((m & Modifier.PROTECTED) == Modifier.PROTECTED) {
			return AccessLevel.PROTECTED;
		} else {
			return AccessLevel.DEFAULT;
		}
	}
}
