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

package boofcv.alg.feature.describe;


import boofcv.factory.filter.kernel.FactoryKernelGaussian;
import boofcv.struct.convolve.Kernel2D_F64;

/**
 * @author Peter Abeles
 */
public class TestSurfDescribeOps_feature extends StandardSurfTests{

	Kernel2D_F64 weightSurf = FactoryKernelGaussian.gaussianWidth(-1, 20);

	@Override
	protected void describe(double x, double y, double yaw, double scale, double[] features) {
		SurfDescribeOps.features(x,y,yaw, scale, weightSurf,4,5, sparse,features);
	}
}
