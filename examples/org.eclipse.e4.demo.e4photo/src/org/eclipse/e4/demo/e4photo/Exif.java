/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class Exif {

	IFD[] ifds;

	private String make;
	private String model;
	private Integer orientation;
	private String software;
	private String timestamp;
	private String exposure;
	private Integer iso;
	private Double aperture;
	private String exposureComp;
	private Boolean flash;
	private Integer width;
	private Integer height;
	private Double focalLength;
	private Integer whiteBalance;
	private String lightSource;
	private String exposureProgram;
	private String gpsLatitude;
	private String gpsLongitude;

	private final String name;

	private URI uri;

	public String gpsLatitudeRef;

	public String gpsLongitudeRef;

	public URI getUri() {
		return uri;
	}

	public String getName() {
		return name;
	}

	public String getMake() {
		return make;
	}

	public String getModel() {
		return model;
	}

	public Integer getOrientation() {
		return orientation;
	}

	public String getSoftware() {
		return software;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getExposure() {
		return exposure;
	}

	public Integer getIso() {
		return iso;
	}

	public Double getAperture() {
		return aperture;
	}

	public String getExposureComp() {
		return exposureComp;
	}

	public Boolean getFlash() {
		return flash;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public Double getFocalLength() {
		return focalLength;
	}

	public Integer getWhiteBalance() {
		return whiteBalance;
	}

	public String getLightSource() {
		return lightSource;
	}

	public String getExposureProgram() {
		return exposureProgram;
	}

	public String getGpsLatitude() {
		if (gpsLatitude == null) {
			return null;
		}
		return ("S".equals(gpsLatitudeRef) ? "-" : "" ) + gpsLatitude;
	}

	public String getGpsLongitude() {
		if (gpsLongitude == null) {
			return null;
		}
		return ("W".equals(gpsLongitudeRef) ? "-" : "" ) + gpsLongitude;
	}
	
	static class IFD {
		Entry[] entries;
	}

	static class Entry {
		int tag;
		int format;
		Object[] data;

		Entry(int tag, int format, Object[] data) {
			this.tag = tag;
			this.format = format;
			this.data = data;
		}

		void applyValue(Exif exif, boolean gpsInfo) {
			switch (tag) {
			case 1:
				if (gpsInfo) {
					exif.gpsLatitudeRef = (String) data[0];
				}
				break;
			case 2:
				if (gpsInfo) {
					double degrees = ((Fraction)data[0]).toDouble();
					double minutes = ((Fraction)data[1]).toDouble();
					double seconds = ((Fraction)data[2]).toDouble();
					if (seconds == 0.0) {
						exif.gpsLatitude = ((int) degrees) + "\u00B0" + minutes + "'";
					} else {
						exif.gpsLatitude = ((int) degrees) + "\u00B0" + ((int)minutes) + "'" + ((int)seconds) + "\"";
					}
				}
				break;
			case 3:
				if (gpsInfo) {
					exif.gpsLongitudeRef = (String) data[0];
				}
				break;
			case 4:
				if (gpsInfo) {
					double degrees = ((Fraction)data[0]).toDouble();
					double minutes = ((Fraction)data[1]).toDouble();
					double seconds = ((Fraction)data[2]).toDouble();
					if (seconds == 0.0) {
						exif.gpsLongitude = ((int) degrees) + "\u00B0" + minutes + "'";
					} else {
						exif.gpsLongitude = ((int) degrees) + "\u00B0" + ((int)minutes) + "'" + ((int)seconds) + "\"";
					}
				}
				break;
			case 0x10F:
				exif.make = (String) data[0];
				break;
			case 0x110:
				exif.model = (String) data[0];
				break;
			case 0x112:
				exif.orientation = (Integer) data[0];
				break;
			case 0x131:
				exif.software = (String) data[0];
				break;
			case 0x132:
				exif.timestamp = (String) data[0];
				break;
			case 0x829A:
				exif.exposure = ((Fraction) data[0]).toFraction();
				break;
			case 0x829D:
				exif.aperture = ((Fraction) data[0]).toDouble();
				break;
			case 0x8822:
				switch (((Integer) data[0]).intValue()) {
				default:
					exif.exposureProgram = "Not defined";
					break;
				case 1:
					exif.exposureProgram = "Manual";
					break;
				case 2:
					exif.exposureProgram = "Normal program";
					break;
				case 3:
					exif.exposureProgram = "Aperture priority";
					break;
				case 4:
					exif.exposureProgram = "Shutter priority";
					break;
				case 5:
					exif.exposureProgram = "Creative program";
					break;
				case 6:
					exif.exposureProgram = "Action program";
					break;
				case 7:
					exif.exposureProgram = "Portrait mode";
					break;
				case 8:
					exif.exposureProgram = "Landscape mode";
					break;
				}
				break;
			case 0x8827:
				exif.iso = (Integer) data[0];
				break;
			case 0x9204:
				exif.exposureComp = ((Fraction) data[0]).toFraction();
				break;
			case 0x9209:
				exif.flash = new Boolean(
						(((Integer) data[0]).intValue() & 1) != 0);
				break;
			case 0x920A:
				exif.focalLength = ((Fraction) data[0]).toDouble();
				break;
			case 0xA002:
				exif.width = (Integer) data[0];
				break;
			case 0xA003:
				exif.height = (Integer) data[0];
				break;
			case 0xA403:
				exif.whiteBalance = (Integer) data[0];
				break;
			case 0x9208:
				switch (((Integer) data[0]).intValue()) {
				default:
					exif.lightSource = "Unknown";
					break;
				case 1:
					exif.lightSource = "Daylight";
					break;
				case 2:
					exif.lightSource = "Fluorescent";
					break;
				case 3:
					exif.lightSource = "Tungsten (incandescent light)";
					break;
				case 4:
					exif.lightSource = "Flash";
					break;
				case 9:
					exif.lightSource = "Fine weather";
					break;
				case 10:
					exif.lightSource = "Cloudy weather";
					break;
				case 11:
					exif.lightSource = "Shade";
					break;
				case 12:
					exif.lightSource = "Daylight fluorescent (D 5700 - 7100K)";
					break;
				case 13:
					exif.lightSource = "Day white fluorescent (N 4600 - 5400K)";
					break;
				case 14:
					exif.lightSource = "Cool white fluorescent (W 3900 - 4500K)";
					break;
				case 15:
					exif.lightSource = "White fluorescent (WW 3200 - 3700K)";
					break;
				case 17:
					exif.lightSource = "Standard light A";
					break;
				case 18:
					exif.lightSource = "Standard light B";
					break;
				case 19:
					exif.lightSource = "Standard light C";
					break;
				case 20:
					exif.lightSource = "D55";
					break;
				case 21:
					exif.lightSource = "D65";
					break;
				case 22:
					exif.lightSource = "D75";
					break;
				case 23:
					exif.lightSource = "D50";
					break;
				case 24:
					exif.lightSource = "ISO studio tungsten";
					break;
				case 255:
					exif.lightSource = "Other light source";
					break;
				}
				break;
			}
		}

		public String toString() {
			String name = "unknown (" + tag + ")";
			switch (tag) {
			case 0x100:
				name = "ImageWidth";
				break;
			case 0x101:
				name = "ImageLength";
				break;
			case 0x102:
				name = "BitsPerSample";
				break;
			case 0x103:
				name = "Compression";
				break;
			case 0x106:
				name = "PhotometricInterpretation";
				break;
			case 0x10A:
				name = "FillOrder";
				break;
			case 0x10D:
				name = "DocumentName";
				break;
			case 0x10E:
				name = "ImageDescription";
				break;
			case 0x10F:
				name = "Make";
				break;
			case 0x110:
				name = "Model";
				break;
			case 0x111:
				name = "StripOffsets";
				break;
			case 0x112:
				name = "Orientation";
				break;
			case 0x115:
				name = "SamplesPerPixel";
				break;
			case 0x116:
				name = "RowsPerStrip";
				break;
			case 0x117:
				name = "StripByteCounts";
				break;
			case 0x11A:
				name = "XResolution";
				break;
			case 0x11B:
				name = "YResolution";
				break;
			case 0x11C:
				name = "PlanarConfiguration";
				break;
			case 0x128:
				name = "ResolutionUnit";
				break;
			case 0x12D:
				name = "TransferFunction";
				break;
			case 0x131:
				name = "Software";
				break;
			case 0x132:
				name = "DateTime";
				break;
			case 0x13B:
				name = "Artist";
				break;
			case 0x13E:
				name = "WhitePoint";
				break;
			case 0x13F:
				name = "PrimaryChromaticities";
				break;
			case 0x156:
				name = "TransferRange";
				break;
			case 0x200:
				name = "JPEGProc";
				break;
			case 0x201:
				name = "JPEGInterchangeFormat";
				break;
			case 0x202:
				name = "JPEGInterchangeFormatLength";
				break;
			case 0x211:
				name = "YCbCrCoefficients";
				break;
			case 0x212:
				name = "YCbCrSubSampling";
				break;
			case 0x213:
				name = "YCbCrPositioning";
				break;
			case 0x214:
				name = "ReferenceBlackWhite";
				break;
			case 0x828F:
				name = "BatteryLevel";
				break;
			case 0x8298:
				name = "Copyright";
				break;
			case 0x829A:
				name = "ExposureTime";
				break;
			case 0x829D:
				name = "FNumber";
				break;
			case 0x83BB:
				name = "IPTC/NAA";
				break;
			case 0x8769:
				name = "ExifIFDPointer";
				break;
			case 0x8773:
				name = "InterColorProfile";
				break;
			case 0x8822:
				name = "ExposureProgram";
				break;
			case 0x8824:
				name = "SpectralSensitivity";
				break;
			case 0x8825:
				name = "GPSInfoIFDPointer";
				break;
			case 0x8827:
				name = "ISOSpeedRatings";
				break;
			case 0x8828:
				name = "OECF";
				break;
			case 0x9000:
				name = "ExifVersion";
				break;
			case 0x9003:
				name = "DateTimeOriginal";
				break;
			case 0x9004:
				name = "DateTimeDigitized";
				break;
			case 0x9101:
				name = "ComponentsConfiguration";
				break;
			case 0x9102:
				name = "CompressedBitsPerPixel";
				break;
			case 0x9201:
				name = "ShutterSpeedValue";
				break;
			case 0x9202:
				name = "ApertureValue";
				break;
			case 0x9203:
				name = "BrightnessValue";
				break;
			case 0x9204:
				name = "ExposureBiasValue";
				break;
			case 0x9205:
				name = "MaxApertureValue";
				break;
			case 0x9206:
				name = "SubjectDistance";
				break;
			case 0x9207:
				name = "MeteringMode";
				break;
			case 0x9208:
				name = "LightSource";
				break;
			case 0x9209:
				name = "Flash";
				break;
			case 0x920A:
				name = "FocalLength";
				break;
			case 0x9214:
				name = "SubjectArea";
				break;
			case 0x927C:
				name = "MakerNote";
				break;
			case 0x9286:
				name = "UserComment";
				break;
			case 0x9290:
				name = "SubSecTime";
				break;
			case 0x9291:
				name = "SubSecTimeOriginal";
				break;
			case 0x9292:
				name = "SubSecTimeDigitized";
				break;
			case 0xA000:
				name = "FlashPixVersion";
				break;
			case 0xA001:
				name = "ColorSpace";
				break;
			case 0xA002:
				name = "PixelXDimension";
				break;
			case 0xA003:
				name = "PixelYDimension";
				break;
			case 0xA004:
				name = "RelatedSoundFile";
				break;
			case 0xA005:
				name = "InteroperabilityIFDPointer";
				break;
			case 0xA20B:
				name = "FlashEnergy";
				break;
			case 0xA20C:
				name = "SpatialFrequencyResponse";
				break;
			case 0xA20E:
				name = "FocalPlaneXResolution";
				break;
			case 0xA20F:
				name = "FocalPlaneYResolution";
				break;
			case 0xA210:
				name = "FocalPlaneResolutionUnit";
				break;
			case 0xA214:
				name = "SubjectLocation";
				break;
			case 0xA215:
				name = "ExposureIndex";
				break;
			case 0xA217:
				name = "SensingMethod";
				break;
			case 0xA300:
				name = "FileSource";
				break;
			case 0xA301:
				name = "SceneType";
				break;
			case 0xA302:
				name = "CFAPattern";
				break;
			case 0xA401:
				name = "CustomRendered";
				break;
			case 0xA402:
				name = "ExposureMode";
				break;
			case 0xA403:
				name = "WhiteBalance";
				break;
			case 0xA404:
				name = "DigitalZoomRatio";
				break;
			case 0xA405:
				name = "FocalLengthIn35mmFilm";
				break;
			case 0xA406:
				name = "SceneCaptureType";
				break;
			case 0xA407:
				name = "GainControl";
				break;
			case 0xA408:
				name = "Contrast";
				break;
			case 0xA409:
				name = "Saturation";
				break;
			case 0xA40A:
				name = "Sharpness";
				break;
			case 0xA40B:
				name = "DeviceSettingDescription";
				break;
			case 0xA40C:
				name = "SubjectDistanceRange";
				break;
			case 0xA420:
				name = "ImageUniqueID";
				break;
			}
			return name + ": " + toString(data);
		}

		private String toString(Object[] array) {
			if (array.length == 0) {
				return "[]";
			} else if (array.length == 1) {
				return "" + array[0];
			} else {
				StringBuffer result = new StringBuffer("[");
				for (int i = 0; i < array.length; i++) {
					if (i > 0)
						result.append(", ");
					result.append("" + array[i]);
				}
				result.append("]");
				return result.toString();
			}
		}
	}

	static class Fraction {
		int num;
		int den;

		Fraction(int num, int den) {
			this.num = num;
			this.den = den;
		}

		public String toFraction() {
			if (num == 0) {
				return "0";
			}
			int gcd = gcd(num, den);
			return (num / gcd) + "/" + (den / gcd);
		}

		private int gcd(int a, int b) {
			if (b == 0) {
				return a;
			} else {
				return gcd(b, a % b);
			}
		}

		public Double toDouble() {
			return new Double((double) num / (double) den);
		}

		public String toString() {
			return num + "/" + den;
		}
	}

	public Exif(URI uri, InputStream is) throws IOException {
		this.uri = uri;
		this.name = getName(uri);
		
		read(is, false);
	}

	private String getName(URI uri) {
		String result = uri.getPath();
		int indexOfSlash = result.lastIndexOf('/');
		if (indexOfSlash != -1) {
			result = result.substring(indexOfSlash + 1);
		}
		return result;
	}

	public Exif(String name, InputStream is) throws IOException {
		this.name = name;
		read(is, false);
	}
	
	public Exif(String name, InputStream is, boolean storeIFDs)
			throws IOException {
		this.name = name;
		read(is, storeIFDs);
	}

	private void read(InputStream is, boolean storeIFDs) throws IOException {
		try {
			int b;
			if ((b = is.read()) != 0xff)
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 0xd8)
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 0xff)
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 0xe1)
				throw new IllegalArgumentException("Not Exif");
			int lengthH = (is.read() & 0xff);
			if (lengthH == -1)
				throw new IllegalArgumentException("Unexpected EOF");
			int lengthL = (is.read() & 0xff);
			if (lengthL == -1)
				throw new IllegalArgumentException("Unexpected EOF");
			int length = (lengthH << 8) + lengthL;
			if ((b = is.read()) != 'E')
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 'x')
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 'i')
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 'f')
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 0x00)
				throw new IllegalArgumentException("Not Exif");
			if ((b = is.read()) != 0x00)
				throw new IllegalArgumentException("Not Exif");
			byte[] tiff_data = readBytes(is, length - 8);
			char endian = (char) tiff_data[0]; // 'I' for Intel or 'M' for
			// Motorola
			int ifd_offset = readInt(tiff_data, 4, 4, endian);
			List<IFD> ifds = new ArrayList<IFD>();
			while (ifd_offset != 0) {
				IFD ifd = new IFD();
				ifd_offset = readIFD(ifd, tiff_data, ifd_offset, endian);
				for (Entry entry : ifd.entries) {
					entry.applyValue(this, false);
					if ((entry.tag == 0x8769 || entry.tag == 0x8825) && entry.data[0] instanceof IFD) {
						IFD subIFD = (IFD) entry.data[0];
						for (Entry subEntry : subIFD.entries) {
							subEntry.applyValue(this, entry.tag == 0x8825);
						}
					}
				}
				ifds.add(ifd);
			}
			if (storeIFDs) {
				this.ifds = ifds.toArray(new IFD[ifds.size()]);
			}
		} finally {
			is.close();
		}
	}

	private int readIFD(IFD ifd, byte[] data, int offset, char endian) {
		int numEntries = readInt(data, offset, 2, endian);
		offset += 2;
		ifd.entries = new Entry[numEntries];
		for (int i = 0; i < numEntries; i++) {
			Entry entry = readEntry(data, offset, endian);
			offset += 12;
			// System.out.println(entry.toString());
			ifd.entries[i] = entry;
			if (entry.tag == 0x8769) {
				int subOffset = ((Integer) entry.data[0]).intValue();
				if (subOffset < data.length) {
					IFD subIFD = new IFD();
					readIFD(subIFD, data, subOffset, endian);
					entry.data[0] = subIFD;
				}
			} else if (entry.tag == 0x8825) {
				int subOffset = ((Integer) entry.data[0]).intValue();
				if (subOffset < data.length) {
					IFD subIFD = new IFD();
					readIFD(subIFD, data, subOffset, endian);
					entry.data[0] = subIFD;
				}
			}
		}
		return 0; // readInt(data, offset, 4, endian);
	}

	private Entry readEntry(byte[] data, int offset, char endian) {
		int tag = readInt(data, offset, 2, endian);
		int format = readInt(data, offset + 2, 2, endian);
		int numComponents = readInt(data, offset + 4, 4, endian);
		Object[] values = new Object[numComponents];
		int bytesPerComponent = new int[] { -1, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8,
				4, 8 }[format];
		if (numComponents * bytesPerComponent > 4) {
			offset = readInt(data, offset + 8, 4, endian);
		} else {
			offset = offset + 8;
		}
		if (format == 7) {
			values = new Object[] { null };
		} else if (format == 2) {
			values = new Object[] { new String(data, offset, numComponents - 1) };
		} else {
			for (int i = 0; i < numComponents; i++) {
				switch (format) {
				case 1:
					values[i] = new Byte(data[offset]);
					break;
				case 3:
					values[i] = new Integer(readInt(data, offset, 2, endian));
					break;
				case 4:
					values[i] = new Integer(readInt(data, offset, 4, endian));
					break;
				case 5:
					values[i] = new Fraction(readInt(data, offset, 4, endian),
							readInt(data, offset + 4, 4, endian));
					break;
				case 6:
					values[i] = new Byte(data[offset]);
					break;
				case 7:
					values[i] = new Byte(data[offset]);
					break;
				case 8:
					values[i] = new Integer(readInt(data, offset, 2, endian));
					break;
				case 9:
					values[i] = new Integer(readInt(data, offset, 4, endian));
					break;
				case 10:
					values[i] = new Fraction(readInt(data, offset, 4, endian),
							readInt(data, offset + 4, 4, endian));
					break;
				case 11:
					values[i] = new Float(Float.intBitsToFloat(readInt(data,
							offset, 4, endian)));
					break;
				case 12:
					values[i] = new Float(Float.intBitsToFloat(readInt(data,
							offset, 8, endian)));
					break;
				default:
					throw new RuntimeException("unexpected case");
				}
				offset += bytesPerComponent;
			}
		}
		return new Entry(tag, format, values);
	}

	private int readInt(byte[] data, int offset, int length, char endian) {
		if (endian == 'M') {
			int result = 0;
			for (int i = 0; i < length; i++) {
				result = result << 8;
				result = result | (data[offset + i] & 0xff);
			}
			return result;
		} else {
			int result = 0;
			for (int i = length - 1; i >= 0; i--) {
				result = result << 8;
				result = result | (data[offset + i] & 0xff);
			}
			return result;
		}
	}

	private byte[] readBytes(InputStream is, int n) throws IOException {
		byte[] result = new byte[n];
		int read;
		int index = 0;
		while ((read = is.read(result, index, n)) > 0) {
			n -= read;
			index += read;
		}
		if (n > 0) {
			throw new IOException("Could only read " + read
					+ " bytes but expected " + n + " more.");
		}
		return result;
	}

	public static void main(String... args) throws FileNotFoundException,
			IOException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		String filename = args[0];
		File file = new File(filename);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			List<Exif> exifs = new ArrayList<Exif>();
			for (int i = 0; i < files.length; i++) {
				try {
					exifs.add(new Exif(files[i].getName(), new FileInputStream(
							files[i])));
				} catch (IllegalArgumentException ex) {
					System.out.println("skipping " + files[i].getName());
				}
			}
			displayExifs(exifs.toArray(new Exif[exifs.size()]));
			return;
		}
		Exif exif = new Exif(filename, new FileInputStream(filename), true);
		exif.printStuff();
	}

	private static void displayExifs(Exif[] exifs) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columns = { "Name", "Make", "Model", "Orientation",
				"Software", "Timestamp", "Exposure", "Iso", "Aperture",
				"ExposureComp", "Flash", "Width", "Height", "FocalLength",
				"WhiteBalance", "LightSource", "ExposureProgram" };
		Method[] columnMethods = new Method[columns.length];

		for (int i = 0; i < columns.length; i++) {
			new TableColumn(table, SWT.NONE).setText(columns[i]);
			columnMethods[i] = Exif.class.getDeclaredMethod("get" + columns[i],
					new Class<?>[0]);
		}

		for (int i = 0; i < exifs.length; i++) {
			Exif exif = exifs[i];
			TableItem item = new TableItem(table, SWT.NONE);
			for (int j = 0; j < columnMethods.length; j++) {
				item.setText(j, "" + columnMethods[j].invoke(exif));
			}
		}

		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).pack();
		}

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void printStuff() {
		for (IFD ifd : ifds) {
			System.out.println("---IFD---");
			for (Entry entry : ifd.entries) {
				System.out.println(entry);
				if ((entry.tag == 0x8769 || entry.tag == 0x8825) && entry.data[0] instanceof IFD) {
					System.out.println("---Sub-IFD---");
					IFD subIFD = (IFD) entry.data[0];
					for (Entry subEntry : subIFD.entries) {
						System.out.println(subEntry);
					}
					System.out.println("---End-Sub---");
				} 
			}
		}
	}
}
