/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.filter.convolve;

import gecv.core.image.GeneralizedImageOps;
import gecv.core.image.border.*;
import gecv.struct.convolve.Kernel1D_F32;
import gecv.struct.convolve.Kernel1D_I32;
import gecv.struct.convolve.Kernel2D_F32;
import gecv.struct.convolve.Kernel2D_I32;
import gecv.struct.image.*;
import gecv.testing.GecvTesting;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestConvolveWithBorderSparse {

	Random rand = new Random(234);
	int width = 20;
	int height = 30;
	int numExpected = 2;

	@Test
	public void testHorizontal() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		int numFound = 0;

		Method methods[] = ConvolveWithBorderSparse.class.getMethods();

		for( Method m : methods ) {
			if( !m.getName().equals("horizontal"))
				continue;

			testMethod(m);
			numFound++;
		}

		assertEquals(numExpected,numFound);
	}

	@Test
	public void testVertical() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		int numFound = 0;

		Method methods[] = ConvolveWithBorderSparse.class.getMethods();

		for( Method m : methods ) {
			if( !m.getName().equals("vertical"))
				continue;

			testMethod(m);
			numFound++;
		}

		assertEquals(numExpected,numFound);
	}

	@Test
	public void testConvolve() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		int numFound = 0;

		Method methods[] = ConvolveWithBorderSparse.class.getMethods();

		for( Method m : methods ) {
			if( !m.getName().equals("convolve"))
				continue;

			testMethod(m);
			numFound++;
		}

		assertEquals(numExpected,numFound);
	}

	public void testMethod( Method m ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		Class<?> kernelType = m.getParameterTypes()[0];
		Class<?> borderType = m.getParameterTypes()[1];
		Class<?> inputType = borderToInputType(borderType);
		Class<?> outputType = borderToOutputType(borderType);

		ImageBase input = GeneralizedImageOps.createImage(inputType,width,height);
		GeneralizedImageOps.randomize(input,rand,0,20);

		ImageBase expected = GeneralizedImageOps.createImage(outputType,width,height);
		Object kernel = createKernel(kernelType,2);
		ImageBorder border = createBorder(borderType);

		Method checkM = GecvTesting.findMethod(ConvolveWithBorder.class,m.getName(),kernelType,inputType,outputType,borderType);
		checkM.invoke(null,kernel,input,expected,border);

		Number v = (Number)m.invoke(null,kernel,border,3,6);

		double expectedValue = GeneralizedImageOps.get(expected,3,6);

		assertEquals(expectedValue,v.doubleValue(),1e-3);
	}

	protected static Object createKernel(Class<?> kernelType , int kernelRadius ) {
		Object kernel;
		if (Kernel1D_F32.class == kernelType) {
			kernel = KernelFactory.gaussian1D_F32(kernelRadius,true);
		} else if (Kernel1D_I32.class == kernelType) {
			kernel = KernelFactory.gaussian1D_I32(kernelRadius);
		} else if (Kernel2D_F32.class == kernelType) {
			kernel = KernelFactory.gaussian2D_F32(1,kernelRadius,true);
		} else if (Kernel2D_I32.class == kernelType) {
			kernel = KernelFactory.gaussian2D_I32(1,kernelRadius);
		} else {
			throw new RuntimeException("Unknown kernel type");
		}
		return kernel;
	}

	protected static ImageBorder createBorder(Class<?> borderType ) {
		ImageBorder ret;
		if (ImageBorder_F32.class == borderType) {
			ret = FactoryImageBorder.general(ImageFloat32.class,BorderType.REFLECT);
		} else if (ImageBorder_F64.class == borderType) {
			ret = FactoryImageBorder.general(ImageFloat64.class,BorderType.REFLECT);
		} else if (ImageBorder_I32.class == borderType) {
			ret =FactoryImageBorder.general(ImageSInt32.class,BorderType.REFLECT);
		} else if (ImageBorder_I64.class == borderType) {
			ret = FactoryImageBorder.general(ImageSInt64.class,BorderType.REFLECT);
		} else {
			throw new RuntimeException("Unknown kernel type");
		}
		return ret;
	}

	protected static Class<?> borderToInputType(Class<?> borderType ) {
		if (ImageBorder_F32.class == borderType) {
			return ImageFloat32.class;
		} else if (ImageBorder_F64.class == borderType) {
			return ImageFloat64.class;
		} else if (ImageBorder_I32.class == borderType) {
			return ImageUInt8.class;
		} else if (ImageBorder_I64.class == borderType) {
			return ImageSInt64.class;
		} else {
			throw new RuntimeException("Unknown border type");
		}
	}

	protected static Class<?> borderToOutputType(Class<?> borderType ) {
		if (ImageBorder_F32.class == borderType) {
			return ImageFloat32.class;
		} else if (ImageBorder_F64.class == borderType) {
			return ImageFloat64.class;
		} else if (ImageBorder_I32.class == borderType) {
			return ImageSInt16.class;
		} else if (ImageBorder_I64.class == borderType) {
			return ImageSInt64.class;
		} else {
			throw new RuntimeException("Unknown border type");
		}
	}
}
