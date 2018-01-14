package com.sirolf2009.gladiator

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.sirolf2009.commonwealth.ITick
import com.sirolf2009.commonwealth.Tick
import com.sirolf2009.commonwealth.timeseries.Point
import com.sirolf2009.commonwealth.trading.ITrade
import com.sirolf2009.commonwealth.trading.Trade
import com.sirolf2009.commonwealth.trading.orderbook.ILimitOrder
import com.sirolf2009.commonwealth.trading.orderbook.IOrderbook
import com.sirolf2009.commonwealth.trading.orderbook.LimitOrder
import com.sirolf2009.commonwealth.trading.orderbook.Orderbook
import com.sirolf2009.util.TimeUtil
import java.io.File
import java.time.Duration
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.stream.Collectors
import java.util.Optional
import java.io.BufferedReader
import java.io.InputStreamReader
import com.jcraft.jsch.Session

class DataRetriever implements AutoCloseable {
	
	val JSch jsch
	val Session session
	val ChannelSftp channel

	new() {
		jsch = new JSch()
		jsch.addIdentity("~/.ssh/id_rsa")
		session = jsch.getSession("root", "serenity")
		session.setConfig("StrictHostKeyChecking", "no")
		session.connect()
		channel = session.openChannel("sftp") as ChannelSftp
		channel.connect()
	}

	def getData(Date from, Date to) {
		return TimeUtil.getPointsToDate(from.roundTo15Min, to.roundTo15Min, 15).map[getData].filter[present].map[get].flatten
	}

	def getData(Date timestamp) {
		val ticks = new ArrayList<ITick>()
		val trades = new ArrayList<ITrade>()
		try {
			timestamp.file.getLines.forEach [
				if(startsWith("o")) {
					val orderbook = parseOrderbook
					ticks += new Tick(orderbook.timestamp, orderbook, trades.stream.collect(Collectors.toList()))
					trades.clear()
				} else {
					trades += parseTrade
				}
			]
			return Optional.of(ticks)
		} catch(Exception e) {
			return Optional.empty
		}
	}

	def getLines(File file) {
		val in = channel.get(file.toString())
		val reader = new BufferedReader(new InputStreamReader(in))
		val lines = reader.lines.collect(Collectors.toList())
		reader.close()
		in.close()
		return lines
	}

	def parseOrderbook(String line) {
		try {
			val it = line.split(",")
			val timestamp = new Date(Long.parseLong(get(1)))
			val asks = get(2).split(";").map [
				try {
					val it = split(":")
					return new LimitOrder(Double.parseDouble(get(0)), Double.parseDouble(get(1))) as ILimitOrder
				} catch(Exception e) {
//					log.warning("Failed to parse " + line, e)
					e.printStackTrace()
					return null
				}
			].filter[it !== null].toList()
			val bids = get(3).split(";").map [
				try {
					val it = split(":")
					return new LimitOrder(Double.parseDouble(get(0)), Double.parseDouble(get(1))) as ILimitOrder
				} catch(Exception e) {
//					log.warning("Failed to parse " + line, e)
					e.printStackTrace()
					return null
				}
			].filter[it !== null].toList()
			return new Orderbook(timestamp, asks, bids) as IOrderbook
		} catch(Exception e) {
//			log.warning("Failed to parse " + line, e)
			e.printStackTrace()
		}
	}

	def parseTrade(String line) {
		val it = line.split(",")
		return new Trade(new Point(Long.parseLong(get(1)), Double.parseDouble(get(2))), Double.parseDouble(get(3))) as ITrade
	}

	def getFile(Date timestamp) {
		return timestamp.calendar.file
	}

	def getFile(Calendar cal) {
		val year = new File(new File("/BTCUSD"), cal.get(Calendar.YEAR).toString())
		val month = new File(year, cal.get(Calendar.MONTH).toString())
		val day = new File(month, cal.get(Calendar.DAY_OF_MONTH).toString())
		val hour = new File(day, cal.get(Calendar.HOUR_OF_DAY).toString())
		return new File(hour, TimeUtil.format(cal.time))
	}

	def getCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam")).roundTo15Min
	}

	def roundTo15Min(Date date) {
		return date.calendar.roundTo15Min.time
	}

	def getCalendar(Date date) {
		val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"))
		cal.time = date
		return cal
	}

	def roundTo15Min(Calendar cal) {
		cal.set(Calendar.MILLISECOND, 0)
		cal.set(Calendar.SECOND, 0)
		val minute = cal.minute
		cal.set(Calendar.MINUTE, minute - (minute % 15))
		return cal
	}

	def getMinute(Calendar cal) {
		return cal.get(Calendar.MINUTE)
	}
	
	override close() throws Exception {
		channel.disconnect()
		session.disconnect()
	}

	def static void main(String[] args) {
		val retriever = new DataRetriever()
		retriever.getData(new Date(System.currentTimeMillis - Duration.ofHours(4).toMillis()), new Date()).forEach[
			println(it)
		]
	}

}
