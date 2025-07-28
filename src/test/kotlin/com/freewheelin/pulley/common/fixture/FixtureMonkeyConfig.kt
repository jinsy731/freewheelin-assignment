package com.freewheelin.pulley.common.fixture

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin

object FixtureMonkeyConfig {
    val fixtureMonkey: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JacksonPlugin())
        .build()
}