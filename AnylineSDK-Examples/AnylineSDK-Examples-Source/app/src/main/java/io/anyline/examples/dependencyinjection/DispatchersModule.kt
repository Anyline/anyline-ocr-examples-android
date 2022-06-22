package io.anyline.examples.dependencyinjection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Define different Dispatchers to be injected using Dagger.
 * [@Qualifier] defines the different Dispatchers so Dagger knows which one to use.
 *
 * We do this, because using Dispatchers (i.e. Dispatchers.IO) should not be hardcoded, so
 * they can be injected during Tests.
 *
 * Reference: https://www.valueof.io/blog/injecting-coroutines-dispatchers-with-dagger
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher
