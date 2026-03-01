package com.bountybugger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bountybugger.domain.model.*

/**
 * Room entity for storing bounty programs in local database
 */
@Entity(tableName = "bounty_programs")
@TypeConverters(BountyConverters::class)
data class BountyProgramEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val platform: String,
    val url: String,
    val description: String?,
    val rewardsMin: Int?,
    val rewardsMax: Int?,
    val rewardsCurrency: String?,
    val rewardsDetails: String?,
    val scopesJson: String,
    val outOfScopesJson: String,
    val bountyTypesJson: String,
    val industriesJson: String,
    val publishedAt: String?,
    val lastUpdated: String?,
    val isPrivate: Boolean,
    val disclosurePolicy: String?,
    val safeHarbor: Boolean,
    val savedAt: Long,
    val isFavorite: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): BountyProgram {
        val converters = BountyConverters()
        return BountyProgram(
            id = id,
            name = name,
            platform = BountyPlatform.valueOf(platform),
            url = url,
            description = description,
            rewards = if (rewardsMin != null || rewardsMax != null) {
                Rewards(rewardsMin, rewardsMax, rewardsCurrency ?: "USD", rewardsDetails)
            } else null,
            scopes = converters.fromJsonToScopeList(scopesJson),
            outOfScopes = converters.fromJsonToStringList(outOfScopesJson),
            bountyType = converters.fromJsonToBountyTypeList(bountyTypesJson),
            industry = converters.fromJsonToIndustryList(industriesJson),
            minBounty = rewardsMin,
            maxBounty = rewardsMax,
            publishedAt = publishedAt,
            lastUpdated = lastUpdated,
            isPrivate = isPrivate,
            eligibility = null,
            disclosurePolicy = disclosurePolicy,
            safeHarbor = safeHarbor,
            savedAt = savedAt,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromDomainModel(program: BountyProgram): BountyProgramEntity {
            val converters = BountyConverters()
            return BountyProgramEntity(
                id = program.id,
                name = program.name,
                platform = program.platform.name,
                url = program.url,
                description = program.description,
                rewardsMin = program.minBounty,
                rewardsMax = program.maxBounty,
                rewardsCurrency = program.rewards?.currency,
                rewardsDetails = program.rewards?.details,
                scopesJson = converters.scopeListToJson(program.scopes),
                outOfScopesJson = converters.stringListToJson(program.outOfScopes),
                bountyTypesJson = converters.bountyTypeListToJson(program.bountyType),
                industriesJson = converters.industryListToJson(program.industry),
                publishedAt = program.publishedAt,
                lastUpdated = program.lastUpdated,
                isPrivate = program.isPrivate,
                disclosurePolicy = program.disclosurePolicy,
                safeHarbor = program.safeHarbor,
                savedAt = program.savedAt ?: System.currentTimeMillis(),
                isFavorite = program.isFavorite
            )
        }
    }
}

/**
 * Type converters for Room database
 */
class BountyConverters {
    @TypeConverter
    fun scopeListToJson(scopes: List<Scope>): String {
        return com.google.gson.Gson().toJson(scopes)
    }

    @TypeConverter
    fun fromJsonToScopeList(json: String): List<Scope> {
        val type = object : com.google.gson.reflect.TypeToken<List<Scope>>() {}.type
        return com.google.gson.Gson().fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    fun stringListToJson(list: List<String>): String {
        return com.google.gson.Gson().toJson(list)
    }

    @TypeConverter
    fun fromJsonToStringList(json: String): List<String> {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        return com.google.gson.Gson().fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    fun bountyTypeListToJson(types: List<BountyType>): String {
        return com.google.gson.Gson().toJson(types.map { it.name })
    }

    @TypeConverter
    fun fromJsonToBountyTypeList(json: String): List<BountyType> {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        val names: List<String> = com.google.gson.Gson().fromJson(json, type) ?: emptyList()
        return names.mapNotNull { runCatching { BountyType.valueOf(it) }.getOrNull() }
    }

    @TypeConverter
    fun industryListToJson(industries: List<Industry>): String {
        return com.google.gson.Gson().toJson(industries.map { it.name })
    }

    @TypeConverter
    fun fromJsonToIndustryList(json: String): List<Industry> {
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        val names: List<String> = com.google.gson.Gson().fromJson(json, type) ?: emptyList()
        return names.mapNotNull { runCatching { Industry.valueOf(it) }.getOrNull() }
    }
}
