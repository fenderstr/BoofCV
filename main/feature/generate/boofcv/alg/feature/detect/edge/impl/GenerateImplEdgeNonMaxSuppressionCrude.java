/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.feature.detect.edge.impl;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;

import java.io.FileNotFoundException;


/**
 * @author Peter Abeles
 */
public class GenerateImplEdgeNonMaxSuppressionCrude extends CodeGeneratorBase {
	String className = "ImplEdgeNonMaxSuppressionCrude";

	public GenerateImplEdgeNonMaxSuppressionCrude() throws FileNotFoundException {
		setOutputFile(className);
	}

	@Override
	public void generate() throws FileNotFoundException {
		printPreamble();

		printInner(AutoTypeImage.F32);
		printInner(AutoTypeImage.S16);
		printInner(AutoTypeImage.S32);

		printBorder(AutoTypeImage.F32);
		printBorder(AutoTypeImage.I);

		out.print("\n" +
				"}\n");
	}

	private void printPreamble() {
		out.print("import boofcv.core.image.border.FactoryImageBorder;\n" +
				"import boofcv.core.image.border.*;\n" +
				"import boofcv.struct.image.*;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Implementations of the crude version of non-maximum edge suppression.  If the gradient is positive or negative\n" +
				" * is used to determine the direction of suppression.\n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * DO NOT MODIFY. Generated by {@link GenerateImplEdgeNonMaxSuppressionCrude}.\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	private void printInner(AutoTypeImage derivType) {

		String bitWise = derivType.getBitWise();

		out.print("\t/**\n" +
				"\t * Only processes the inner image.  Ignoring the border.\n" +
				"\t */\n" +
				"\tstatic public void inner4( ImageFloat32 intensity , "+derivType.getImageName()+" derivX , "+derivType.getImageName()+" derivY, ImageFloat32 output )\n" +
				"\t{\n" +
				"\t\tfinal int w = intensity.width;\n" +
				"\t\tfinal int h = intensity.height-1;\n" +
				"\n" +
				"\t\tfor( int y = 1; y < h; y++ ) {\n" +
				"\t\t\tint indexI = intensity.startIndex + y*intensity.stride+1;\n" +
				"\t\t\tint indexX = derivX.startIndex + y*derivX.stride+1;\n" +
				"\t\t\tint indexY = derivY.startIndex + y*derivY.stride+1;\n" +
				"\t\t\tint indexO = output.startIndex + y*output.stride+1;\n" +
				"\n" +
				"\t\t\tint end = indexI + w - 2;\n" +
				"\t\t\tfor( ; indexI < end; indexI++ , indexX++, indexY++, indexO++ ) {\n" +
				"\t\t\t\tint dx,dy;\n" +
				"\n" +
				"\t\t\t\tif( derivX.data[indexX]"+bitWise+" > 0 ) dx = 1; else dx = -1;\n" +
				"\t\t\t\tif( derivY.data[indexY]"+bitWise+" > 0 ) dy = 1; else dy = -1;\n" +
				"\n" +
				"\t\t\t\tfloat middle = intensity.data[indexI];\n" +
				"\n" +
				"\t\t\t\t// suppress the value if either of its neighboring values are more than or equal to it\n" +
				"\t\t\t\tif( intensity.data[indexI-dx-dy*intensity.stride] > middle || intensity.data[indexI+dx+dy*intensity.stride] > middle ) {\n" +
				"\t\t\t\t\toutput.data[indexO] = 0;\n" +
				"\t\t\t\t} else {\n" +
				"\t\t\t\t\toutput.data[indexO] = middle;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printBorder(AutoTypeImage derivType) {
		out.print("\t/**\n" +
				"\t * Just processes the image border.\n" +
				"\t */\n" +
				"\tstatic public void border4( ImageFloat32 _intensity , "+derivType.getImageName()+" derivX , "+derivType.getImageName()+" derivY , ImageFloat32 output )\n" +
				"\t{\n" +
				"\t\tint w = _intensity.width;\n" +
				"\t\tint h = _intensity.height-1;\n" +
				"\n" +
				"\t\tImageBorder_F32 intensity = FactoryImageBorder.value(_intensity,0);\n" +
				"\n" +
				"\t\t// top border\n" +
				"\t\tfor( int x = 0; x < w; x++ ) {\n" +
				"\t\t\tint dx,dy;\n" +
				"\n" +
				"\t\t\tif( derivX.get(x,0) > 0 ) dx = 1; else dx = -1;\n" +
				"\t\t\tif( derivY.get(x,0) > 0 ) dy = 1; else dy = -1;\n" +
				"\n" +
				"\t\t\tfloat left = intensity.get(x-dx,-dy);\n" +
				"\t\t\tfloat middle = intensity.get(x,0);\n" +
				"\t\t\tfloat right = intensity.get(x+dx,dy);\n" +
				"\n" +
				"\t\t\tif( left > middle || right > middle ) {\n" +
				"\t\t\t\toutput.set(x,0,0);\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\toutput.set(x,0,middle);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// bottom border\n" +
				"\t\tfor( int x = 0; x < w; x++ ) {\n" +
				"\t\t\tint dx,dy;\n" +
				"\n" +
				"\t\t\tif( derivX.get(x,h) > 0 ) dx = 1; else dx = -1;\n" +
				"\t\t\tif( derivY.get(x,h) > 0 ) dy = 1; else dy = -1;\n" +
				"\n" +
				"\t\t\tfloat left = intensity.get(x-dx,h-dy);\n" +
				"\t\t\tfloat middle = intensity.get(x,h);\n" +
				"\t\t\tfloat right = intensity.get(x+dx,h+dy);\n" +
				"\n" +
				"\t\t\tif( left > middle || right > middle ) {\n" +
				"\t\t\t\toutput.set(x,h,0);\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\toutput.set(x,h,middle);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// left border\n" +
				"\t\tfor( int y = 1; y < h; y++ ) {\n" +
				"\t\t\tint dx,dy;\n" +
				"\n" +
				"\t\t\tif( derivX.get(0,y) > 0 ) dx = 1; else dx = -1;\n" +
				"\t\t\tif( derivY.get(0,y) > 0 ) dy = 1; else dy = -1;\n" +
				"\n" +
				"\t\t\tfloat left = intensity.get(-dx,y-dy);\n" +
				"\t\t\tfloat middle = intensity.get(0,y);\n" +
				"\t\t\tfloat right = intensity.get(dx,y+dy);\n" +
				"\n" +
				"\t\t\tif( left > middle || right > middle ) {\n" +
				"\t\t\t\toutput.set(0,y,0);\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\toutput.set(0,y,middle);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// right border\n" +
				"\t\tw = w - 1;\n" +
				"\t\tfor( int y = 1; y < h; y++ ) {\n" +
				"\t\t\tint dx,dy;\n" +
				"\n" +
				"\t\t\tif( derivX.get(w,y) > 0 ) dx = 1; else dx = -1;\n" +
				"\t\t\tif( derivY.get(w,y) > 0 ) dy = 1; else dy = -1;\n" +
				"\n" +
				"\t\t\tfloat left = intensity.get(w-dx,y-dy);\n" +
				"\t\t\tfloat middle = intensity.get(w,y);\n" +
				"\t\t\tfloat right = intensity.get(w+dx,y+dy);\n" +
				"\n" +
				"\t\t\tif( left > middle || right > middle ) {\n" +
				"\t\t\t\toutput.set(w,y,0);\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\toutput.set(w,y,middle);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateImplEdgeNonMaxSuppressionCrude app = new GenerateImplEdgeNonMaxSuppressionCrude();
		app.generate();
	}
}
