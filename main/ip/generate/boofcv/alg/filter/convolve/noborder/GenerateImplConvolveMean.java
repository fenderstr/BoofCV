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

package boofcv.alg.filter.convolve.noborder;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * @author Peter Abeles
 */
public class GenerateImplConvolveMean {

	String className = "ImplConvolveMean";
	PrintStream out;

	AutoTypeImage imageIn;
	AutoTypeImage imageOut;

	public GenerateImplConvolveMean() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	public void createAll() throws FileNotFoundException {
		printPreamble();
		addFunctions(AutoTypeImage.U8, AutoTypeImage.I8);
		addFunctions(AutoTypeImage.S16, AutoTypeImage.I16);
		addFunctions(AutoTypeImage.F32, AutoTypeImage.F32);
		out.println("}");
	}

	public void addFunctions( AutoTypeImage imageIn , AutoTypeImage imageOut ) throws FileNotFoundException {

		this.imageIn = imageIn;
		this.imageOut = imageOut;
		printHorizontal();
		printVertical();
	}

	public void printPreamble() {
		out.print(CodeGeneratorUtil.copyright);
		out.print("package boofcv.alg.filter.convolve.noborder;\n" +
				"\n" +
				"import boofcv.struct.image.*;\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Convolves a mean filter across the image.  The mean value of all the pixels are computed inside the kernel.\n" +
				" * </p>\n" +
				" * <p>\n" +
				" * Do not modify.  Auto generated by GenerateImplConvolveBox.\n" +
				" * </p>\n" +
				" * \n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class " + className + " {\n\n");
	}

	public void printHorizontal() {

		String typeCast = imageOut.getTypeCastFromSum();
		String sumType = imageIn.getSumType();
		String bitWise = imageIn.getBitWise();

		out.print("\tpublic static void horizontal( " + imageIn.getImageName() + " input , " + imageOut.getImageName() + " output , int radius , boolean includeBorder) {\n" +
				"\t\tfinal int kernelWidth = radius*2 + 1;\n" +
				"\n" +
				"\t\tfinal int startY = includeBorder ? 0 : radius;\n" +
				"\t\tfinal int endY = includeBorder ? input.height : input.height - radius;\n" +
				"\t\tfinal " + sumType + " divisor = kernelWidth;\n" +
				"\n" +
				"\t\tfor( int y = startY; y < endY; y++ ) {\n" +
				"\t\t\tint indexIn = input.startIndex + input.stride*y;\n" +
				"\t\t\tint indexOut = output.startIndex + output.stride*y + radius;\n" +
				"\n" +
				"\t\t\t" + sumType + " total = 0;\n" +
				"\n" +
				"\t\t\tint indexEnd = indexIn + kernelWidth;\n" +
				"\t\t\t\n" +
				"\t\t\tfor( ; indexIn < indexEnd; indexIn++ ) {\n" +
				"\t\t\t\ttotal += input.data[indexIn] " + bitWise + ";\n" +
				"\t\t\t}\n" +
				"\t\t\toutput.data[indexOut++] = " + typeCast + "(total/divisor);\n" +
				"\n" +
				"\t\t\tindexEnd = indexIn + input.width - kernelWidth;\n" +
				"\t\t\tfor( ; indexIn < indexEnd; indexIn++ ) {\n" +
				"\t\t\t\ttotal -= input.data[ indexIn - kernelWidth ] " + bitWise + ";\n" +
				"\t\t\t\ttotal += input.data[ indexIn ] " + bitWise + ";\n" +
				"\n" +
				"\t\t\t\toutput.data[indexOut++] = " + typeCast + "(total/divisor);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printVertical() {

		String typeCast = imageOut.getTypeCastFromSum();
		String sumType = imageIn.getSumType();
		String bitWise = imageIn.getBitWise();

		out.print("\tpublic static void vertical( " + imageIn.getImageName() + " input , " + imageOut.getImageName() + " output , int radius , boolean includeBorder ) {\n" +
				"\t\tfinal int kernelWidth = radius*2 + 1;\n" +
				"\n" +
				"\t\tfinal int startX = includeBorder ? 0 : radius;\n" +
				"\t\tfinal int endX = includeBorder ? input.width : input.width - radius;\n" +
				"\n" +
				"\t\tfinal int backStep = kernelWidth*input.stride;\n" +
				"\n" +
				"\t\t"+sumType+" divisor = kernelWidth;\n" +
				"\t\t"+sumType+" totals[] = new "+sumType+"[ input.width ];\n" +
				"\n" +
				"\t\tfor( int x = startX; x < endX; x++ ) {\n" +
				"\t\t\tint indexIn = input.startIndex + x;\n" +
				"\t\t\tint indexOut = output.startIndex + output.stride*radius + x;\n" +
				"\n" +
				"\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\tint indexEnd = indexIn + input.stride*kernelWidth;\n" +
				"\t\t\tfor( ; indexIn < indexEnd; indexIn += input.stride) {\n" +
				"\t\t\t\ttotal += input.data[indexIn] "+bitWise+";\n" +
				"\t\t\t}\n" +
				"\t\t\ttotals[x] = total;\n" +
				"\t\t\toutput.data[indexOut] = "+typeCast+"(total/divisor);\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// change the order it is processed in to reduce cache misses\n" +
				"\t\tfor( int y = radius+1; y < output.height-radius; y++ ) {\n" +
				"\t\t\tint indexIn = input.startIndex + (y+radius)*input.stride+startX;\n" +
				"\t\t\tint indexOut = output.startIndex + y*output.stride+startX;\n" +
				"\n" +
				"\t\t\tfor( int x = startX; x < endX; x++ ,indexIn++,indexOut++) {\n" +
				"\t\t\t\t"+sumType+" total = totals[ x ]  - (input.data[ indexIn - backStep ]"+bitWise+");\n" +
				"\t\t\t\ttotals[ x ] = total += input.data[ indexIn ]"+bitWise+";\n" +
				"\n" +
				"\t\t\t\toutput.data[indexOut] = "+typeCast+"(total/divisor);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public static void main(String args[]) throws FileNotFoundException {
		GenerateImplConvolveMean generator = new GenerateImplConvolveMean();

		generator.createAll();
	}
}
