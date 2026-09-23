package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.model.ConditionalFormatRule
import com.example.model.FormulaSpec
import com.example.model.TrackerPlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "trackers")
data class TrackerEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val headersJson: String,
    val sampleRowsJson: String,
    val formulasJson: String,
    val conditionalFormattingJson: String,
    val createdAt: Long
) {
    fun toTrackerPlan(): TrackerPlan {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

        val listStringAdapter = moshi.adapter<List<String>>(
            Types.newParameterizedType(List::class.java, String::class.java)
        )
        val listOfListAdapter = moshi.adapter<List<List<String>>>(
            Types.newParameterizedType(List::class.java, Types.newParameterizedType(List::class.java, String::class.java))
        )
        val formulaListAdapter = moshi.adapter<List<FormulaSpec>>(
            Types.newParameterizedType(List::class.java, FormulaSpec::class.java)
        )
        val condListAdapter = moshi.adapter<List<ConditionalFormatRule>>(
            Types.newParameterizedType(List::class.java, ConditionalFormatRule::class.java)
        )

        val headers = listStringAdapter.fromJson(headersJson) ?: emptyList()
        val sampleRows = listOfListAdapter.fromJson(sampleRowsJson) ?: emptyList()
        val formulas = formulaListAdapter.fromJson(formulasJson) ?: emptyList()
        val conditionalFormatting = condListAdapter.fromJson(conditionalFormattingJson) ?: emptyList()

        return TrackerPlan(
            id = id,
            title = title,
            headers = headers,
            sample_rows = sampleRows,
            formulas = formulas,
            conditional_formatting = conditionalFormatting,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromTrackerPlan(plan: TrackerPlan): TrackerEntity {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

            val listStringAdapter = moshi.adapter<List<String>>(
                Types.newParameterizedType(List::class.java, String::class.java)
            )
            val listOfListAdapter = moshi.adapter<List<List<String>>>(
                Types.newParameterizedType(List::class.java, Types.newParameterizedType(List::class.java, String::class.java))
            )
            val formulaListAdapter = moshi.adapter<List<FormulaSpec>>(
                Types.newParameterizedType(List::class.java, FormulaSpec::class.java)
            )
            val condListAdapter = moshi.adapter<List<ConditionalFormatRule>>(
                Types.newParameterizedType(List::class.java, ConditionalFormatRule::class.java)
            )

            return TrackerEntity(
                id = plan.id,
                title = plan.title,
                headersJson = listStringAdapter.toJson(plan.headers),
                sampleRowsJson = listOfListAdapter.toJson(plan.sample_rows),
                formulasJson = formulaListAdapter.toJson(plan.formulas),
                conditionalFormattingJson = condListAdapter.toJson(plan.conditional_formatting),
                createdAt = plan.createdAt
            )
        }
    }
}
