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

package boofcv.alg.interpolate.impl;

import boofcv.alg.interpolate.BilinearPixel;
import boofcv.struct.image.ImageFloat32;


/**
 * <p>
 * Implementation of {@link BilinearPixel} for a specific image type.
 * </p>
 *
 * <p>
 * NOTE: This code was automatically generated using {@link GenerateImplBilinearPixel}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplBilinearPixel_F32 extends BilinearPixel<ImageFloat32> {

	public ImplBilinearPixel_F32() {
	}

	public ImplBilinearPixel_F32(ImageFloat32 orig) {
		setImage(orig);
	}


	@Override
	public float get_unsafe(float x, float y) {
		int xt = (int) x;
		int yt = (int) y;
		float ax = x - xt;
		float ay = y - yt;

		int index = orig.startIndex + yt * stride + xt;

		int dx = xt == width - 1 ? 0 : 1;
		int dy = yt == height - 1 ? 0 : stride;

		float[] data = orig.data;

		float val = (1.0f - ax) * (1.0f - ay) * (data[index] ); // (x,y)
		val += ax * (1.0f - ay) * (data[index + dx] ); // (x+1,y)
		val += ax * ay * (data[index + dx + dy] ); // (x+1,y+1)
		val += (1.0f - ax) * ay * (data[index + dy] ); // (x,y+1)

		return val;
	}

	@Override
	public float get(float x, float y) {
		int xt = (int) x;
		int yt = (int) y;

		if (xt < 0 || yt < 0 || xt >= width || yt >= height)
			throw new IllegalArgumentException("Point is outside of the image");

		float ax = x - xt;
		float ay = y - yt;

		int index = orig.startIndex + yt * stride + xt;

		// throw allows borders to be interpolated gracefully by double counting appropriate pixels
		int dx = xt == width - 1 ? 0 : 1;
		int dy = yt == height - 1 ? 0 : stride;

		float[] data = orig.data;

		float val = (1.0f - ax) * (1.0f - ay) * (data[index] ); // (x,y)
		val += ax * (1.0f - ay) * (data[index + dx] ); // (x+1,y)
		val += ax * ay * (data[index + dx + dy] ); // (x+1,y+1)
		val += (1.0f - ax) * ay * (data[index + dy] ); // (x,y+1)

		return val;
	}

}
