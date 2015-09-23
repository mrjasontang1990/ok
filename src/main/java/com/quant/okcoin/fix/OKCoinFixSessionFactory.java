package com.quant.okcoin.fix;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.Field;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.StringField;
import quickfix.UnsupportedMessageType;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequestReject;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderStatusRequest;
import quickfix.fix44.Quote;
import quickfix.fix44.QuoteCancel;
import quickfix.fix44.QuoteRequest;
import quickfix.fix44.QuoteRequestReject;
import quickfix.fix44.QuoteResponse;

import com.google.common.collect.Lists;
import com.quant.core.api.Contract;
import com.quant.core.api.MDEntry;
import com.quant.core.api.impl.CommonMDEntry;
import com.quant.core.connection.Session;
import com.quant.core.connection.impl.CommonQuantSessionFactory;

public class OKCoinFixSessionFactory extends CommonQuantSessionFactory {

	private static final Logger logger = Logger.getLogger(OKCoinFixSessionFactory.class);

	private OKCoinFixSession session;

	public Session openSession() throws Exception {
		session = new OKCoinFixSession();
		return session;
	}

}
