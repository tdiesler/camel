/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.scala.dsl.builder;

import org.apache.camel.model.{ChoiceDefinition, ProcessorDefinition}
import org.apache.camel.model.dataformat.DataFormatDefinition
import org.apache.camel.Routes

import collection.mutable.Stack
import _root_.scala.reflect.Manifest

import org.apache.camel.scala.dsl._

import org.apache.camel.scala.dsl.languages.Languages

/**
 * Scala RouteBuilder implementation
 */
class RouteBuilder extends Preamble with DSL with Routes with Languages {

  val builder = new org.apache.camel.builder.RouteBuilder {
    override def configure() =  {}
  }

  val stack = new Stack[DSL];

  implicit def stringToRoute(target: String) : SRouteDefinition = new SRouteDefinition(builder.from(target), this)  
  implicit def unwrap[W](wrapper: Wrapper[W]) = wrapper.unwrap
  implicit def constantToExpression(value: Any) : (Exchange => Any) = (exchange: Exchange) => value 

  def print() = {
    println(builder)
    this
  }

  def build(context: DSL, block: => Unit) {
    stack.push(context)
    block
    stack.pop()
  }

  def from(uri: String) = new SRouteDefinition(builder.from(uri), this)
  def handle[E](block: => Unit)(implicit manifest: Manifest[E]) = {
    val exception = new SOnExceptionDefinition(builder.onException(manifest.erasure))(this)
    exception.apply(block)
  }

  def attempt = stack.top.attempt
  def bean(bean: Any) = stack.top.bean(bean)
  def choice = stack.top.choice
  def -->(uris: String*) = stack.top.to(uris: _*)
  def to(uris: String*) = stack.top.to(uris: _*)
  def when(filter: Exchange => Boolean) = stack.top.when(filter)
  def as[Target](toType: Class[Target]) = stack.top.as(toType)
  def recipients(expression: Exchange => Any) = stack.top.recipients(expression)
  def idempotentconsumer(expression: Exchange => Any) = stack.top.idempotentconsumer(expression)
  def inOnly = stack.top.inOnly
  def inOut = stack.top.inOut
  def loop(expression: Exchange => Any) = stack.top.loop(expression)
  def split(expression: Exchange => Any) = stack.top.split(expression)
  def otherwise = stack.top.otherwise
  def marshal(format: DataFormatDefinition) = stack.top.marshal(format)
  def multicast = stack.top.multicast
  def process(function: Exchange => Unit) = stack.top.process(function)
  def throttle(frequency: Frequency) = stack.top.throttle(frequency)
  def loadbalance = stack.top.loadbalance
  def delay(delay: Period) = stack.top.delay(delay)
  def resequence(expression: Exchange => Any) = stack.top.resequence(expression)
  def rollback = stack.top.rollback
  def setbody(expression : Exchange => Any) = stack.top.setbody(expression)
  def setheader(name: String, expression: Exchange => Any) = stack.top.setheader(name, expression)
  def thread(count: Int) = stack.top.thread(count)
  def unmarshal(format: DataFormatDefinition) = stack.top.unmarshal(format)
  def wiretap(uri: String) = stack.top.wiretap(uri)
  def aggregate(expression: Exchange => Any) = stack.top.aggregate(expression)

  // implementing the Routes interface to allow RouteBuilder to be discovered by Spring
  def getRouteList : java.util.List[Route[_ <: org.apache.camel.Exchange]] = builder.getRouteList()
  def getContext = builder.getContext()
  def setContext(context: CamelContext) = builder.setContext(context)
  
  val serialization = new org.apache.camel.model.dataformat.SerializationDataFormat
}
