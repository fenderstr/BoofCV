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

import gecv.alg.filter.convolve.border.CompareImageBorder;
import gecv.core.image.GeneralizedImageOps;
import gecv.struct.image.ImageBase;
import gecv.struct.image.generalized.FactorySingleBandImage;
import gecv.struct.image.generalized.SingleBandImage;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * Compares convolve extended against an inner convolution performed against a larger image
 * which is an extension of the original image.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class TestConvolveExtended extends CompareImageBorder {
	public TestConvolveExtended() {
		super(ConvolveExtended.class);
	}

	@Test
	public void compareToNoBorder() {
		performTests(12);
	}

	/**
	 * Fillers the border in the larger image with an extended version of the smaller image.  A duplicate
	 * of the smaller image is contained in the center of the larger image.
	 */
	protected void fillTestImage(ImageBase smaller, ImageBase larger) {
		stripBorder(larger).setTo(smaller);

		SingleBandImage s = FactorySingleBandImage.wrap(smaller);
		SingleBandImage l = FactorySingleBandImage.wrap(larger);

		for( int y = 0; y < larger.height; y++ ) {
			for( int x = 0; x < larger.width; x++ ) {
				int sx,sy;

				if( x < kernelRadius )
					sx = 0;
				else if( x >= larger.width - kernelRadius )
					sx = smaller.width-1;
				else
					sx = x - kernelRadius;

				if( y < kernelRadius )
					sy = 0;
				else if( y >= larger.height - kernelRadius )
					sy = smaller.height-1;
				else
					sy = y - kernelRadius;

				l.set(x,y,s.get(sx,sy));
			}
		}

//		ShowImages.showWindow((ImageFloat32)larger,"large",true);
	}

		@Override
	protected boolean isEquivalent(Method candidate, Method evaluation) {
		if( evaluation.getName().compareTo(candidate.getName()) != 0 )
			return false;

		Class<?> e[] = evaluation.getParameterTypes();
		Class<?> c[] = candidate.getParameterTypes();

		if( e[0] != c[0] )
			return false;
		if( !e[1].isAssignableFrom(c[1]) )
			return false;
		if( !e[2].isAssignableFrom(c[2]) )
			return false;

		return true;
	}

	@Override
	protected Object[][] createInputParam(Method candidate, Method validation) {
		Class<?> paramTypes[] = candidate.getParameterTypes();

		Object kernel = createKernel(paramTypes[0]);

		ImageBase src = ConvolutionTestHelper.createImage(validation.getParameterTypes()[1], width, height);
		GeneralizedImageOps.randomize(src, 0, 5, rand);
		ImageBase dst = ConvolutionTestHelper.createImage(validation.getParameterTypes()[2], width, height);

		Object[][] ret = new Object[1][paramTypes.length];
		ret[0][0] = kernel;
		ret[0][1] = src;
		ret[0][2] = dst;

		return ret;
	}

	@Override
	protected Object[] reformatForValidation(Method m, Object[] targetParam) {
		Object[] ret;
		if( m.getName().contains("convolve")) {
			ret =  new Object[]{targetParam[0],targetParam[1],targetParam[2]};
		} else {
			ret = new Object[]{targetParam[0],targetParam[1],targetParam[2],false};
		}

		ImageBase inputImage = (ImageBase)targetParam[1];

		ret[1] = inputImage._createNew(width+kernelRadius*2,height+kernelRadius*2);
		ret[2] = ((ImageBase)targetParam[2])._createNew(width+kernelRadius*2,height+kernelRadius*2);

		fillTestImage(inputImage,(ImageBase)ret[1]);

		return ret;
	}

	@Override
	protected void compareResults(Object targetResult, Object[] targetParam, Object validationResult, Object[] validationParam) {
		ImageBase targetOut = (ImageBase)targetParam[2];
		ImageBase validationOut = (ImageBase)validationParam[2];

		// remove the border
		validationOut = stripBorder(validationOut);

		SingleBandImage t = FactorySingleBandImage.wrap(targetOut);
		SingleBandImage v = FactorySingleBandImage.wrap(validationOut);

		for( int y = 0; y < targetOut.height; y++ ) {
			for( int x = 0; x < targetOut.width; x++ ) {
				assertEquals("Failed at "+x+" "+y,v.get(x,y).doubleValue(),t.get(x,y).doubleValue(),1e-4f);
			}
		}
	}
}
