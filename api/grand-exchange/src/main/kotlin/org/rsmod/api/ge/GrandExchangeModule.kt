package org.rsmod.api.ge

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.rsmod.api.market.MarketPrices
import org.rsmod.module.ExtendedModule
import org.rsmod.server.services.Service

public object GrandExchangeModule : ExtendedModule() {
    override fun bind() {
        bind(GeClock::class.java).toInstance(SystemGeClock)
        bind(GePersistence::class.java).to(AccountGePersistence::class.java).`in`(Scopes.SINGLETON)
        bindBaseAndImpl<GrandExchange>(GrandExchangeImpl::class.java)
        bindBaseInstance<MarketPrices>(GeMarketPrices::class.java)
        addSetBinding<Service>(GeService::class.java)
    }
}

/**
 * For integration tests: the exchange with no database, used as a child module of
 * `runInjectedGameTest`. Bind your own [GeClock] or [GePersistence] by overriding with a module of
 * your own instead if a test needs them.
 */
public open class GrandExchangeTestModule(
    private val clock: GeClock = SystemGeClock,
    private val persistence: GePersistence = NoopGePersistence,
) : AbstractModule() {
    override fun configure() {
        bind(GeClock::class.java).toInstance(clock)
        bind(GePersistence::class.java).toInstance(persistence)
        bind(GrandExchangeImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GrandExchange::class.java).to(GrandExchangeImpl::class.java)
    }
}
