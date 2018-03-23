package de.hpi.epic.pricewars.logging.marketplace

import de.hpi.epic.pricewars.types.{Amount, Timestamp, Token}

case class InventoryLevel(merchant_id: Token, level: Amount, timestamp: Timestamp)

