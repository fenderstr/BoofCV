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

package boofcv.core.image.border;


/**
 * How the image border is handled by a convolution filter.  Care should be taken when selecting
 * a border method since some types will not produce meaningful results for all kernel types.
 *
 * @author Peter Abeles
 */
public enum BorderType {
	/**
	 * Image borders are not processed and are simply skipped over.
	 */
	SKIP,

	/**
	 * The pixels along the image border are extended outwards
	 */
	EXTENDED,

	/**
	 * The kernel is renormalized to take in account that parts of it are not inside the image.
	 * Typically only used with kernels that blur the image.
	 */
	NORMALIZED,

	/**
	 * Access to outside the array are reflected back into the array around the closest border.  This
	 * is an even symmetric function, e.g. f(-1) = f(1) = 1, f(-2) = f(2) = 2.
	 */
	REFLECT,

	/**
	 * Also known as periodic, an access outside of one border is wrapped around to the other border.
	 */
	WRAP,

	/**
	 * The image border is set to a fixed value
	 */
	VALUE
}
