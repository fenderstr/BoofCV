/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
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

package boofcv.alg.feature.detect.intensity;

import boofcv.alg.InputSanityCheck;
import boofcv.alg.feature.detect.intensity.impl.ImplMedianCornerIntensity;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;

/**
 * <p>
 * Corner detector based on median filter.  First a median filter is run on the input image then the difference
 * between the input image and the median image is computed.  Corners are points of high intensity in the difference
 * image.
 * </p>
 *
 * @author Peter Abeles
 */
public class MedianCornerIntensity {

	public static void process(ImageFloat32 intensity , ImageFloat32 originalImage, ImageFloat32 medianImage)
	{
		InputSanityCheck.checkSameShape(intensity,originalImage,medianImage);

		ImplMedianCornerIntensity.process(intensity,originalImage,medianImage);
	}

	public static void process(ImageFloat32 intensity , ImageUInt8 originalImage, ImageUInt8 medianImage)
	{
		InputSanityCheck.checkSameShape(intensity,originalImage,medianImage);

		ImplMedianCornerIntensity.process(intensity,originalImage,medianImage);
	}
}
