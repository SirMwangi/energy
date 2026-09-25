package com.example.smartmoney.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Request payload for linking a bank account via bank-integration-service.
 */
@Serializable
data class LinkBankRequest(
    @SerialName("userId")
    val userId: String,

    @SerialName("accountNumber")
    val accountNumber: String,

    @SerialName("cardType")
    val cardType: String = "Debit",

    @SerialName("bankId")
    val bankId: String? = null
)

/**
 * Response payload returned when linking a bank account via bank-integration-service.
 */
@Serializable
data class BankLinkResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("accountId")
    val accountId: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("bankId")
    val bankId: String,

    @SerialName("institution")
    val institution: String,

    @SerialName("accountNumber")
    val accountNumber: String,

    @SerialName("accountName")
    val accountName: String,

    @SerialName("cardType")
    val cardType: String,

    @SerialName("maskedIdentifier")
    val maskedIdentifier: String,

    @SerialName("connectionStatus")
    val connectionStatus: String = "CONNECTED",

    @SerialName("dataSource")
    val dataSource: String = "BANK_API",

    @SerialName("message")
    val message: String? = null
)

/**
 * Supported bank information returned from bank-integration-service.
 */
@Serializable
data class SupportedBankResponse(
    @SerialName("bankId")
    val bankId: String,

    @SerialName("name")
    val name: String,

    @SerialName("status")
    val status: String,

    @SerialName("authType")
    val authType: String,

    @SerialName("supportsInstantAlerts")
    val supportsInstantAlerts: Boolean
)

/**
 * Retrofit interface for communicating with bank-integration-service (:8090).
 */
interface BankIntegrationApi {

    @POST("api/v1/banks/kcb/link")
    suspend fun linkKcbAccount(
        @Body request: LinkBankRequest
    ): Response<BankLinkResponse>

    @POST("api/v1/banks/link")
    suspend fun linkBank(
        @Body request: LinkBankRequest
    ): Response<BankLinkResponse>

    @GET("api/v1/banks")
    suspend fun getSupportedBanks(): Response<List<SupportedBankResponse>>
}
