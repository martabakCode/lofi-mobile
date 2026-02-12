package com.loanfinancial.lofi.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lofi_prefs")

@Singleton
class DataStoreManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.dataStore

        companion object {
            const val KEY_ACCESS_TOKEN = "access_token"
            const val KEY_REFRESH_TOKEN = "refresh_token"
            const val KEY_USER_ID = "user_id"
            const val KEY_USER_EMAIL = "user_email"
            const val KEY_USER_NAME = "user_name"
            const val KEY_IS_LOGGED_IN = "is_logged_in"
            const val KEY_FCM_TOKEN = "fcm_token"
            const val KEY_USER_PHONE = "user_phone"
            const val KEY_USER_AVATAR = "user_avatar"
            const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

            private val PREF_ACCESS_TOKEN = stringPreferencesKey(KEY_ACCESS_TOKEN)
            private val PREF_REFRESH_TOKEN = stringPreferencesKey(KEY_REFRESH_TOKEN)
            private val PREF_USER_ID = stringPreferencesKey(KEY_USER_ID)
            private val PREF_USER_EMAIL = stringPreferencesKey(KEY_USER_EMAIL)
            private val PREF_USER_NAME = stringPreferencesKey(KEY_USER_NAME)
            private val PREF_IS_LOGGED_IN = booleanPreferencesKey(KEY_IS_LOGGED_IN)
            private val PREF_FCM_TOKEN = stringPreferencesKey(KEY_FCM_TOKEN)
            private val PREF_USER_PHONE = stringPreferencesKey(KEY_USER_PHONE)
            private val PREF_USER_AVATAR = stringPreferencesKey(KEY_USER_AVATAR)
            private val PREF_BIOMETRIC_ENABLED = booleanPreferencesKey(KEY_BIOMETRIC_ENABLED)
            const val KEY_IS_PIN_SET = "is_pin_set"
            const val KEY_IS_PROFILE_COMPLETED = "is_profile_completed"
            const val KEY_IS_FIRST_INSTALL = "is_first_install"
            const val KEY_HAS_COMPLETED_FIRST_SESSION = "has_completed_first_session"

            private val PREF_IS_PIN_SET = booleanPreferencesKey(KEY_IS_PIN_SET)
            private val PREF_IS_PROFILE_COMPLETED = booleanPreferencesKey(KEY_IS_PROFILE_COMPLETED)
            private val PREF_IS_FIRST_INSTALL = booleanPreferencesKey(KEY_IS_FIRST_INSTALL)
            private val PREF_HAS_COMPLETED_FIRST_SESSION = booleanPreferencesKey(KEY_HAS_COMPLETED_FIRST_SESSION)
        }

        suspend fun saveAuthTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            dataStore.edit { preferences ->
                preferences[PREF_ACCESS_TOKEN] = accessToken
                preferences[PREF_REFRESH_TOKEN] = refreshToken
                preferences[PREF_IS_LOGGED_IN] = true
            }
        }

        suspend fun saveProfileStatus(
            pinSet: Boolean,
            profileCompleted: Boolean,
        ) {
            dataStore.edit { preferences ->
                preferences[PREF_IS_PIN_SET] = pinSet
                preferences[PREF_IS_PROFILE_COMPLETED] = profileCompleted
            }
        }

        suspend fun setPinSet(isSet: Boolean) {
            dataStore.edit { preferences ->
                preferences[PREF_IS_PIN_SET] = isSet
            }
        }

        suspend fun setProfileCompleted(isCompleted: Boolean) {
            dataStore.edit { preferences ->
                preferences[PREF_IS_PROFILE_COMPLETED] = isCompleted
            }
        }

        suspend fun saveUserInfo(
            userId: String,
            email: String,
            name: String,
            phone: String? = null,
            avatar: String? = null,
        ) {
            dataStore.edit { preferences ->
                preferences[PREF_USER_ID] = userId
                preferences[PREF_USER_EMAIL] = email
                preferences[PREF_USER_NAME] = name
                phone?.let { preferences[PREF_USER_PHONE] = it }
                avatar?.let { preferences[PREF_USER_AVATAR] = it }
            }
        }

        suspend fun saveFcmToken(token: String) {
            dataStore.edit { preferences ->
                preferences[PREF_FCM_TOKEN] = token
            }
        }

        suspend fun getAccessToken(): String? = dataStore.data.map { it[PREF_ACCESS_TOKEN] }.first()

        suspend fun getRefreshToken(): String? = dataStore.data.map { it[PREF_REFRESH_TOKEN] }.first()

        suspend fun getUserId(): String? = dataStore.data.map { it[PREF_USER_ID] }.first()

        suspend fun getUserEmail(): String? = dataStore.data.map { it[PREF_USER_EMAIL] }.first()

        suspend fun getUserName(): String? = dataStore.data.map { it[PREF_USER_NAME] }.first()

        suspend fun isLoggedIn(): Boolean = dataStore.data.map { it[PREF_IS_LOGGED_IN] ?: false }.first()

        suspend fun isPinSet(): Boolean = dataStore.data.map { it[PREF_IS_PIN_SET] ?: false }.first()

        suspend fun isProfileCompleted(): Boolean = dataStore.data.map { it[PREF_IS_PROFILE_COMPLETED] ?: false }.first()

        suspend fun getFcmToken(): String? = dataStore.data.map { it[PREF_FCM_TOKEN] }.first()

        suspend fun saveToken(token: String) {
            dataStore.edit { preferences ->
                preferences[PREF_ACCESS_TOKEN] = token
            }
        }

        val tokenFlow: Flow<String?> =
            dataStore.data.map { preferences ->
                preferences[PREF_ACCESS_TOKEN]
            }

        val isLoggedInFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PREF_IS_LOGGED_IN] ?: false
            }

        val isPinSetFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PREF_IS_PIN_SET] ?: false
            }

        val isProfileCompletedFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PREF_IS_PROFILE_COMPLETED] ?: false
            }

        val userInfoFlow: Flow<UserInfo> =
            dataStore.data.map { preferences ->
                UserInfo(
                    userId = preferences[PREF_USER_ID] ?: "",
                    email = preferences[PREF_USER_EMAIL] ?: "",
                    name = preferences[PREF_USER_NAME] ?: "",
                    phone = preferences[PREF_USER_PHONE],
                    avatar = preferences[PREF_USER_AVATAR],
                )
            }

        suspend fun setBiometricEnabled(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[PREF_BIOMETRIC_ENABLED] = enabled
            }
        }

        suspend fun isBiometricEnabled(): Boolean =
            dataStore.data.map { it[PREF_BIOMETRIC_ENABLED] ?: false }.first()

        val biometricEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PREF_BIOMETRIC_ENABLED] ?: false
            }

        val isFirstInstallFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PREF_IS_FIRST_INSTALL] ?: true // Default to true (first install)
            }

        suspend fun setFirstInstall(isFirst: Boolean) {
            dataStore.edit { preferences ->
                preferences[PREF_IS_FIRST_INSTALL] = isFirst
            }
        }

        val hasCompletedFirstSessionFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PREF_HAS_COMPLETED_FIRST_SESSION] ?: false
            }

        suspend fun setHasCompletedFirstSession(completed: Boolean) {
            dataStore.edit { preferences ->
                preferences[PREF_HAS_COMPLETED_FIRST_SESSION] = completed
            }
        }

        suspend fun clearAuthData() {
            dataStore.edit { preferences ->
                preferences.remove(PREF_ACCESS_TOKEN)
                preferences.remove(PREF_REFRESH_TOKEN)
                preferences.remove(PREF_USER_ID)
                preferences.remove(PREF_USER_EMAIL)
                preferences.remove(PREF_USER_NAME)
                preferences.remove(PREF_IS_LOGGED_IN)
                preferences.remove(PREF_USER_PHONE)
                preferences.remove(PREF_USER_AVATAR)
                preferences.remove(PREF_BIOMETRIC_ENABLED)
                preferences.remove(PREF_IS_PIN_SET)
                preferences.remove(PREF_IS_PROFILE_COMPLETED)
                preferences.remove(PREF_IS_FIRST_INSTALL)
            }
        }

        suspend fun clear() {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

data class UserInfo(
    val userId: String,
    val email: String,
    val name: String,
    val phone: String? = null,
    val avatar: String? = null,
)
