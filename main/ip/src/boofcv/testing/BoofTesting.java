/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.testing;

import boofcv.core.image.FactoryGImageSingleBand;
import boofcv.core.image.GImageSingleBand;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.*;
import sun.awt.image.ByteInterleavedRaster;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Functions to aid in unit testing code for correctly handling sub-images
 *
 * @author Peter Abeles
 */
// todo remove all comwapare with border functions and use sub-images instead
@SuppressWarnings({"unchecked"})
public class BoofTesting {

	public static <T> T convertToGenericType(Class<?> type) {
		if (type == ImageSInt8.class || type == ImageUInt8.class)
			return (T) ImageInt8.class;
		if (type == ImageSInt16.class || type == ImageUInt16.class)
			return (T) ImageInt16.class;
		return (T) type;
	}

	public static ImageTypeInfo convertToGenericType(ImageTypeInfo<?> type) {
		if (type.isInteger()) {
			if (type.getNumBits() == 8)
				return ImageTypeInfo.I8;
			else if (type.getNumBits() == 16)
				return ImageTypeInfo.I16;
		}

		return type;
	}

	/**
	 * If an image is to be created then the generic type can't be used a specific one needs to be.  An arbitrary
	 * specific image type is returned here.
	 */
	public static <T> T convertGenericToSpecificType(Class<?> type) {
		if (type == ImageInt8.class)
			return (T) ImageUInt8.class;
		if (type == ImageInt16.class)
			return (T) ImageSInt16.class;
		return (T) type;
	}

	/**
	 * <p>
	 * Returns an image which is a sub-image but contains the same values of the input image.  Use for
	 * testing compliance with sub-images.  The subimage is created by creating a larger image,
	 * copying over the input image into the inner portion, then creating a subimage of the copied part.
	 * </p>
	 */
	@SuppressWarnings({"unchecked"})
	public static <T extends ImageSingleBand> T createSubImageOf(T input) {
		// create the larger image
		T ret = (T) input._createNew(input.width + 10, input.height + 12);
		// create a sub-image of the inner portion
		ret = (T) ret.subimage(5, 7, input.width + 5, input.height + 7);
		// copy input image into the subimage
		ret.setTo(input);

		return ret;
	}

	/**
	 * Searches for functions that accept only images and makes sure they only accept
	 * images which have he same width and height.
	 *
	 * @param testClass Instance of the class being tested
	 */
	public static void checkImageDimensionValidation(Object testClass, int numFunctions) {
		int count = 0;
		Method methods[] = testClass.getClass().getMethods();

		for (Method m : methods) {
			// see if the inputs are all images
			if (!areAllInputsImages(m))
				continue;

			// test a positive case
			Class params[] = m.getParameterTypes();
			Object[] inputs = new Object[params.length];
			for (int i = 0; i < params.length; i++) {
				inputs[i] = GeneralizedImageOps.createSingleBand(params[i], 10, 20);
			}

			try {
				m.invoke(testClass, inputs);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}

			// test negative cases
			for (int target = 0; target < params.length; target++) {
				for (int i = 0; i < params.length; i++) {
					if (i != target)
						inputs[i] = GeneralizedImageOps.createSingleBand(params[i], 10, 20);
					else
						inputs[i] = GeneralizedImageOps.createSingleBand(params[i], 11, 22);
				}

				try {
					m.invoke(testClass, inputs);
					throw new RuntimeException("Expected an exception here");
				} catch (InvocationTargetException e) {
					if (e.getTargetException().getClass() != IllegalArgumentException.class)
						throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
			count++;
		}

		if (count != numFunctions)
			throw new RuntimeException("Unexpected number of functions");
	}

	private static boolean areAllInputsImages(Method m) {

		Class<?> params[] = m.getParameterTypes();

		if (params.length == 0)
			return false;

		for (Class<?> p : params) {
			if (!ImageSingleBand.class.isAssignableFrom(p)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Tests the specified function with the original image provided and with an equivalent
	 * sub-image.  The two results are then compared. The function being tested must only
	 * have one input parameter of type {@link boofcv.struct.image.ImageUInt8}.
	 *
	 * @param testClass   Instance of the class that contains the function being tested.
	 * @param function    The name of the function being tested.
	 * @param checkEquals Checks to see if the two images have been modified the same way on output
	 * @param inputParam  The original input parameters
	 */
	// TODO make sure pixels outside are not modified of sub-matrix
	// todo have the submatrices be from different shaped inputs
	@SuppressWarnings({"unchecked"})
	public static void checkSubImage(Object testClass,
									 String function,
									 boolean checkEquals,
									 Object... inputParam) {
		try {
			ImageBase[] larger = new ImageBase[inputParam.length];
			ImageBase[] subImg = new ImageBase[inputParam.length];
			Class<?> paramDesc[] = new Class<?>[inputParam.length];
			Object[] inputModified = new Object[inputParam.length];

			for (int i = 0; i < inputParam.length; i++) {
				if (ImageBase.class.isAssignableFrom(inputParam[i].getClass())) {
					ImageBase<?> img = (ImageBase<?>) inputParam[i];

					// copy the original image inside of a larger image
					larger[i] = img._createNew(img.getWidth() + 10, img.getHeight() + 12);
					// extract a sub-image and make it equivalent to the original image.
					subImg[i] = larger[i].subimage(5, 6, 5 + img.getWidth(), 6 + img.getHeight());
					subImg[i].setTo(img);

				}

				// the first time it is called use the original inputs
				inputModified[i] = inputParam[i];
				paramDesc[i] = inputParam[i].getClass();
			}


			// first try it with the original image
			Method m = findMethod(testClass.getClass(), function, paramDesc);

			m.invoke(testClass, inputModified);

			// now try it with the sub-image
			for (int i = 0; i < inputModified.length; i++) {
				if (subImg[i] != null)
					inputModified[i] = subImg[i];
			}
			m.invoke(testClass, inputModified);

			// the result should be the identical
			if (checkEquals) {
				for (int i = 0; i < inputParam.length; i++) {
					if (subImg[i] == null)
						continue;
					assertEquals((ImageBase)inputModified[i], subImg[i], 0);
				}
			}

		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Searches for a function which is a perfect match.  if none it exists it checks
	 * to see if any matches that could accept an input of the specified type.  If there
	 * is only one such match that is returned.
	 */
	public static Method findMethod(Class<?> type, String name, Class<?>... params) {
		Method methods[] = type.getMethods();

		List<Method> found = new ArrayList<Method>();
		for (Method m : methods) {
			if (m.getName().compareTo(name) != 0)
				continue;

			Class<?> a[] = m.getParameterTypes();
			if (a.length != params.length)
				continue;

			boolean match = true;
			for (int i = 0; i < a.length; i++) {
				if (a[i] != params[i]) {
					match = false;
					break;
				}
			}

			if (match) {
				// its a perfect match
				return m;
			}

			// see if it could be called with these parameters
			match = true;
			for (int i = 0; i < a.length; i++) {
				if (params[i] == a[i])
					continue;
				if (a[i].isPrimitive()) {
					if (a[i] == Byte.TYPE && params[i] == Byte.class)
						continue;
					if (a[i] == Short.TYPE && params[i] == Short.class)
						continue;
					if (a[i] == Integer.TYPE && params[i] == Integer.class)
						continue;
					if (a[i] == Long.TYPE && params[i] == Long.class)
						continue;
					if (a[i] == Float.TYPE && params[i] == Float.class)
						continue;
					if (a[i] == Double.TYPE && params[i] == Double.class)
						continue;
				}
				if (!a[i].isAssignableFrom(params[i])) {
					match = false;
					break;
				}
			}

			if (match) {
				found.add(m);
			}
		}

		if (found.size() == 1) {
			return found.get(0);
		}

		throw new RuntimeException("Couldn't find matching *public* function to " + name);
	}

	/**
	 * Searches for all functions with the specified name in the target class.  Once it finds
	 * that function it invokes the specified function in the owner class. That function must
	 * take in a Method as its one and only parameter.  The method will be one of the matching
	 * ones in the target class.
	 *
	 * @param owner
	 * @param ownerMethod
	 * @param target
	 * @param targetMethod
	 * @return The number of times 'targetMethod' was found and called.
	 */
	public static int findMethodThenCall(Object owner, String ownerMethod, Class target, String targetMethod) {
		int total = 0;
		Method[] list = target.getMethods();

		try {
			Method om = owner.getClass().getMethod(ownerMethod, Method.class);

			for (Method m : list) {
				if (!m.getName().equals(targetMethod))
					continue;

				om.invoke(owner, m);

				total++;
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return total;
	}

	public static void assertEquals(double a[], double b[], double tol) {
		for (int i = 0; i < a.length; i++) {
			double diff = Math.abs(a[i] - b[i]);
			if (diff > tol)
				throw new RuntimeException("Element " + i + " not equals. " + a[i] + " " + b[i]);
		}
	}

	public static void assertEquals(double a[], float b[], double tol) {
		for (int i = 0; i < a.length; i++) {
			double diff = Math.abs(a[i] - b[i]);
			if (diff > tol)
				throw new RuntimeException("Element " + i + " not equals. " + a[i] + " " + b[i]);
		}
	}

	public static void assertEquals(double a[], int b[]) {
		for (int i = 0; i < a.length; i++) {
			double diff = Math.abs((int) a[i] - b[i]);
			if (diff != 0)
				throw new RuntimeException("Element " + i + " not equals. " + a[i] + " " + b[i]);
		}
	}

	public static void assertEquals(float a[], float b[], float tol) {
		for (int i = 0; i < a.length; i++) {
			double diff = Math.abs(a[i] - b[i]);
			if (diff > tol)
				throw new RuntimeException("Element " + i + " not equals. " + a[i] + " " + b[i]);
		}
	}

	public static void assertEquals(ImageBase imgA, ImageBase imgB, double tol ) {

		// if no specialized check exists, use a slower generalized approach
		if( imgA instanceof ImageSingleBand ) {
			GImageSingleBand a = FactoryGImageSingleBand.wrap((ImageSingleBand)imgA);
			GImageSingleBand b = FactoryGImageSingleBand.wrap((ImageSingleBand)imgB);

			for( int y = 0; y < imgA.height; y++ ) {
				for( int x = 0; x < imgA.width; x++ ) {
					double valA = a.get(x,y).doubleValue();
					double valB = b.get(x,y).doubleValue();

					double difference = valA - valB;
					if( Math.abs(difference) > tol )
						throw new RuntimeException("Values not equal at ("+x+","+y+") "+valA+"  "+valB);
				}
			}
		} else if( imgA instanceof MultiSpectral ){
			MultiSpectral a = (MultiSpectral)imgA;
			MultiSpectral b = (MultiSpectral)imgB;

			if( a.getNumBands() != b.getNumBands() )
				throw new RuntimeException("Number of bands not equal");

			for( int band = 0; band < a.getNumBands(); band++ ) {
				assertEquals(a.getBand(band),b.getBand(band),tol );
			}
		} else if( imgA instanceof ImageInterleaved ) {
			ImageInterleavedInt8 a = (ImageInterleavedInt8)imgA;
			ImageInterleavedInt8 b = (ImageInterleavedInt8)imgB;

			if( a.getNumBands() != b.getNumBands() )
				throw new RuntimeException("Number of bands not equal");

			int numBands = a.getNumBands();

			for( int y = 0; y < imgA.height; y++ ) {
				for( int x = 0; x < imgA.width; x++ ) {
					for( int band = 0; band < numBands; band++ ) {
						int valA = a.getBand(x, y, band);
						int valB = b.getBand(x, y, band);

						double difference = valA - valB;
						if( Math.abs(difference) > tol )
							throw new RuntimeException("Values not equal at ("+x+","+y+") "+valA+"  "+valB);
					}
				}
			}

		} else {
			throw new RuntimeException("Unknown image type");
		}
	}

	public static void assertEqualsInner(ImageBase imgA, ImageBase imgB, double tol , int borderX , int borderY ,
										 boolean relative ) {

		// if no specialized check exists, use a slower generalized approach
		if( imgA instanceof ImageSingleBand ) {
			GImageSingleBand a = FactoryGImageSingleBand.wrap((ImageSingleBand)imgA);
			GImageSingleBand b = FactoryGImageSingleBand.wrap((ImageSingleBand)imgB);

			for( int y = borderY; y < imgA.height-borderY; y++ ) {
				for( int x = borderX; x < imgA.width-borderX; x++ ) {
					double valA = a.get(x,y).doubleValue();
					double valB = b.get(x,y).doubleValue();

					double error = Math.abs(valA - valB);
					if( relative ) {
						double denominator = Math.abs(valA) + Math.abs(valB);
						if( denominator == 0 )
							denominator = 1;
						error /= denominator;
					}
					if( error > tol )
						throw new RuntimeException("Values not equal at ("+x+","+y+") "+valA+"  "+valB);
				}
			}
		} else if( imgA instanceof MultiSpectral ){
			MultiSpectral a = (MultiSpectral)imgA;
			MultiSpectral b = (MultiSpectral)imgB;

			if( a.getNumBands() != b.getNumBands() )
				throw new RuntimeException("Number of bands not equal");

			for( int band = 0; band < a.getNumBands(); band++ ) {
				assertEqualsInner(a.getBand(band),b.getBand(band),tol,borderX,borderY,relative );
			}
		} else {
			throw new RuntimeException("Unknown image type");
		}
	}

	public static void assertEqualsRelative(ImageBase imgA, ImageBase imgB, double tolFrac ) {

		// if no specialized check exists, use a slower generalized approach
		if( imgA instanceof ImageSingleBand ) {
			GImageSingleBand a = FactoryGImageSingleBand.wrap((ImageSingleBand)imgA);
			GImageSingleBand b = FactoryGImageSingleBand.wrap((ImageSingleBand)imgB);

			for( int y = 0; y < imgA.height; y++ ) {
				for( int x = 0; x < imgA.width; x++ ) {
					double valA = a.get(x,y).doubleValue();
					double valB = b.get(x,y).doubleValue();

					double difference = valA - valB;
					double max = Math.max( Math.abs(valA), Math.abs(valB));
					if( max == 0 )
						max = 1;
					if( Math.abs(difference)/max > tolFrac )
						throw new RuntimeException("Values not equal at ("+x+","+y+") "+valA+"  "+valB);
				}
			}
		} else if( imgA instanceof MultiSpectral ){
			MultiSpectral a = (MultiSpectral)imgA;
			MultiSpectral b = (MultiSpectral)imgB;

			if( a.getNumBands() != b.getNumBands() )
				throw new RuntimeException("Number of bands not equal");

			for( int band = 0; band < a.getNumBands(); band++ ) {
				assertEqualsRelative(a.getBand(band),b.getBand(band),tolFrac );
			}
		} else {
			throw new RuntimeException("Unknown image type");
		}
	}

	/**
	 * Checks to see if only the image borders are equal to each other within tolerance
	 */
	public static void assertEqualsBorder(ImageSingleBand imgA, ImageSingleBand imgB, double tol, int borderX, int borderY) {
		if (imgA.getWidth() != imgB.getWidth())
			throw new RuntimeException("Widths are not equals");

		if (imgA.getHeight() != imgB.getHeight())
			throw new RuntimeException("Heights are not equals");

		GImageSingleBand a = FactoryGImageSingleBand.wrap(imgA);
		GImageSingleBand b = FactoryGImageSingleBand.wrap(imgB);

		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < borderX; x++) {
				compareValues(tol, a, b, x, y);
			}
			for (int x = imgA.getWidth() - borderX; x < imgA.getWidth(); x++) {
				compareValues(tol, a, b, x, y);
			}
		}

		for (int x = borderX; x < imgA.getWidth() - borderX; x++) {
			for (int y = 0; y < borderY; y++) {
				compareValues(tol, a, b, x, y);
			}
			for (int y = imgA.getHeight() - borderY; y < imgA.getHeight(); y++) {
				compareValues(tol, a, b, x, y);
			}
		}
	}

	private static void compareValues(double tol, GImageSingleBand a, GImageSingleBand b, int x, int y) {
		double normalizer = Math.abs(a.get(x, y).doubleValue()) + Math.abs(b.get(x, y).doubleValue());
		if (normalizer < 1.0) normalizer = 1.0;
		if (Math.abs(a.get(x, y).doubleValue() - b.get(x, y).doubleValue()) / normalizer > tol)
			throw new RuntimeException("values not equal at (" + x + " " + y + ") " + a.get(x, y) + "  " + b.get(x, y));
	}

	public static void checkEquals(BufferedImage imgA, ImageBase imgB, double tol) {
		if (ImageUInt8.class == imgB.getClass()) {
			checkEquals(imgA, (ImageUInt8) imgB);
		} else if (ImageFloat32.class == imgB.getClass()) {
			checkEquals(imgA, (ImageFloat32) imgB, (float) tol);
		} else if (ImageInterleavedInt8.class == imgB.getClass()) {
			checkEquals(imgA, (ImageInterleavedInt8) imgB);
		} else if (MultiSpectral.class == imgB.getClass()) {
			checkEquals(imgA, (MultiSpectral) imgB, (float) tol);
		}
	}

	/**
	 * Checks to see if the BufferedImage has the same intensity values as the ImageUInt8
	 *
	 * @param imgA BufferedImage
	 * @param imgB ImageUInt8
	 */
	public static void checkEquals(BufferedImage imgA, ImageUInt8 imgB) {

		if (imgA.getRaster() instanceof ByteInterleavedRaster &&
				imgA.getType() != BufferedImage.TYPE_BYTE_INDEXED) {
			ByteInterleavedRaster raster = (ByteInterleavedRaster) imgA.getRaster();

			if (raster.getNumBands() == 1) {
				int strideA = raster.getScanlineStride();
				int offsetA = raster.getDataOffset(0)-raster.getNumBands()+1;

				// handle a special case where the RGB conversion is screwed
				for (int i = 0; i < imgA.getHeight(); i++) {
					for (int j = 0; j < imgA.getWidth(); j++) {
						int valB = imgB.get(j, i);
						int valA = raster.getDataStorage()[ offsetA + i*strideA + j];
						if (!imgB.getTypeInfo().isSigned())
							valA &= 0xFF;

						if (valA != valB)
							throw new RuntimeException("Images are not equal: "+valA+" "+valB);
					}
				}
				return;
			}
		}

		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < imgA.getWidth(); x++) {
				int rgb = imgA.getRGB(x, y);

				int gray = (byte) ((((rgb >>> 16) & 0xFF) + ((rgb >>> 8) & 0xFF) + (rgb & 0xFF)) / 3);
				int grayB = imgB.get(x, y);
				if (!imgB.getTypeInfo().isSigned())
					gray &= 0xFF;

				if (Math.abs(gray - grayB) != 0) {
					throw new RuntimeException("images are not equal: (" + x + " , " + y + ") A = " + gray + " B = " + grayB);
				}
			}
		}
	}

	/**
	 * Checks to see if the BufferedImage has the same intensity values as the ImageUInt8
	 *
	 * @param imgA BufferedImage
	 * @param imgB ImageUInt8
	 */
	public static void checkEquals(BufferedImage imgA, ImageFloat32 imgB, float tol) {

		if (imgA.getRaster() instanceof ByteInterleavedRaster &&
				imgA.getType() != BufferedImage.TYPE_BYTE_INDEXED ) {
			ByteInterleavedRaster raster = (ByteInterleavedRaster) imgA.getRaster();

			if (raster.getNumBands() == 1) {
				int strideA = raster.getScanlineStride();
				int offsetA = raster.getDataOffset(0)-raster.getNumBands()+1;

				// handle a special case where the RGB conversion is screwed
				for (int i = 0; i < imgA.getHeight(); i++) {
					for (int j = 0; j < imgA.getWidth(); j++) {
						float valB = imgB.get(j, i);
						int valA = raster.getDataStorage()[offsetA + i * strideA + j];
						valA &= 0xFF;

						if (Math.abs(valA - valB) > tol)
							throw new RuntimeException("Images are not equal: A = " + valA + " B = " + valB);
					}
				}
				return;
			}
		}

		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < imgA.getWidth(); x++) {
				int rgb = imgA.getRGB(x, y);

				float gray = (((rgb >>> 16) & 0xFF) + ((rgb >>> 8) & 0xFF) + (rgb & 0xFF)) / 3.0f;
				float grayB = imgB.get(x, y);

				if (Math.abs(gray - grayB) > tol) {
					throw new RuntimeException("images are not equal: A = " + gray + " B = " + grayB);
				}
			}
		}
	}

	/**
	 * Checks to see if the BufferedImage has the same intensity values as the ImageUInt8
	 *
	 * @param imgA BufferedImage
	 * @param imgB ImageUInt8
	 */
	public static void checkEquals(BufferedImage imgA, ImageInterleavedInt8 imgB) {

		if (imgA.getRaster() instanceof ByteInterleavedRaster) {
			ByteInterleavedRaster raster = (ByteInterleavedRaster) imgA.getRaster();

			if (raster.getNumBands() == 1) {
				// handle a special case where the RGB conversion is screwed
				for (int i = 0; i < imgA.getHeight(); i++) {
					for (int j = 0; j < imgA.getWidth(); j++) {
						int valB = imgB.getBand(j, i, 0);
						int valA = raster.getDataStorage()[i * imgA.getWidth() + j];

						if (valA != valB)
							throw new RuntimeException("Images are not equal");
					}
				}
				return;
			}
		}

		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < imgA.getWidth(); x++) {
				int rgb = imgA.getRGB(x, y);

				int r = (rgb >>> 16) & 0xFF;
				int g = (rgb >>> 8) & 0xFF;
				int b = rgb & 0xFF;

				if (Math.abs(b - imgB.getBand(x, y, 0) & 0xFF) != 0)
					throw new RuntimeException("images are not equal: ");
				if (Math.abs(g - imgB.getBand(x, y, 1) & 0xFF) != 0)
					throw new RuntimeException("images are not equal: ");
				if (Math.abs(r - imgB.getBand(x, y, 2) & 0xFF) != 0)
					throw new RuntimeException("images are not equal: ");
			}
		}
	}

	public static void checkEquals(BufferedImage imgA, MultiSpectral imgB, float tol) {

		if (imgA.getRaster() instanceof ByteInterleavedRaster &&
				imgA.getType() != BufferedImage.TYPE_BYTE_INDEXED ) {
			ByteInterleavedRaster raster = (ByteInterleavedRaster) imgA.getRaster();

			if (raster.getNumBands() == 1) {
				GImageSingleBand band = FactoryGImageSingleBand.wrap(imgB.getBand(0));

				int strideA = raster.getScanlineStride();
				int offsetA = raster.getDataOffset(0)-raster.getPixelStride()+1;

				// handle a special case where the RGB conversion is screwed
				for (int i = 0; i < imgA.getHeight(); i++) {
					for (int j = 0; j < imgA.getWidth(); j++) {
						double valB = band.get(j, i).doubleValue();
						int valA = raster.getDataStorage()[offsetA + i * strideA + j];
						valA &= 0xFF;

						if (Math.abs(valA - valB) > tol)
							throw new RuntimeException("Images are not equal: A = " + valA + " B = " + valB);
					}
				}
				return;
			}
		}

		GImageSingleBand band1, band2, band3;

		if (imgA.getType() == BufferedImage.TYPE_3BYTE_BGR ||
				imgA.getType() == BufferedImage.TYPE_INT_BGR ) {
			band1 = FactoryGImageSingleBand.wrap(imgB.getBand(2));
			band2 = FactoryGImageSingleBand.wrap(imgB.getBand(1));
			band3 = FactoryGImageSingleBand.wrap(imgB.getBand(0));
		} else if (imgA.getType() == BufferedImage.TYPE_INT_RGB ||
				imgA.getType() == BufferedImage.TYPE_BYTE_INDEXED ) {
			band1 = FactoryGImageSingleBand.wrap(imgB.getBand(0));
			band2 = FactoryGImageSingleBand.wrap(imgB.getBand(1));
			band3 = FactoryGImageSingleBand.wrap(imgB.getBand(2));
		} else if (imgA.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
			band1 = FactoryGImageSingleBand.wrap(imgB.getBand(3));
			band2 = FactoryGImageSingleBand.wrap(imgB.getBand(2));
			band3 = FactoryGImageSingleBand.wrap(imgB.getBand(1));
		} else if (imgA.getType() == BufferedImage.TYPE_INT_ARGB) {
			band1 = FactoryGImageSingleBand.wrap(imgB.getBand(1));
			band2 = FactoryGImageSingleBand.wrap(imgB.getBand(2));
			band3 = FactoryGImageSingleBand.wrap(imgB.getBand(3));
		} else {
			throw new RuntimeException("Handle this case");
		}

		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < imgA.getWidth(); x++) {
				int rgb = imgA.getRGB(x, y);

				int val1 = ((rgb >>> 16) & 0xFF);
				int val2 = ((rgb >>> 8) & 0xFF);
				int val3 = (rgb & 0xFF);

				float mult1 = band1.get(x, y).floatValue();
				float mult2 = band2.get(x, y).floatValue();
				float mult3 = band3.get(x, y).floatValue();

				if (Math.abs(val1 - mult1) > tol) {
					throw new RuntimeException("images are not equal: A = " + val1 + " B = " + mult1);
				}
				if (Math.abs(val2 - mult2) > tol) {
					throw new RuntimeException("images are not equal: A = " + val2 + " B = " + mult2);
				}
				if (Math.abs(val3 - mult3) > tol) {
					throw new RuntimeException("images are not equal: A = " + val3 + " B = " + mult3);
				}
			}
		}
	}


	public static void checkBorderZero(ImageSingleBand outputImage, int border) {
		GImageSingleBand img = FactoryGImageSingleBand.wrap(outputImage);

		for (int y = 0; y < img.getHeight(); y++) {
			if (y >= border && y < img.getHeight() - border)
				continue;

			for (int x = 0; x < img.getWidth(); x++) {
				if (x >= border && x < img.getWidth() - border)
					continue;
				if (img.get(x, y).intValue() != 0)
					throw new RuntimeException("The border is not zero");
			}
		}
	}

	public static void printDiff(ImageSingleBand imgA, ImageSingleBand imgB) {

		GImageSingleBand a = FactoryGImageSingleBand.wrap(imgA);
		GImageSingleBand b = FactoryGImageSingleBand.wrap(imgB);

		System.out.println("------- Difference -----------");
		for (int y = 0; y < imgA.getHeight(); y++) {
			for (int x = 0; x < imgA.getWidth(); x++) {
				double diff = Math.abs(a.get(x, y).doubleValue() - b.get(x, y).doubleValue());
				System.out.printf("%2d ", (int) diff);
			}
			System.out.println();
		}
	}
}
