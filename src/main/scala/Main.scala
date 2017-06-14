import scala.lms.common._
import ch.ethz.acl.passera.unsigned._
import ch.ethz.acl.intrinsics._

object Main {
  //
  // Create the DSL or the LMS Intermediate Representation (IR).
  // This IR will support AVX and AVX2 instructions. In order to
  // make it work, lets make sure that it also supports arrays,
  // booleans operations and primitve nummeric operations.
  //
  val AVX2_IR = new PrimitiveOpsExpOpt
    with NumericOpsExpOpt 
    with BooleanOpsExp
    with ArrayOpsExpOpt
    with SeqOpsExp
    with IntrinsicsBase
    with IntrinsicsArrays
    with AVX
    with AVX2
  { self =>
    //
    // Since the Intrinsics also work with unsigned types
    // we must include those in the DSL. Pure LMS does not
    // take care of unsigned yet.
    //
    implicit def anyTyp    : Typ[Any]    = manifestTyp
    implicit def uByteTyp  : Typ[UByte]  = manifestTyp
    implicit def uIntTyp   : Typ[UInt]   = manifestTyp
    implicit def uLongTyp  : Typ[ULong]  = manifestTyp
    implicit def uShortTyp : Typ[UShort] = manifestTyp
    //
    // Create a C code generator. And make sure you can
    // generate code for each included node IR that we
    // have mixed in the DSL.
    //
    val codegen = new CGenNumericOps
      with CGenPrimitiveOps
      with CGenBooleanOps
      with CGenArrayOps
      with CGenSeqOps
      with CGenAVX
      with CGenAVX2
    {
      val IR: self.type = self
      //
      // Make sure that the Arrays are remapped properly.
      //
      override def remap[A](m: Typ[A]) : String = {
        if (m.erasure.isArray) {
          remap(m.typeArguments.head) + "*"
        } else {
          super.remap(m)
        }
      }
    }
  }
  //
  // Make each AVX intrinsic available in the context that we work.
  //
  import AVX2_IR._
  //
  // Stage a simple function that takes two arrays of Floats, a and b
  // Extracts 8 elements from the first array, adds them, and writes
  // the result to array b. LMS assumes that both a and b are immutable.
  // Therefore, lets inform LMS that b will hold our results.
  //
  def add_first_8elements(a: Rep[Array[Float]], b: Rep[Array[Float]]): Rep[Unit] = {
    val b_write = reflectMutableSym(b.asInstanceOf[Sym[Array[Float]]])
    val tmp1 = _mm256_loadu_ps(a, Const(0))
    val tmp2 = _mm256_add_ps(tmp1, tmp1)
    _mm256_storeu_ps(b_write, tmp2, Const(0))
  }

  def main(args: Array[String]) : Unit = {
    val source = new java.io.StringWriter()
    val writer = new java.io.PrintWriter(source)
    //
    // Generate the C code for the add_first_8elements.
    //
    codegen.emitSource2(add_first_8elements, "add_first_8elements", writer)
    //
    // Print the AVX2 headers.
    //
    codegen.getIntrinsicsHeaders().foreach(h => {
      println("#include <" + h + ">")
    })
    //
    // Print the rest of the code to System.out.
    //
    println(source.toString)
  }

}
