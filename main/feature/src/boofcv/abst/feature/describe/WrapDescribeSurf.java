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

package boofcv.abst.feature.describe;

import boofcv.alg.feature.describe.DescribePointSurf;
import boofcv.alg.feature.orientation.OrientationIntegral;
import boofcv.alg.transform.ii.GIntegralImageOps;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.image.ImageSingleBand;


/**
 * @author Peter Abeles
 */
public class WrapDescribeSurf<T extends ImageSingleBand, II extends ImageSingleBand>
		implements DescribeRegionPoint<T> {

	// computes SURF feature descriptor
	DescribePointSurf<II> surf;
	// estimates feature's orientation
	// would not be included normally, but this way the integral image will only need to be computed once
	OrientationIntegral<II> orientationAlg;
	// integral image
	II ii;


	public WrapDescribeSurf(DescribePointSurf<II> surf,
							OrientationIntegral<II> orientation)
	{
		this.surf = surf;
		this.orientationAlg = orientation;
	}

	@Override
	public int getCanonicalRadius() {
		return surf.getRadius();
	}

	/**
	 *
	 *
	 *
	 * @param image The image which contains the features.
	 */
	@Override
	public void setImage(T image) {
		if( ii != null ) {
			ii.reshape(image.width,image.height);
		}

		// compute integral image
		ii = GIntegralImageOps.transform(image,ii);
		if( orientationAlg != null )
			orientationAlg.setImage(ii);
		surf.setImage(ii);
	}

	@Override
	public int getDescriptionLength() {
		return surf.getDescriptionLength();
	}

	@Override
	public TupleDesc_F64 process(double x, double y, double orientation , double scale, TupleDesc_F64 ret) {

		double angle = orientation;

		if( orientationAlg != null ) {
			orientationAlg.setScale(scale);
			angle = orientationAlg.compute(x,y);
		}

		SurfFeature f = surf.describe(x,y,scale,angle,null);
		if( f == null)
			return null;
		if( ret != null ) {
			System.arraycopy(f.value,0,ret.value,0,f.value.length);
			return ret;
		}
		return f;
	}

	@Override
	public boolean requiresScale() {
		return true;
	}

	@Override
	public boolean requiresOrientation() {
		return orientationAlg == null;
	}
}
