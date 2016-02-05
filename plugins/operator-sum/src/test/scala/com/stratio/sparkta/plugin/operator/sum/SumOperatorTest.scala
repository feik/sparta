/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.operator.sum

import com.stratio.sparkta.sdk.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class SumOperatorTest extends WordSpec with Matchers {

  "Sum operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaFail = StructType(Seq(
      StructField("field2", IntegerType, false)
    ))

    "processMap must be " in {
      val inputField = new SumOperator("sum", initSchema, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new SumOperator("sum", initSchemaFail, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val inputFields3 = new SumOperator("sum", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val inputFields4 = new SumOperator("sum", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row("1", 2)) should be(Some(1))

      val inputFields5 = new SumOperator("sum", initSchema, Map("inputField" -> "field1"))
      inputFields5.processMap(Row("foo", 2)) should be(None)

      val inputFields6 = new SumOperator("sum", initSchema, Map("inputField" -> "field1"))
      inputFields6.processMap(Row(1.5, 2)) should be(Some(1.5))

      val inputFields7 = new SumOperator("sum", initSchema, Map("inputField" -> "field1"))
      inputFields7.processMap(Row(5L, 2)) should be(Some(5L))

      val inputFields8 = new SumOperator("sum", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields8.processMap(Row(1, 2)) should be(Some(1L))

      val inputFields9 = new SumOperator("sum", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields9.processMap(Row(1, 2)) should be(None)

      val inputFields10 = new SumOperator("sum", initSchema,
        Map("inputField" -> "field1", "filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields10.processMap(Row(1, 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new SumOperator("sum", initSchema, Map())
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new SumOperator("sum", initSchema, Map())
      inputFields2.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some(20d))

      val inputFields3 = new SumOperator("sum", initSchema, Map())
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be(Some(20d))

      val inputFields4 = new SumOperator("sum", initSchema, Map())
      inputFields4.processReduce(Seq(None)) should be(Some(0d))
    }

    "processReduce distinct must be " in {
      val inputFields = new SumOperator("sum", initSchema, Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new SumOperator("sum", initSchema, Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(1), Some(2), Some(1))) should be(Some(3d))
    }

    "associative process must be " in {
      val inputFields = new SumOperator("count", initSchema, Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(2d))

      val inputFields2 = new SumOperator("count", initSchema, Map("typeOp" -> "string"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      inputFields2.associativity(resultInput2) should be(Some("2.0"))
    }
  }
}
