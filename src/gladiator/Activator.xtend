package gladiator

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.sirolf2009.bitfinex.wss.BitfinexWebsocketClient
import com.sirolf2009.bitfinex.wss.event.OnDisconnected
import com.sirolf2009.bitfinex.wss.event.OnSubscribed
import com.sirolf2009.bitfinex.wss.model.SubscribeOrderbook
import com.sirolf2009.bitfinex.wss.model.SubscribeTrades
import com.sirolf2009.commonwealth.ITick
import com.sirolf2009.commonwealth.Tick
import com.sirolf2009.commonwealth.trading.ITrade
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook
import com.sirolf2009.commonwealth.trading.orderbook.LimitOrder
import com.sirolf2009.commonwealth.trading.orderbook.Orderbook
import com.sirolf2009.gladiator.DataRetriever
import com.sirolf2009.serenity.collector.Collector
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.LinkedList
import java.util.List
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Collectors
import org.apache.commons.io.IOUtils
import org.eclipse.jface.preference.IPreferenceStore
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

class Activator implements BundleActivator {

	private static val shouldReconnect = new AtomicBoolean(true)
	private static val List<ITrade> tickTrades = Collections.synchronizedList(new ArrayList())
	private static val tickOrderbook = new AtomicReference<IOrderbook>()
	private static val isPriming = new AtomicBoolean(true)
	private static val dataQueue = new LinkedList<Callable<Void>>()
	private static val data = new EventBus()
	private static var Activator instance
	private static var BundleContext context
	private static var BitfinexWebsocketClient exchange

	override start(BundleContext bundleContext) throws Exception {
		instance = this
		Activator.context = bundleContext

		val timer = new Timer()
		timer.scheduleAtFixedRate(new TimerTask() {
			override run() {
				new Thread [
					val timestamp = new Date(scheduledExecutionTime)
					val currentTrades = tickTrades.stream.collect(Collectors.toList())
					tickTrades.clear()
					val orderbook = tickOrderbook.get()
					if(orderbook !== null) {
						data.post(new Tick(timestamp, orderbook, currentTrades))
					}
				].start()
			}
		}, getFirstRunTime(), getPeriod())

//		if(preferenceStore.getBoolean("authenticate")) {
//			if(!preferenceStore.getString("username").empty) {
////				val userName = preferenceStore.getString("username")
//			}
//			if(!preferenceStore.getString("apiKey").empty) {
////				val apiKey = preferenceStore.getString("apiKey")
//			}
//			if(!preferenceStore.getString("secretKey").empty) {
////				val secretKey = preferenceStore.getString("secretKey")
//			}
//			// TODO auth
//			connect()
//		} else {
//		}
		new Thread [
			try {
				val retriever = new DataRetriever()
				retriever.getData(new Date(System.currentTimeMillis - Duration.ofHours(2).toMillis()), new Date(System.currentTimeMillis + Duration.ofMinutes(30).toMillis())).forEach [
					trades.forEach [
						data.post(it)
					]
				]
			} catch(Exception e) {
				e.printStackTrace()
			}
			isPriming.set(false)
			dataQueue.forEach[it.call()]
		].start()
		connect()
	}

	def void connect() {
		exchange = new BitfinexWebsocketClient() => [
			eventBus.register(this)
			new Thread [
				connectBlocking()
				send(new SubscribeTrades("BTCUSD"))
				send(new SubscribeOrderbook("BTCUSD", SubscribeOrderbook.PREC_PRECISE, SubscribeOrderbook.FREQ_REALTIME))
			].start()
		]
	}

	@Subscribe
	def void onSubscribed(OnSubscribed onSubscribed) {
		onSubscribed.eventBus.register(this)
	}

	@Subscribe
	def void onDisconnected(OnDisconnected onDisconnected) {
		if(shouldReconnect.get()) {
//			log.log(new Status(IStatus.WARNING, "serenity", "Disconnected from bitfinex. Reconnecting..."))
			connect()
		} else {
//			log.log(new Status(IStatus.INFO, "serenity", "Disconnected from bitfinex."))
		}
	}

	@Subscribe
	def void onTrade(ITrade trade) {
		if(!isPriming.get()) {
			try {
				data.post(trade)
				tickTrades.add(trade)
			} catch(Exception e) {
				e.printStackTrace()
			}
		} else {
			dataQueue.add(new Callable<Void>() {
				override call() throws Exception {
					try {
						data.post(trade)
						tickTrades.add(trade)
					} catch(Exception e) {
						e.printStackTrace()
					}
					return null
				}
			})
		}
	}

	@Subscribe
	def void onOrderbook(IOrderbook orderbook) {
		if(!isPriming.get()) {
			try {
				data.post(orderbook)
				tickOrderbook.set(orderbook)
			} catch(Exception e) {
				e.printStackTrace()
			}

			dataQueue.add(new Callable<Void>() {
				override call() throws Exception {
					try {
						data.post(orderbook)
						tickOrderbook.set(orderbook)
					} catch(Exception e) {
						e.printStackTrace()
					}
					return null
				}
			})
		}
	}

	override stop(BundleContext bundleContext) throws Exception {
		Activator.context = null
		shouldReconnect.set(false)
		exchange?.close()
		exchange = null
	}

	def static getConfiguration(IPreferenceStore preferences, String name, String defaultValue) {
		val conf = preferences.getString(name)
		if(conf.empty) {
			return defaultValue
		} else {
			return conf
		}
	}

	def getFirstRunTime() {
		val cal = Calendar.getInstance()
		cal.set(Calendar.MILLISECOND, 0)
		cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 1)
		return cal.time
	}

	def getPeriod() {
		return Duration.ofSeconds(1).toMillis()
	}

	def static BundleContext getContext() {
		return context
	}

	def static getData() {
		return data
	}

	def static getDefault() {
		return instance
	}

	def static getOrderbooksFromApi(String host) {
		val url = new URL("http", host, "/orderbook")
		val gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create()
		val array = gson.fromJson(IOUtils.toString(url, Charset.defaultCharset), JsonArray)
		array.map [
			val orderbook = asJsonObject
			val date = gson.fromJson(orderbook.get("timestamp"), Date)
			val asks = orderbook.getAsJsonArray("asks").map [
				val price = asJsonObject.getAsJsonPrimitive("price").asDouble
				val amount = asJsonObject.getAsJsonPrimitive("amount").asDouble
				return new LimitOrder(price, amount) as ILimitOrder
			].sortBy[price.doubleValue].toList()
			val bids = orderbook.getAsJsonArray("bids").map [
				val price = asJsonObject.getAsJsonPrimitive("price").asDouble
				val amount = asJsonObject.getAsJsonPrimitive("amount").asDouble
				return new LimitOrder(price, amount) as ILimitOrder
			].sortBy[price.doubleValue].reverse().toList()
			return new Orderbook(date, asks, bids) as IOrderbook
		].sortBy[timestamp]
	}

	def static getOrderbookData() {
		val linecounter = new AtomicLong(-1)
		Files.readAllLines(Paths.get("/home/sirolf2009/git/serenity/orderbook.csv")).map[split(",")].map [
			try {
				linecounter.incrementAndGet()
				val orders = (1 ..< size()).map [ i |
					try {
						val data = get(i).split(":")
						val price = Double.parseDouble(data.get(1))
						val amount = Double.parseDouble(data.get(2))
						data.get(0) -> new LimitOrder(price, amount) as ILimitOrder
					} catch(Exception e) {
						throw new RuntimeException("Failed to parse column " + i + ": " + get(i), e)
					}
				].groupBy[key]
				new Orderbook(new Date(Long.parseLong(get(0))), orders.get("ask").map [
					value
				].sortBy[price.doubleValue()].toList(), orders.get("bid").map [
					value
				].sortBy[price.doubleValue()].toList()) as IOrderbook
			} catch(Exception e) {
				throw new RuntimeException("Failed to parse line " + linecounter.get() + " " + it.toList(), e)
			}
		]
	}

	def static getSerenityData(Consumer<ITick> tickConsumer) {
		Collector.getData("serenity", new Date(System.currentTimeMillis() - Duration.ofMinutes(15).toMillis()), new Date(), tickConsumer)
	}

}
