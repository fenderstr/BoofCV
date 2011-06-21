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

package gecv.alg.wavelet.impl;

import gecv.alg.misc.ImageTestingOps;
import gecv.alg.wavelet.FactoryWaveletDaub;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt32;
import gecv.struct.image.ImageUInt8;
import gecv.struct.wavelet.WaveletDesc_F32;
import gecv.struct.wavelet.WaveletDesc_I32;
import gecv.testing.GecvTesting;
import org.junit.Test;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class TestImplWaveletTransformNaive {

	Random rand = new Random(234);
	int width = 20;
	int height = 30;

	WaveletDesc_F32 forward_F32 = FactoryWaveletDaub.standard_F32(4);
	WaveletDesc_F32 reverse_F32 = FactoryWaveletDaub.standard_F32(4);

	WaveletDesc_I32 forward_I32 = FactoryWaveletDaub.biorthogonal_I32(5);
	WaveletDesc_I32 reverse_I32 = FactoryWaveletDaub.biorthogonalInv_I32(5);

	/**
	 * See if it handles an image with an odd number of pixels
	 */
	@Test
	public void oddImage_F32() {
		testEncodeDecode_F32(width-1,height-1);
	}

	/**
	 * See if it handles an image with an even number of pixels
	 */
	@Test
	public void evenImage_F32() {
		testEncodeDecode_F32(width,height);
	}

		/**
	 * See if it handles an image with an odd number of pixels
	 */
	@Test
	public void oddImage_I32() {
		testEncodeDecode_I(width-1,height-1);
	}

	/**
	 * See if it handles an image with an even number of pixels
	 */
	@Test
	public void evenImage_I32() {
		testEncodeDecode_I(width,height);
	}

	private void testEncodeDecode_F32( int widthOrig , int heightOrig ) {
		ImageFloat32 orig = new ImageFloat32(widthOrig, heightOrig);
		ImageTestingOps.randomize(orig,rand,0,30);

		ImageFloat32 transformed = new ImageFloat32(width,height);
		ImageFloat32 reconstructed = new ImageFloat32(widthOrig, heightOrig);

		GecvTesting.checkSubImage(this,"checkTransforms_F32",true,
				orig, transformed, reconstructed );
	}

	public void checkTransforms_F32(ImageFloat32 orig,
									ImageFloat32 transformed,
									ImageFloat32 reconstructed ) {
		// Test horizontal transformation
		ImplWaveletTransformNaive.horizontal(forward_F32,orig,transformed);
		ImplWaveletTransformNaive.horizontalInverse(reverse_F32,transformed,reconstructed);
		GecvTesting.assertEquals(orig,reconstructed,0,1e-2f);

		// Test vertical transformation
		ImplWaveletTransformNaive.vertical(forward_F32,orig,transformed);
		ImplWaveletTransformNaive.verticalInverse(reverse_F32,transformed,reconstructed);
		GecvTesting.assertEquals(orig,reconstructed,0,1e-2f);
	}

	private void testEncodeDecode_I( int widthOrig , int heightOrig ) {
		ImageUInt8 orig = new ImageUInt8(widthOrig, heightOrig);
		ImageTestingOps.randomize(orig,rand,0,10);

		ImageSInt32 transformed = new ImageSInt32(width,height);
		ImageUInt8 reconstructed = new ImageUInt8(widthOrig, heightOrig);

		GecvTesting.checkSubImage(this,"checkTransforms_I",true,
				orig, transformed, reconstructed );
	}

	public void checkTransforms_I(ImageUInt8 orig,
								  ImageSInt32 transformed,
								  ImageUInt8 reconstructed ) {
		// Test horizontal transformation
		ImplWaveletTransformNaive.horizontal( forward_I32,orig,transformed);
		ImplWaveletTransformNaive.horizontalInverse(reverse_I32,transformed,reconstructed);

//		GecvTesting.printDiff(orig,reconstructed);
		GecvTesting.assertEquals(orig,reconstructed,1);

		// Test vertical transformation
		ImplWaveletTransformNaive.vertical( forward_I32,orig,transformed);
		ImplWaveletTransformNaive.verticalInverse(reverse_I32,transformed,reconstructed);
		GecvTesting.assertEquals(orig,reconstructed,0);
	}
}