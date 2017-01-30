package io.iohk.ethereum.db

import io.iohk.ethereum.ObjectGenerators
import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks
import io.iohk.ethereum.rlp.{decode => rlpDecode, encode => rlpEncode}
import io.iohk.ethereum.rlp.RLPImplicits._

import scala.util.Random

class KeyValueStorageSuite extends FunSuite with PropertyChecks with ObjectGenerators{
  val iterationsNumber = 100

  test("Insert ints to KeyValueStorage") {
    forAll(Gen.listOfN(iterationsNumber, Gen.listOf(intGen))) { listOfListOfInt =>
      val initialKeyValueStorage = new KeyValueStorage[Int, Int](
        dataSource = EphemDataSource(),
        rlpEncode(_),
        rlpEncode(_),
        rlpDecode[Int]
      )

      val keyValueStorage = listOfListOfInt.foldLeft(initialKeyValueStorage){ case (recKeyValueStorage, intList) =>
        recKeyValueStorage.update(Seq(), intList.zip(intList))
      }

      listOfListOfInt.flatten.foreach{ i =>
        assert(keyValueStorage.get(i).contains(i))
      }
    }
  }

  test("Delete ints from KeyValueStorage") {
    forAll(Gen.listOf(intGen)) { listOfInt =>
      //Insert of keys
      val initialKeyValueStorage = new KeyValueStorage[Int, Int](
        dataSource = EphemDataSource(),
        rlpEncode(_),
        rlpEncode(_),
        rlpDecode[Int]
      ).update(Seq(), listOfInt.zip(listOfInt))

      val (toDelete, toLeave) = Random.shuffle(listOfInt).splitAt(Gen.choose(0, listOfInt.size).sample.get)

      //Delete of ints
      val keyValueStorage = toDelete.foldLeft(initialKeyValueStorage){ case (recKeyValueStorage, i) =>
        recKeyValueStorage.update(Seq(i), Seq())
      }

      toDelete.foreach{ i =>
        assert(keyValueStorage.get(i).isEmpty)
      }
      toLeave.foreach{ i =>
        assert(keyValueStorage.get(i).contains(i))
      }
    }
  }
}
