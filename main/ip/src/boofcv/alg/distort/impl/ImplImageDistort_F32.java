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

package boofcv.alg.distort.impl;

import boofcv.alg.distort.ImageDistort;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.core.image.border.ImageBorder;
import boofcv.core.image.border.ImageBorder_F32;
import boofcv.struct.distort.PixelTransform_F32;
import boofcv.struct.image.ImageFloat32;


/**
 * <p>Implementation of {@link boofcv.alg.distort.ImageDistort}.</p>
 *
 * <p>
 * DO NOT MODIFY: Generated by {@link boofcv.alg.distort.impl.GeneratorImplImageDistort}.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"UnnecessaryLocalVariable"})
public class ImplImageDistort_F32 implements ImageDistort<ImageFloat32> {

	// transform from dst to src image
	private PixelTransform_F32 dstToSrc;
	// sub pixel interpolation
	private InterpolatePixel<ImageFloat32> interp;
	// handle the image border
	private ImageBorder_F32 border;

	// crop boundary
	int x0,y0,x1,y1;

	public ImplImageDistort_F32(PixelTransform_F32 dstToSrc, InterpolatePixel<ImageFloat32> interp , ImageBorder<ImageFloat32> border ) {
		this.dstToSrc = dstToSrc;
		this.interp = interp;
		this.border = (ImageBorder_F32)border;
	}

	@Override
	public void setModel(PixelTransform_F32 dstToSrc) {
		this.dstToSrc = dstToSrc;
	}

	@Override
	public void apply( ImageFloat32 srcImg , ImageFloat32 dstImg ) {
		interp.setImage(srcImg);

		x0 = 0;y0 = 0;x1 = dstImg.width;y1 = dstImg.height;

		if( border != null )
			applyBorder(srcImg, dstImg);
		else
			applyNoBorder(srcImg, dstImg);
	}

	@Override
	public void apply( ImageFloat32 srcImg , ImageFloat32 dstImg , int dstX0, int dstY0, int dstX1, int dstY1 ) {
		interp.setImage(srcImg);

		x0 = dstX0;y0 = dstY0;x1 = dstX1;y1 = dstY1;

		if( border != null )
			applyBorder(srcImg, dstImg);
		else
			applyNoBorder(srcImg, dstImg);
	}

	public void applyBorder( ImageFloat32 srcImg , ImageFloat32 dstImg ) {

		final float widthF = srcImg.getWidth();
		final float heightF = srcImg.getHeight();

		for( int y = y0; y < y1; y++ ) {
			int indexDst = dstImg.startIndex + dstImg.stride*y + x0;
			for( int x = x0; x < x1; x++ , indexDst++ ) {
				dstToSrc.compute(x,y);

				final float sx = dstToSrc.distX;
				final float sy = dstToSrc.distY;

				if( sx < 0f || sx >= widthF || sy < 0f || sy >= heightF ) {
					dstImg.data[indexDst] = border.getOutside((int)sx,(int)sy);
				} else {
					dstImg.data[indexDst] = interp.get(sx,sy);
				}
			}
		}
	}

	public void applyNoBorder( ImageFloat32 srcImg , ImageFloat32 dstImg ) {

		final float widthF = srcImg.getWidth();
		final float heightF = srcImg.getHeight();

		for( int y = y0; y < y1; y++ ) {
			int indexDst = dstImg.startIndex + dstImg.stride*y + x0;
			for( int x = x0; x < x1; x++ , indexDst++ ) {
				dstToSrc.compute(x,y);

				final float sx = dstToSrc.distX;
				final float sy = dstToSrc.distY;

				if( sx >= 0f && sx < widthF && sy >= 0f && sy < heightF ) {
					dstImg.data[indexDst] = interp.get(sx,sy);
				}
			}
		}
	}

}
