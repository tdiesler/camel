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
package org.un.flex.unComponentToolbox.ui {
	
	import mx.containers.VBox;
	import mx.controls.Button;
	import flash.events.MouseEvent;
	
	/**
	 * This is an extended VBox that allows to hide
	 * and show anything it contains on a button press.
	 * The purpose is to hide a control panel when it is
	 * not used in order to have more drawing space available.
	 * */
	public class Grip extends VBox {
		
		/**
		 * Tooltip for Grip component */
		[Bindable]
		public var gripTip:String; 

		/**
		 * Icon to be displayed if any */
		[Bindable]
		public var gripIcon:Class;
		
		private var _cntrlPanelButton:Button;
		
		/**
		 * Standard constructor.
		 * */
		public function Grip() {
			super();
			
			/* add the control panel button */
			_cntrlPanelButton = new Button();
			_cntrlPanelButton.id = "cntrlPanelButton";
			_cntrlPanelButton.toolTip = gripTip;
			_cntrlPanelButton.toggle = true;
			_cntrlPanelButton.percentWidth = 100;
			_cntrlPanelButton.percentHeight = 100;
			_cntrlPanelButton.addEventListener(MouseEvent.CLICK,navPanel);
			//cntrlPanelButton.setStyle("upIcon",<GRIP IMAGE>);
			//cntrlPanelButton.setStyle("downIcon",<VISUALISATION IMAGE>);
			
			this.addChild(_cntrlPanelButton);
		}
		
		/**
		 * This sets the current State of the parent document
		 * to hideCntrlPanel or showCntrlPanel.
		 * @param event The mouse click event on which this method was called.
		 * */
		public function navPanel(event:MouseEvent):void {
			if (_cntrlPanelButton.selected==true) {
				parentDocument.currentState="hideCntrlPanel";
			} else {
				parentDocument.currentState="showCntrlPanel";
			}
		}
	}
}
   
  