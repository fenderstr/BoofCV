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

package boofcv.alg.feature.associate;

import boofcv.struct.feature.NccFeature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Peter Abeles
 */
public class TestScoreAssociateNccFeature {

	@Test
	public void compareToExpected() {
		ScoreAssociateNccFeature scorer = new ScoreAssociateNccFeature();

		NccFeature a = new NccFeature(5);
		NccFeature b = new NccFeature(5);

		a.variance=12;
		b.variance=7;
		a.value=new double[]{1,2,3,4,5};
		b.value=new double[]{2,-1,7,-8,10};

		assertEquals(-0.46429,scorer.score(a,b),1e-2);
	}

	@Test
	public void checkZeroMinimum() {
		ScoreAssociateNccFeature scorer = new ScoreAssociateNccFeature();
		assertFalse(scorer.isZeroMinimum());
	}
}
