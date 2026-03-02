package com.msaitodev.core.notifications

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    // NotificationPolicy の具体的な実装は、各アプリケーションモジュール（Hub）側で
    // @Provides または @Binds されることを期待します。
}
