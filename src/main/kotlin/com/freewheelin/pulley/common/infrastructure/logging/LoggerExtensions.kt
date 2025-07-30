package com.freewheelin.pulley.common.infrastructure.logging

import mu.KLogger
import mu.KotlinLogging

/**
 * 클래스에서 간편하게 logger를 사용할 수 있도록 하는 extension property
 */
inline val <reified T : Any> T.logger: KLogger
    get() = KotlinLogging.logger(T::class.java.name)