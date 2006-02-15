package org.eclipse.ui.internal.intro.universal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;


public class ExtensionDataTransfer extends ByteArrayTransfer {

	private static final String MYTYPENAME = "ExtensionData"; //$NON-NLS-1$
	private static final int MYTYPEID = registerType(MYTYPENAME);
	private static ExtensionDataTransfer _instance = new ExtensionDataTransfer();

	public static ExtensionDataTransfer getInstance() {
		return _instance;
	}

	protected String[] getTypeNames() {
		return new String[] { MYTYPENAME };
	}

	protected int[] getTypeIds() {
		return new int[] { MYTYPEID };
	}

	public void javaToNative(Object object, TransferData transferData) {
		if (!checkMyType(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		ExtensionData[] myTypes = (ExtensionData[]) object;
		try {
			// write data to a byte array and then ask super to convert to pMedium
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream writeOut = new DataOutputStream(out);
			for (int i = 0, length = myTypes.length; i < length; i++) {
				ExtensionData ed = myTypes[i];
				byte[] buffer = ed.getId().getBytes();
				writeOut.writeInt(ed.getId().length());
				writeOut.write(buffer);
				writeOut.writeInt(ed.getName().length());
				buffer = ed.getName().getBytes();
				writeOut.write(buffer);
				writeOut.writeInt(myTypes[i].getImportance());
			}
			byte[] buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch (IOException e) {
		}
	}

	public Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			ExtensionData[] myData = new ExtensionData[0];
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				DataInputStream readIn = new DataInputStream(in);
				while (readIn.available() > 12) {
					int importance;
					String id;
					String name;
					int size = readIn.readInt();
					byte[] buff = new byte[size];
					readIn.read(buff);
					id = new String(buff);
					size = readIn.readInt();
					buff = new byte[size];
					readIn.read(buff);
					name = new String(buff);
					importance = readIn.readInt();

					ExtensionData[] newMyData = new ExtensionData[myData.length + 1];
					System.arraycopy(myData, 0, newMyData, 0, myData.length);
					newMyData[myData.length] = new ExtensionData(id, name, importance);
					myData = newMyData;
				}
				readIn.close();
			} catch (IOException ex) {
				return null;
			}
			return myData;
		}

		return null;
	}


	boolean checkMyType(Object object) {
		if (object == null || !(object instanceof ExtensionData[]) || ((ExtensionData[]) object).length == 0) {
			return false;
		}
		ExtensionData[] myTypes = (ExtensionData[]) object;
		for (int i = 0; i < myTypes.length; i++) {
			if (myTypes[i] == null || myTypes[i].getId() == null || myTypes[i].getName() == null)
				return false;
		}
		return true;
	}

	protected boolean validate(Object object) {
		return checkMyType(object);
	}
}
