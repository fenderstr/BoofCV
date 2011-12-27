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

package boofcv.alg.feature.orientation.impl;

import boofcv.alg.feature.orientation.OrientationHistogram;
import boofcv.struct.image.ImageSInt32;


/**
 * <p>
 * Implementation of {@link OrientationHistogram} for a specific image type.
 * </p>
 *
 * <p>
 * WARNING: Do not modify.  Automatically generated by {@link GenerateImplOrientationHistogram}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ImplOrientationHistogram_S32 extends OrientationHistogram<ImageSInt32> {

	public ImplOrientationHistogram_S32(int numAngles , boolean isWeighted ) {
		super(numAngles,isWeighted);
	}

	@Override
	public Class<ImageSInt32> getImageType() {
		return ImageSInt32.class;
	}

	@Override
	protected void computeUnweightedScore() {
		// compute the score for each angle in the histogram
		for( int y = rect.y0; y < rect.y1; y++ ) {
			int indexX = derivX.startIndex + derivX.stride*y + rect.x0;
			int indexY = derivY.startIndex + derivY.stride*y + rect.x0;

			for( int x = rect.x0; x < rect.x1; x++ , indexX++ , indexY++ ) {
				int dx = derivX.data[indexX];
				int dy = derivY.data[indexY];

				double angle = Math.atan2(dy,dx);
				// compute which discretized angle it is
				int discreteAngle = (int)((angle + angleRound)/angleDiv) % numAngles;
				// sum up the "score" for this angle
				sumDerivX[discreteAngle] += dx;
				sumDerivY[discreteAngle] += dy;
			}
		}
	}

	@Override
	protected void computeWeightedScore( int c_x , int c_y ) {
		// compute the score for each angle in the histogram
		for( int y = rect.y0; y < rect.y1; y++ ) {
			int indexX = derivX.startIndex + derivX.stride*y + rect.x0;
			int indexY = derivY.startIndex + derivY.stride*y + rect.x0;
			int indexW = (y-c_y+radiusScale)*weights.width + rect.x0-c_x+radiusScale;

			for( int x = rect.x0; x < rect.x1; x++ , indexX++ , indexY++ , indexW++ ) {
				float w = weights.data[indexW];

				int dx = derivX.data[indexX];
				int dy = derivY.data[indexY];

				double angle = Math.atan2(dy,dx);
				// compute which discretized angle it is
				int discreteAngle = (int)((angle + angleRound)/angleDiv) % numAngles;
				// sum up the "score" for this angle
				sumDerivX[discreteAngle] += w*dx;
				sumDerivY[discreteAngle] += w*dy;
			}
		}
	}

}
