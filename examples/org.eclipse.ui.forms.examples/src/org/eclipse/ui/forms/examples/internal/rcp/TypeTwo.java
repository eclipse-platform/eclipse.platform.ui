/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.examples.internal.rcp;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TypeTwo extends NamedObject {
	public static final String P_FLAG1="flag1";
	public static final String P_FLAG2="flag2";
	private boolean flag1;
	private boolean flag2;

	/**
	 * @param name
	 */
	public TypeTwo(String name, boolean flag1, boolean flag2) {
		super(name);
		this.flag1 = flag1;
		this.flag2 = flag2;
	}
	public boolean getFlag1() {
		return flag1;
	}
	public boolean getFlag2() {
		return flag2;
	}
	public void setFlag1(boolean flag1) {
		this.flag1 = flag1;
		model.fireModelChanged(new Object[] {this}, IModelListener.CHANGED, P_FLAG1);
	}
	public void setFlag2(boolean flag2) {
		this.flag2 = flag2;
		model.fireModelChanged(new Object[] {this}, IModelListener.CHANGED, P_FLAG2);
	}
}