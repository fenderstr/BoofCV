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

package boofcv.alg.feature.detect.interest;

import boofcv.abst.filter.ImageFunctionSparse;
import boofcv.factory.filter.derivative.FactoryDerivativeSparse;
import boofcv.factory.transform.gss.FactoryGaussianScaleSpace;
import boofcv.struct.gss.GaussianScaleSpace;
import boofcv.struct.image.ImageFloat32;

/**
 * @author Peter Abeles
 */
public class TestFeatureLaplaceScaleSpace extends GenericFeatureScaleDetector {

	@Override
	protected Object createDetector(GeneralFeatureDetector<ImageFloat32, ImageFloat32> detector) {
		ImageFunctionSparse<ImageFloat32> sparseLaplace = FactoryDerivativeSparse.createLaplacian(ImageFloat32.class, null);

		return new FeatureLaplaceScaleSpace<ImageFloat32, ImageFloat32>(detector, sparseLaplace, 2);
	}

	@Override
	protected int detectFeature(ImageFloat32 input, Object detector) {
		GaussianScaleSpace<ImageFloat32, ImageFloat32> ss = FactoryGaussianScaleSpace.nocache_F32();
		ss.setScales(new double[]{1,2,4,8});
		ss.setImage(input);

		FeatureLaplaceScaleSpace<ImageFloat32, ImageFloat32> alg = (FeatureLaplaceScaleSpace<ImageFloat32, ImageFloat32>) detector;
		alg.detect(ss);
		return alg.getInterestPoints().size();
	}
}
