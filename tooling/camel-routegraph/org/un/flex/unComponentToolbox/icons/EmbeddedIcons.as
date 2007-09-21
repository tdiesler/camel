/* 
 * The MIT License
 *
 * Copyright (c) 2007 The SixDegrees Project Team
 * (Jason Bellone, Juan Rodriguez, Segolene de Basquiat, Daniel Lang).
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.un.flex.unComponentToolbox.icons {
	import mx.core.ClassFactory;
	
	/**
	 * This class contains all embedded icons as public
	 * static classes as some form of image library, but
	 * as they are embedded they do not need to be bundled
	 * separately.
	 * */
	public class EmbeddedIcons {
		
	 	[Embed(source="images/services.png")]
	 	static public var servicesNode:Class;
	 	
	 	[Embed(source="images/service.png")]
	 	static public var serviceNode:Class;
	 	
	 	[Embed(source="images/network.png")]
	 	static public var networkNode:Class;
	 	
	 	[Embed(source="images/cog.png")]
	 	static public var cogNode:Class;
	 	
	 	[Embed(source="images/visualization.png")]
	 	static public var sixDegreeNode:Class;

	 	[Embed(source="images/refresh.png")]
	 	static public var refreshIcon:Class;
	}
}