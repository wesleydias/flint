/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.flint.timeseries.summarize.summarizer

import com.twosigma.flint.timeseries.row.Schema
import com.twosigma.flint.timeseries.summarize.ColumnList.Sequence
import com.twosigma.flint.timeseries.summarize.{ BaseSummarizerFactory, ColumnList }
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.types.{ DoubleType, StructType }

case class VarianceSummarizerFactory(column: String, applyBesselCorrection: Boolean = true)
  extends BaseSummarizerFactory(column) {
  override def apply(inputSchema: StructType): VarianceSummarizer =
    new VarianceSummarizer(inputSchema, prefixOpt, requiredColumns, applyBesselCorrection)

}

class VarianceSummarizer(
  override val inputSchema: StructType,
  override val prefixOpt: Option[String],
  override val requiredColumns: ColumnList,
  val applyBesselCorrection: Boolean
) extends NthCentralMomentSummarizer(inputSchema, prefixOpt, requiredColumns, 2) {
  private val Sequence(Seq(column)) = requiredColumns
  override val schema = Schema.of(s"${column}_variance" -> DoubleType)
  override def fromV(v: V): GenericInternalRow = {
    var variance = v.nthCentralMoment(2)
    if (applyBesselCorrection) {
      variance = variance * (v.count / (v.count - 1d))
    }
    new GenericInternalRow(Array[Any](variance))
  }
}
