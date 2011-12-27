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

package boofcv.io.wrapper.images;

import boofcv.io.UtilIO;
import boofcv.struct.image.ImageFloat32;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLoadFileImageSequence {
	/**
	 * See if it loads the expected number of files.
	 */
	@Test
	public void basicLoadTest() {
		LoadFileImageSequence<ImageFloat32> alg = new LoadFileImageSequence<ImageFloat32>(ImageFloat32.class,
				UtilIO.getPathToBase()+"main/io/data/test/","png");

		assertTrue(ImageFloat32.class == alg.getImageType());
		assertTrue(!alg.isLoop());

		int total = 0;
		while( alg.hasNext() ) {
			total++;
			ImageFloat32 image = alg.next();
			assertEquals(100,image.width);
			assertEquals(100,image.height);

			BufferedImage buff = alg.getGuiImage();
			assertEquals(100,buff.getWidth());
			assertEquals(100,buff.getHeight());
		}

		assertEquals(3,total);
	}

		/**
	 * See if it loads the expected number of files.
	 */
	@Test
	public void checkLoop() {
		LoadFileImageSequence<ImageFloat32> alg = new LoadFileImageSequence<ImageFloat32>(ImageFloat32.class,
				UtilIO.getPathToBase()+"main/io/data/test/","png");
		alg.setLoop(true);

		assertTrue(alg.isLoop());

		int total = 0;
		while( alg.hasNext() && total < 6 ) {
			total++;
			ImageFloat32 image = alg.next();
			assertEquals(100,image.width);
			assertEquals(100,image.height);

			BufferedImage buff = alg.getGuiImage();
			assertEquals(100,buff.getWidth());
			assertEquals(100,buff.getHeight());
		}

		assertEquals(6,total);
	}
}
