package com.quant.okcoin.fix;

import com.quant.core.api.MDEntryAction;
import com.quant.core.api.MDEntryType;
import com.quant.core.api.OrderRejectReason;
import com.quant.core.api.OrderSide;
import com.quant.core.api.OrderStatus;


public class OKCoinFixUtil {
	
	public static final int EXECUTIONTAG_AXGPX = 6;
	public static final int EXECUTIONTAG_CLORDID = 11;
	public static final int EXECUTIONTAG_CUMQTY =14;
	public static final int EXECUTIONTAG_EXCEID = 17;
	public static final int EXECUTIONTAG_ORDERID = 37;
	public static final int EXECUTIONTAG_ORDERQTY = 38;
	public static final int EXECUTIONTAG_ORDSTATUS = 39;
	public static final int EXECUTIONTAG_ORDPRICE = 44;
	public static final int EXECUTIONTAG_SIDE = 54;
	public static final int EXECUTIONTAG_SYMBOL = 55;
	public static final int EXECUTIONTAG_TEXT = 58;
	public static final int EXECUTIONTAG_TRANSACTTIME = 60;
	public static final int EXECUTIONTAG_ORDERREJREASON = 103;
	public static final int EXECUTIONTAG_EXECTYPE = 150;
	public static final int EXECUTIONTAG_LEAVESQTY = 151;

	
	public static final int ENTRYTAG_TYPE=269;
	public static final int ENTRYTAG_SENDINGTIME = 52;
	public static final int ENTRYTAG_SEQ = 34;
	
	public static final int TAG_ENTRY_TYPE = 269;
	public static final int TAG_ENTRY_PRICE = 270;
	public static final int TAG_ENTRY_AMOUNT = 271;
	public static final int TAG_ENTRY_ACTION = 279;
	
	public static final int TAG_ACCOUNT_APIKEY = 1;
	public static final int TAG_ACCOUNT_CURRENCY = 15;
	public static final int TAG_ACCOUNT_ACCREQID = 8000;
	public static final int TAG_ACCOUNT_BALANCE = 8001;
	public static final int TAG_ACCOUNT_FREEBTC = 8101;
	public static final int TAG_ACCOUNT_FREELTC = 8102;
	public static final int TAG_ACCOUNT_FREEUSD = 8103;
	public static final int TAG_ACCOUNT_FROZENBTC = 8104;
	public static final int TAG_ACCOUNT_FROZENLTC = 8105;
	public static final int TAG_ACCOUNT_FROZENUSD = 8106;
	
	public static final int TAG_CANCEL_REJECT_REASON = 279;
	
	public static final int TAG_CANCEL_REJECT_RESPONSE_TO = 434;
	
	public static String convertMDEntryTypeForBidAsk(String value) {
		if(value.equals(OKCoinFixMDEntryType.BID))
			return MDEntryType.BID;
		if(value.equals(OKCoinFixMDEntryType.ASK))
			return MDEntryType.ASK;
		return null;
	}
	
	public static String convertOrderSide(String value) {
		if(OKCoinFixOrderSide.BUY.equals(value)) {
			return OrderSide.LONG;
		} else
			return OrderSide.SHORT;
	}
	
	public static String convertOrderStatus(String value) {
		if(value.equals(OKCoinFixOrderStatus.NEW))
			return OrderStatus.SUBMITTED;
		if(value.equals(OKCoinFixOrderStatus.CANCELED))
			return OrderStatus.CANCELLED;
		if(value.equals(OKCoinFixOrderStatus.FILL))
			return OrderStatus.COMPLETED;
		if(value.equals(OKCoinFixOrderStatus.REJECTED))
			return OrderStatus.REJECTED;
		if(value.equals(OKCoinFixOrderStatus.PARTIAL_FILL)) 
			return OrderStatus.PARTIAL_COMPLETED;
		return null;
	}
	
	public static String convertMDEntrySideForTrade(String value) {
		if(value.equals(OKCoinFixOrderSide.BUY)) {
			return MDEntryType.BUY;
		}
		if(value.equals(OKCoinFixOrderSide.SELL)) {
			return MDEntryType.SELL;
		}
		return null;
	}
	
	public static String convertRejectReason(String value) {
		if(value.equals(OKCoinFixOrderRejectReason.DUPLICATE_ORDER))
			return OrderRejectReason.DUPLICATE_ORDER;
		if(value.equals(OKCoinFixOrderRejectReason.INCORRECT_QUANTITY))
			return OrderRejectReason.INCORRECT_AMOUNT;
		if(value.equals(OKCoinFixOrderRejectReason.ORDER_EXCEEDS_LIMIT))
			return OrderRejectReason.ORDER_EXCEEDS_LIMIT;
		if(value.equals(OKCoinFixOrderRejectReason.UNKNOWN_SYMBOL))
			return OrderRejectReason.UNKNOWN_SYMBOL;
		return OrderRejectReason.OTHERS;
	}
	
	
	public static String convertMDEntryAction(String value) {
		if(value.equals(OKCoinFixMDEntryAction.NEW))
			return MDEntryAction.NEW;
		if(value.equals(OKCoinFixMDEntryAction.CHANGE))
			return MDEntryAction.CHANGE;
		if(value.equals(OKCoinFixMDEntryAction.DELETE)) {
			return MDEntryAction.DELETE;
		}
		return null;
	}
}
