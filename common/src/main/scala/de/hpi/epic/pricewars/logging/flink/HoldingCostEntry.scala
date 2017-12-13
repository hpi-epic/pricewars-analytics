package de.hpi.epic.pricewars.logging.flink

import de.hpi.epic.pricewars.types._


case class HoldingCostEntry (merchant_id: Token, cost: Currency, timestamp: Timestamp)
