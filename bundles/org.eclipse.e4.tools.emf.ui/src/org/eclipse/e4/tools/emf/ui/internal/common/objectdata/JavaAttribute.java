package org.eclipse.e4.tools.emf.ui.internal.common.objectdata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.osgi.framework.FrameworkUtil;

public class JavaAttribute {
	private final JavaObject object;

	private final Field field;

	public JavaAttribute(JavaObject object, Field field) {
		this.object = object;
		this.field = field;
		this.field.setAccessible(true);
	}

	public boolean isInjected() {
		return field.getAnnotation(Inject.class) != null;
	}

	public boolean isStatic() {
		return (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC;
	}

	public String getValue() {
		final Object value = getFieldValue();
		if (field.getType().isPrimitive()) {
			return value + ""; //$NON-NLS-1$
		}
		if (value == null) {
			return "<null>"; //$NON-NLS-1$
		} else if (value instanceof String) {
			return value.toString();
		} else {
			String name;
			if (value.getClass().isAnonymousClass()) {
				name = value.getClass().getName().substring(value.getClass().getPackage().getName().length() + 1);
			} else {
				name = value.getClass().getSimpleName();
			}

			return name + " (id = " + Integer.toHexString(System.identityHashCode(value)) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public List<JavaAttribute> getAttributes() {
		try {
			if (field.getType().isPrimitive()) {
				return Collections.emptyList();
			}
			return new JavaObject(field.get(object.getInstance())).getAttributes();

		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
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
		final int m = field.getModifiers();
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

	public String getContextKey() {
		if (isInjected()) {
			final Named named = field.getAnnotation(Named.class);
			if (named != null) {
				return "@Named(" + named.value() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			final Preference preference = field.getAnnotation(Preference.class);
			if (preference != null) {
				String path = preference.nodePath();
				if (path == null || path.trim().length() == 0) {
					path = FrameworkUtil.getBundle(getFieldValue().getClass()).getSymbolicName();
				}
				return "@Preference(" + preference.nodePath() + "/" + preference.value() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			final EventTopic topic = field.getAnnotation(EventTopic.class);

			if (topic != null) {
				return "@Topic(" + topic.value() + ")"; //$NON-NLS-1$//$NON-NLS-2$
			}

			final UIEventTopic uiTopic = field.getAnnotation(UIEventTopic.class);
			if (uiTopic != null) {
				return "@UITopic(" + uiTopic.value() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			final Translation translation = field.getAnnotation(Translation.class);
			if (translation != null) {
				return "@Translation(" + field.getType().getName() + ")"; //$NON-NLS-1$//$NON-NLS-2$
			}

			return field.getType().getName();
		}

		return ""; //$NON-NLS-1$
	}

	public Object getFieldValue() {
		try {
			return field.get(object.getInstance());
		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
