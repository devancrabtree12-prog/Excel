package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BlueprintSummary
import com.example.model.WorkbookPlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "blueprints")
data class BlueprintEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val domain: String,
    val keyFormulas: String, // comma separated or JSON string
    val effectivenessScore: Int,
    val notes: String,
    val workbookPlanJson: String,
    val isPrebuilt: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toWorkbookPlan(): WorkbookPlan? {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(WorkbookPlan::class.java)
            adapter.fromJson(workbookPlanJson)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun fromWorkbookPlan(
            plan: WorkbookPlan,
            category: String = "General",
            isPrebuilt: Boolean = false
        ): BlueprintEntity {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(WorkbookPlan::class.java)
            val json = adapter.toJson(plan)

            val summary = plan.blueprintSummary
            val formulas = plan.sheets.flatMap { s ->
                s.kpiCards.map { it.formula } + s.columns.mapNotNull { it.formula }
            }.distinct().take(4).joinToString(", ")

            return BlueprintEntity(
                id = plan.id,
                title = plan.title,
                category = summary?.categories?.firstOrNull() ?: category,
                domain = plan.domain,
                keyFormulas = formulas.ifEmpty { "=SUM(), =AVERAGE()" },
                effectivenessScore = summary?.effectivenessScore ?: 95,
                notes = summary?.notes ?: "Extracted multi-tab enterprise architecture.",
                workbookPlanJson = json,
                isPrebuilt = isPrebuilt,
                createdAt = plan.createdAt
            )
        }
    }
}
