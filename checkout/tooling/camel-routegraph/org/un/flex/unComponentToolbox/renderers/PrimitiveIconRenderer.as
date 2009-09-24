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
package org.un.flex.unComponentToolbox.renderers {
	
	import mx.containers.VBox;
	import mx.core.UIComponent;
	import mx.controls.Image;
	import mx.events.FlexEvent;
	import flash.events.MouseEvent;
	import mx.controls.Spacer;
	import mx.controls.LinkButton;
	import flash.events.Event;
	import org.un.flex.unComponentToolbox.icons.EmbeddedIcons;
	
	/**
	 * This renderer displays the graph nodes as
	 * primitive icon or as embedded image along with
	 * a text label. All data (whether primitive icon,
	 * if so, which one, or embedded image as well as text
	 * for the label are taken from the data object, which needs
	 * to be an XML object. The "name" attribute will be used
	 * for label and tooltip. The "nodeType" attribute will determine
	 * the icon.
	 * */
	public class PrimitiveIconRenderer extends VBox {
	
		/* this is the actual image object */
		private var _imageObject:UIComponent;
		private var _allowCustomImages:Boolean = true;
		
		/**
		 * Create an instance of the renderer.
		 * */
		public function PrimitiveIconRenderer() {
			super();
			this.addEventListener(FlexEvent.CREATION_COMPLETE,initComponent);
		}
			
		/**
		 * This method actually initialises the created
		 * renderer object. It is called on the creation
		 * complete event, which is required in order to
		 * initialise it.
		 * */
		private function initComponent(e:Event):void {
			
			var spacer:Spacer;
			var lb:LinkButton;
			
			/* set id */
			this.id = "detailView";
			
			/* set styles */
			this.setStyle("horizontalAlign","center");
			
			/* set the tool tip */
			//this.toolTip = this.data.data.@name;
			this.toolTip = this.data.data.@description;
			
			/* create and add a spacer */
			spacer = new Spacer();
			spacer.id = "makeimgalign";
			spacer.height = 18;
			this.addChild(spacer);
			
			/* create the Image component */
			createImageComponent();
			
			/* add the image component */
			this.addChild(_imageObject);
		
			/* and the linkbutton */
			lb = new LinkButton();
			lb.id = "viewDetails";
			lb.label = this.data.data.@name;
			lb.scaleX = parentDocument.scaleFactor;
			lb.scaleY = parentDocument.scaleFactor;
			//lb.toolTip = "Click to View Details";
			lb.toolTip = this.data.data.@description;
			lb.setStyle("fontWeight","normal");
			lb.setStyle("rollOverColor",0xcccccc);
			
			this.addChild(lb);
			
			this.addEventListener(MouseEvent.CLICK,parentApplication.singleClick);
		}

		/**
		 * Creates a UIComponent which is actually an image
		 * that will be the top of the vbox. The type determines
		 * whether an embedded image has to be used or if a primitive
		 * shape can be used.
		 * */
		private function createImageComponent():void {
			
			var type:String = this.data.data.@nodeType;
			
			/* first create the image object node */
			switch(type) {
		    	
				case "root":
					setImageNode(EmbeddedIcons.servicesNode);
					break;

				case "group":
					setImageNode(EmbeddedIcons.serviceNode);
					break;

				case "node":
					setImageNode(EmbeddedIcons.networkNode);
					break;

				case "box":
					setImageNode(EmbeddedIcons.cogNode);
					break;

				default:
					setImageNode(type);
					break;
			}
				
			/* for any _imageObject */
			_imageObject.scaleX = parentDocument.scaleFactor;
			_imageObject.scaleY = parentDocument.scaleFactor;
		}
		
		/**
		 * If the image type requests an embedded image, this
		 * method selects and sets the one that matches the node type
		 * from the library.
		 * */
		private function setImageNode(source):void {
			_imageObject = new Image();
    		(_imageObject as Image).source = source;
		}
	}
}

