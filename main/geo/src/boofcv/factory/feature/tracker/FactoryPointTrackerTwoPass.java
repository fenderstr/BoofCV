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

package boofcv.factory.feature.tracker;

import boofcv.abst.feature.associate.AssociateDescription2D;
import boofcv.abst.feature.describe.DescribeRegionPoint;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.*;
import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.alg.feature.detect.interest.EasyGeneralFeatureDetector;
import boofcv.alg.feature.detect.interest.GeneralFeatureDetector;
import boofcv.alg.interpolate.InterpolateRectangle;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.factory.transform.pyramid.FactoryPyramid;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.pyramid.PyramidUpdaterDiscrete;

import static boofcv.factory.feature.tracker.FactoryPointTracker.createShiTomasi;

/**
 * @author Peter Abeles
 */
public class FactoryPointTrackerTwoPass {

	/**
	 * Pyramid KLT feature tracker.
	 *
	 * @see boofcv.struct.pyramid.PyramidUpdaterDiscrete
	 *
	 * @param config Config for the tracker. Try PkltConfig.createDefault().
	 * @param configExtract Configuration for extracting features
	 * @return KLT based tracker.
	 */
	public static <I extends ImageSingleBand, D extends ImageSingleBand>
	PointTrackerTwoPass<I> klt(PkltConfig<I, D> config, ConfigGeneralDetector configExtract) {

		GeneralFeatureDetector<I, D> detector = createShiTomasi(configExtract, config.typeDeriv);

		InterpolateRectangle<I> interpInput = FactoryInterpolation.<I>bilinearRectangle(config.typeInput);
		InterpolateRectangle<D> interpDeriv = FactoryInterpolation.<D>bilinearRectangle(config.typeDeriv);

		ImageGradient<I,D> gradient = FactoryDerivative.sobel(config.typeInput, config.typeDeriv);

		PyramidUpdaterDiscrete<I> pyramidUpdater = FactoryPyramid.discreteGaussian(config.typeInput, -1, 2);

		return new PointTrackerTwoPassKltPyramid<I, D>(config,pyramidUpdater,detector,
				gradient,interpInput,interpDeriv);
	}

	public static <I extends ImageSingleBand, D extends ImageSingleBand, Desc extends TupleDesc>
	PointTrackerTwoPass<I> dda(GeneralFeatureDetector<I, D> detector,
							   DescribeRegionPoint<I, Desc> describe,
							   AssociateDescription2D<Desc> associate1,
							   AssociateDescription2D<Desc> associate2,
							   double scale,
							   Class<I> imageType) {

		EasyGeneralFeatureDetector<I,D> easy = new EasyGeneralFeatureDetector<I, D>(detector,imageType,null);

		DdaManagerGeneralPoint<I,D,Desc> manager = new DdaManagerGeneralPoint<I,D,Desc>(easy,describe,scale);

		if( associate2 == null )
			associate2 = associate1;

		return new DetectDescribeAssociateTwoPass<I,Desc>(manager,associate1,associate2,false);
	}

	public static <I extends ImageSingleBand, Desc extends TupleDesc>
	PointTrackerTwoPass<I> dda(DetectDescribePoint<I,Desc> detectDescribe,
							   AssociateDescription2D<Desc> associate1 ,
							   AssociateDescription2D<Desc> associate2 ,
							   boolean updateDescription ) {


		DdaManagerDetectDescribePoint<I,Desc> manager = new DdaManagerDetectDescribePoint<I,Desc>(detectDescribe);

		if( associate2 == null )
			associate2 = associate1;

		return new DetectDescribeAssociateTwoPass<I,Desc>(manager, associate1,associate2,updateDescription);
	}
}
