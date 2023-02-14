package org.eclipse.e4.tools.emf.ui.internal.common.objectdata;

public enum AccessLevel {
	PUBLIC(3), PROTECTED(2), DEFAULT(1), PRIVATE(0);

	public final int value;

	private AccessLevel(int value) {
		this.value = value;
	}
}
