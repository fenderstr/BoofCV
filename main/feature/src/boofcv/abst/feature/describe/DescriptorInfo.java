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

package boofcv.abst.feature.describe;

import boofcv.struct.feature.TupleDesc;

/**
 * Provides information about the feature's descriptor.
 *
 * @author Peter Abeles
 */
public interface DescriptorInfo<Desc extends TupleDesc> {

	/**
	 * Creates new description instance which can be processed by this class
	 *
	 * @return New descriptor
	 */
	public Desc createDescription();

	/**
	 * Returns the number of elements in the descriptor
	 *
	 * @return Number of elements in the description
	 */
	public int getDescriptionLength();

	/**
	 * The type of region descriptor generated
	 *
	 * @return Returns the descriptor type.
	 */
	public Class<Desc> getDescriptionType();
}
