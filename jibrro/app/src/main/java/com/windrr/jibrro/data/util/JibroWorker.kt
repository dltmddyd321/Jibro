package com.windrr.jibrro.data.util

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.windrr.jibrro.domain.usecase.RegisterAlarmUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltWorker
class JibroWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val registerAlarmUseCase: RegisterAlarmUseCase
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.i(this::class.simpleName, "백그라운드 작업 수행")
//        CoroutineScope(Dispatchers.IO).launch {
//            registerAlarmUseCase(System.currentTimeMillis())
//        }
        return Result.success()
    }
}