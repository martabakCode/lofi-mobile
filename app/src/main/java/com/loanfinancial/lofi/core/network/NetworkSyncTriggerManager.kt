package com.loanfinancial.lofi.core.network

import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.repository.INotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkSyncTriggerManager {
    fun startMonitoring()
}

@Singleton
class NetworkSyncTriggerManagerImpl @Inject constructor(
    private val networkManager: NetworkManager,
    private val loanSubmissionManager: LoanSubmissionManager,
    private val notificationRepository: INotificationRepository,
) : NetworkSyncTriggerManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startMonitoring() {
        scope.launch {
            networkManager.observeNetworkState().collect { isConnected ->
                if (isConnected) {
                    triggerSync()
                }
            }
        }
    }

    private suspend fun triggerSync() {
        // Trigger loan submission retry/sync
        loanSubmissionManager.triggerPendingSubmissions()
        
        // Trigger notification sync
        notificationRepository.syncNotifications()
        
        // Add other sync tasks here (e.g., document uploads might be handled by WorkManager automatically if constrained by network, 
        // but if we have manual retry logic, we could trigger it here)
    }
}
