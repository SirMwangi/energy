package com.example.energy.data.remote.datasource

import com.example.energy.data.remote.dto.AccountDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class AccountRemoteDataSource(private val client: SupabaseClient) {

    suspend fun fetchAccounts(userId: String): List<AccountDto> {
        return client.postgrest["accounts"].select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<AccountDto>()
    }

    suspend fun createAccount(account: AccountDto): AccountDto {
        return client.postgrest["accounts"].insert(account) {
            select()
        }.decodeSingle<AccountDto>()
    }
}
