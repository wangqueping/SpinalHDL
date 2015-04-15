/*
 * SpinalHDL
 * Copyright (c) Dolu, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package spinal.core

/**
 * Created by PIC18F on 16.01.2015.
 */

trait BitsCast{
  def toBits(that : Data) : Bits = that.toBits
}

object BitsSet{
  def apply(bitCount: BitCount) = B((BigInt(1) << bitCount.value) - 1,bitCount)
}

trait BitsFactory{
  def Bits() = new Bits()
  def Bits(width: BitCount): Bits = Bits.setWidth(width.value)
}

class Bits extends BitVector {
  override type SSelf = Bits
  def prefix: String = "b"

  def ##(right: Bits): Bits = newBinaryOperator("b##b", right, WidthInfer.cumulateInputWidth, InputNormalize.none,ZeroWidth.binaryTakeOther)

  def |(that: Bits): Bits = newBinaryOperator("b|b", that, WidthInfer.inputMaxWidth, InputNormalize.nodeWidth,ZeroWidth.binaryTakeOther);
  def &(that: Bits): Bits = newBinaryOperator("b&b", that, WidthInfer.inputMaxWidth, InputNormalize.nodeWidth,ZeroWidth.binaryInductZeroWithOtherWidth(B.apply));
  def ^(that: Bits): Bits = newBinaryOperator("b^b", that, WidthInfer.inputMaxWidth, InputNormalize.nodeWidth,ZeroWidth.binaryTakeOther);
  def unary_~(): Bits = newUnaryOperator("~b",WidthInfer.inputMaxWidth,ZeroWidth.unaryZero);

  override def ===(that: SSelf): Bool = newLogicalOperator("b==b", that, InputNormalize.inputWidthMax,ZeroWidth.binaryThatIfBoth(True));
  override def !==(that: SSelf): Bool = newLogicalOperator("b!=b", that, InputNormalize.inputWidthMax,ZeroWidth.binaryThatIfBoth(False));

  def >>(that: Int): this.type = newBinaryOperator("b>>i", IntLiteral(that), WidthInfer.shiftRightWidth, InputNormalize.none,ZeroWidth.shiftRightImpl);
  def <<(that: Int): this.type = newBinaryOperator("b<<i", IntLiteral(that), WidthInfer.shiftLeftWidth, InputNormalize.none,ZeroWidth.shiftLeftImpl(B.apply));
  def >>(that: UInt): this.type = newBinaryOperator("b>>u", that, WidthInfer.shiftRightWidth, InputNormalize.none,ZeroWidth.shiftRightImpl);
  def <<(that: UInt): this.type = newBinaryOperator("b<<u", that, WidthInfer.shiftLeftWidth, InputNormalize.none,ZeroWidth.shiftLeftImpl(B.apply));

  override def \(that: SSelf) = super.\(that)
  override def :=(that: SSelf): Unit = super.:=(that)
  override def <>(that: SSelf): Unit = super.<>(that)

  override def newMultiplexor(sel: Bool, whenTrue: Node, whenFalse: Node): Multiplexer = Multiplex("mux(B,b,b)", sel, whenTrue, whenFalse)

  override def resize(width: Int): this.type = newResize("resize(b,i)", this :: new IntLiteral(width) :: Nil, WidthInfer.intLit1Width,ZeroWidth.resizeImpl(B.apply))

  def toSInt: SInt = new SInt().castFrom("b->s", this)
  def toUInt: UInt = new UInt().castFrom("b->u", this)

  override def toBits: Bits = {
    val ret = new Bits()
    ret := this
    ret
  }
  override def assignFromBits(bits: Bits): Unit = this := bits

  override def isEguals(that: Data): Bool = {
    that match {
      case that: Bits => this === that
      case _ => SpinalError(s"Don't know how compare $this with $that"); null
    }
  }

  def toDataType[T <: Data](dataType : T) : T = {
    val ret = cloneOf(dataType)
    ret.assignFromBits(this)
    ret
  }

  override def getZero: this.type = B(0).asInstanceOf[this.type]
}