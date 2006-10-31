package org.eclipse.compare.internal.patch;

public class HunkPatchedFileNode extends PatchedFileNode implements IHunkDescriptor {

	public HunkPatchedFileNode(byte[] bytes, String type, String name) {
		super(bytes, type, name);
	}

}
