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

package gecv.alg.detect.corner;

import gecv.alg.filter.derivative.GradientSobel;
import gecv.alg.filter.derivative.HessianThree;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt16;

/**
 * Tests basic properties of a corner detector which use the image gradient
 *
 * @author Peter Abeles
 */
public abstract class GenericCornerIntensityGradientTests extends GenericCornerIntensityTests {

	protected ImageSInt16 derivX_I16 = new ImageSInt16(width,height);
	protected ImageSInt16 derivY_I16 = new ImageSInt16(width,height);
	protected ImageSInt16 derivXX_I16 = new ImageSInt16(width,height);
	protected ImageSInt16 derivYY_I16 = new ImageSInt16(width,height);
	protected ImageSInt16 derivXY_I16 = new ImageSInt16(width,height);

	protected ImageFloat32 derivX_F32 = new ImageFloat32(width,height);
	protected ImageFloat32 derivY_F32 = new ImageFloat32(width,height);
	protected ImageFloat32 derivXX_F32 = new ImageFloat32(width,height);
	protected ImageFloat32 derivYY_F32 = new ImageFloat32(width,height);
	protected ImageFloat32 derivXY_F32 = new ImageFloat32(width,height);

	@Override
	protected void computeDerivatives() {
		GradientSobel.process(imageF,derivX_F32,derivY_F32);
		GradientSobel.process(imageI,derivX_I16,derivY_I16);
		HessianThree.process(imageF,derivXX_F32,derivYY_F32,derivXY_F32);
		HessianThree.process(imageI,derivXX_I16,derivYY_I16,derivXY_I16);
	}
}
