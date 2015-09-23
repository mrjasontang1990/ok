package com.quant.okcoin.fix;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.Field;
import quickfix.FieldNotFound;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.Group;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.StringField;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.Advertisement;
import quickfix.fix44.AllocationInstruction;
import quickfix.fix44.AllocationInstructionAck;
import quickfix.fix44.AllocationReport;
import quickfix.fix44.AllocationReportAck;
import quickfix.fix44.AssignmentReport;
import quickfix.fix44.BidRequest;
import quickfix.fix44.BidResponse;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.CollateralAssignment;
import quickfix.fix44.CollateralInquiry;
import quickfix.fix44.CollateralInquiryAck;
import quickfix.fix44.CollateralReport;
import quickfix.fix44.CollateralRequest;
import quickfix.fix44.CollateralResponse;
import quickfix.fix44.Confirmation;
import quickfix.fix44.ConfirmationAck;
import quickfix.fix44.ConfirmationRequest;
import quickfix.fix44.CrossOrderCancelReplaceRequest;
import quickfix.fix44.CrossOrderCancelRequest;
import quickfix.fix44.DerivativeSecurityList;
import quickfix.fix44.DerivativeSecurityListRequest;
import quickfix.fix44.DontKnowTrade;
import quickfix.fix44.Email;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.IndicationOfInterest;
import quickfix.fix44.ListCancelRequest;
import quickfix.fix44.ListExecute;
import quickfix.fix44.ListStatus;
import quickfix.fix44.ListStatusRequest;
import quickfix.fix44.ListStrikePrice;
import quickfix.fix44.Logon;
import quickfix.fix44.Logout;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataRequestReject;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.MassQuote;
import quickfix.fix44.MassQuoteAcknowledgement;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.MultilegOrderCancelReplaceRequest;
import quickfix.fix44.NetworkStatusRequest;
import quickfix.fix44.NetworkStatusResponse;
import quickfix.fix44.NewOrderCross;
import quickfix.fix44.NewOrderList;
import quickfix.fix44.NewOrderMultileg;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.News;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderMassCancelReport;
import quickfix.fix44.OrderMassCancelRequest;
import quickfix.fix44.OrderMassStatusRequest;
import quickfix.fix44.OrderStatusRequest;
import quickfix.fix44.PositionMaintenanceReport;
import quickfix.fix44.PositionMaintenanceRequest;
import quickfix.fix44.PositionReport;
import quickfix.fix44.Quote;
import quickfix.fix44.QuoteCancel;
import quickfix.fix44.QuoteRequest;
import quickfix.fix44.QuoteRequestReject;
import quickfix.fix44.QuoteResponse;
import quickfix.fix44.QuoteStatusReport;
import quickfix.fix44.QuoteStatusRequest;
import quickfix.fix44.RFQRequest;
import quickfix.fix44.RegistrationInstructions;
import quickfix.fix44.RegistrationInstructionsResponse;
import quickfix.fix44.Reject;
import quickfix.fix44.RequestForPositions;
import quickfix.fix44.RequestForPositionsAck;
import quickfix.fix44.ResendRequest;
import quickfix.fix44.SecurityDefinition;
import quickfix.fix44.SecurityDefinitionRequest;
import quickfix.fix44.SecurityList;
import quickfix.fix44.SecurityListRequest;
import quickfix.fix44.SecurityStatus;
import quickfix.fix44.SecurityStatusRequest;
import quickfix.fix44.SecurityTypeRequest;
import quickfix.fix44.SecurityTypes;
import quickfix.fix44.SequenceReset;
import quickfix.fix44.SettlementInstructionRequest;
import quickfix.fix44.SettlementInstructions;
import quickfix.fix44.TestRequest;
import quickfix.fix44.TradeCaptureReport;
import quickfix.fix44.TradeCaptureReportAck;
import quickfix.fix44.TradeCaptureReportRequest;
import quickfix.fix44.TradeCaptureReportRequestAck;
import quickfix.fix44.TradingSessionStatus;
import quickfix.fix44.TradingSessionStatusRequest;
import quickfix.fix44.UserRequest;
import quickfix.fix44.UserResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quant.core.api.Account;
import com.quant.core.api.Contract;
import com.quant.core.api.Currency;
import com.quant.core.api.Execution;
import com.quant.core.api.MDEntry;
import com.quant.core.api.Order;
import com.quant.core.api.OrderStatus;
import com.quant.core.api.impl.CommonExecution;
import com.quant.core.api.impl.CommonMDEntry;
import com.quant.core.connection.Session;
import com.quant.core.fixapi.FixExecutionReportTag;
import com.quant.core.fixapi.FixMarketDataIncrementalRefreshTag;
import com.quant.core.fixapi.FixMarketDataSnapshotFullRefreshTag;
import com.quant.core.fixapi.FixOrderCancelRejectTag;
import com.quant.core.fixapi.FixOrderSide;
import com.quant.core.fixapi.FixUtil;

public class OKCoinFixSession extends Session implements Application {

	private static final String MSG_RESP_ACCOUNT = "Z1001";

	private static final Logger logger = Logger.getLogger(OKCoinFixSession.class);

	private Initiator initiator;

	private SessionID sessionID;

	private Map<String, Contract> requestedContracts = Maps.newConcurrentMap();

	private OKCoinMessageCracker cracker;

	private long randSeq = System.currentTimeMillis();

	private Object locker = new Object();

	public OKCoinFixSession() {
		this.cracker = new OKCoinMessageCracker(this);
	}

	public Contract getRequestedContract(String contractName) {
		return requestedContracts.get(contractName);
	}

	private void addRequestedContract(Contract contract) {
		requestedContracts.put(contract.getContractName(), contract);
	}

	public void setSessionID(SessionID sessionID) {
		this.sessionID = sessionID;
	}

	public boolean login(String requestID, Account account) {
		if (isLogin(account)) {
			logger.warn("login: " + "Already login account - " + account);
			return true;
		}
		try {
			logger.warn("login: " + account);
			this.account = account;
			InputStream inputStream = OKCoinFixSession.class.getResourceAsStream("/okcoin/quickfix-client.properties");
			SessionSettings settings = new SessionSettings(inputStream);
			MessageStoreFactory storeFactory = new FileStoreFactory(settings);
			LogFactory logFactory = new FileLogFactory(settings);
			MessageFactory messageFactory = new DefaultMessageFactory();
			initiator = new SocketInitiator(this, storeFactory, settings, logFactory, messageFactory);
			initiator.start();
		} catch (Exception ex) {
			logger.error(ex);
			return false;
		}
		return true;
	}

	public boolean logout(Account account) {
		// TODO Auto-generated method stub
		if (!isLogin(account)) {
			logger.warn("login: " + "Already logout account - " + account);
			return true;
		}
		initiator.stop();
		return true;
	}

	public boolean requestMarketDepth(String requestID, Contract contract, int depth, boolean subscribe) {
		if (!isLogin(account)) {
			logger.warn("requestMarketDepth: " + "Not login.");
			return false;
		}

		logger.warn("requestMarketDepth: " + contract);
		addRequestedContract(contract);
		quickfix.fix44.MarketDataRequest orderBookRequest = new quickfix.fix44.MarketDataRequest();
		quickfix.fix44.MarketDataRequest.NoRelatedSym noRelatedSym = new quickfix.fix44.MarketDataRequest.NoRelatedSym();
		noRelatedSym.set(new Symbol(contract.getContractName()));
		orderBookRequest.addGroup(noRelatedSym);

		orderBookRequest.set(new MDReqID(requestID));
		orderBookRequest.set(new SubscriptionRequestType('1'));
		orderBookRequest.set(new MDUpdateType(1));
		orderBookRequest.set(new MarketDepth(depth));

		quickfix.fix44.MarketDataRequest.NoMDEntryTypes group1 = new quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
		group1.set(new MDEntryType('0'));
		orderBookRequest.addGroup(group1);
		quickfix.fix44.MarketDataRequest.NoMDEntryTypes group2 = new quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
		group2.set(new MDEntryType('1'));
		orderBookRequest.addGroup(group2);
		quickfix.Session.lookupSession(sessionID).send(orderBookRequest);
		//
		// quickfix.fix44.MarketDataRequest liveTradesRequest = new
		// quickfix.fix44.MarketDataRequest();
		// noRelatedSym = new
		// quickfix.fix44.MarketDataRequest.NoRelatedSym();
		// noRelatedSym.set(new Symbol(contract.getContractName()));
		// liveTradesRequest.addGroup(noRelatedSym);
		// liveTradesRequest.set(new MDReqID(requestID));
		// liveTradesRequest.set(new SubscriptionRequestType('1'));
		// liveTradesRequest.set(new MarketDepth(depth));
		// quickfix.fix44.MarketDataRequest.NoMDEntryTypes group = new
		// quickfix.fix44.MarketDataRequest.NoMDEntryTypes();
		// group.set(new MDEntryType('2'));
		// liveTradesRequest.addGroup(group);
		//
		// quickfix.Session.lookupSession(sessionID).send(liveTradesRequest);
		return true;
	}

	public boolean cancelMarketDepth(String requestID) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean requestMarketTick(String requestID, Contract contract, boolean subscribe) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean cancelMarketTick(String requestID) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean requestAccountInfo(String requestID) {
		// TODO Auto-generated method stub
		if (!isLogin(account)) {
			logger.warn("requestMarketDepth: " + "Not login.");
			return false;
		}
		logger.warn("requestAccountInfo");
		quickfix.Message accountInfoRequest = new quickfix.Message();
		accountInfoRequest.getHeader().setField(new quickfix.field.MsgType("Z1000"));
		accountInfoRequest.setField(new quickfix.field.Account(getAccount().getAccountAccessKey() + ","
				+ getAccount().getAccountSecretKey()));
		accountInfoRequest.setField(new AccReqID(requestID));
		quickfix.Session.lookupSession(sessionID).send(accountInfoRequest);
		return true;
	}

	public boolean requestOrders(String requestID, Contract contract) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean sendOrder(String requestID, Order order) {

		if (!isLogin(account)) {
			logger.warn("sendOrder: " + "Not login.");
			return false;
		}
		logger.warn("sendOrder: " + order);
		order.setOrderStatus(OrderStatus.PENDING_SUBMIT);
		quickfix.fix44.NewOrderSingle newOrderSingleRequest = new quickfix.fix44.NewOrderSingle();
		newOrderSingleRequest.set(new quickfix.field.Account(getAccount().getAccountAccessKey() + ","
				+ getAccount().getAccountSecretKey()));
		newOrderSingleRequest.set(new ClOrdID(order.getCustomerOrderID()));
		newOrderSingleRequest.set(new OrderQty(order.getOrderAmount()));
		newOrderSingleRequest.set(new OrdType('2'));
		newOrderSingleRequest.set(new Price(order.getOrderPrice()));
		newOrderSingleRequest.set(new Side(FixOrderSide.valueOf(order.getOrderSide()).toCharArray()[0]));
		newOrderSingleRequest.set(new Symbol(order.getContract().getContractName()));
		newOrderSingleRequest.set(new TransactTime());
		quickfix.Session.lookupSession(sessionID).send(newOrderSingleRequest);
		return true;
	}

	public boolean requestOrderInfo(String requestID, Order order) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean cancelOrder(String requestID, Order order) {
		if (!isLogin(account)) {
			logger.warn("sendOrder: " + "Not login.");
			return false;
		}
		logger.warn("cancelOrder: " + order);
		order.setOrderStatus(OrderStatus.PENDING_CANCEL);
		quickfix.fix44.OrderCancelRequest OrderCancelRequest = new quickfix.fix44.OrderCancelRequest();
		OrderCancelRequest.set(new ClOrdID(requestID));
		OrderCancelRequest.set(new OrigClOrdID(order.getOrderID()));
		OrderCancelRequest.set(new Side(FixOrderSide.valueOf(order.getOrderSide()).toCharArray()[0]));
		OrderCancelRequest.set(new Symbol(order.getContract().getContractName()));
		OrderCancelRequest.set(new TransactTime(new Date()));
		quickfix.Session.lookupSession(sessionID).send(OrderCancelRequest);
		return false;
	}

	public boolean modifyOrder(String requestID, Order order) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLogin(Account account) {
		// TODO Auto-generated method stub
		if (sessionID != null && account != null && account.equals(this.account)) {
			return initiator == null ? false : initiator.isLoggedOn();
		}
		return false;
	}

	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
		logger.info("fromAdmin" + arg0);
	}

	public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
		logger.info("fromApp" + arg0);

		try {
			cracker.crack(arg0, arg1);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	public void onCreate(SessionID arg0) {
		logger.info("onCreate" + arg0);
	}

	public void onLogon(SessionID arg0) {
		logger.warn("onLogon: " + arg0);
		synchronized (locker) {
			this.sessionID = arg0;
		}
		this.fireOnConnected(this);
	}

	public void onLogout(SessionID arg0) {
		logger.warn("onLogout: " + arg0);
		this.fireOnDisconnected(this);
	}

	public void toAdmin(Message arg0, SessionID arg1) {
		logger.info("toAdmin" + arg0);
		arg0.setField(new StringField(553, this.getAccount().getAccountAccessKey()));
		arg0.setField(new StringField(554, this.getAccount().getAccountSecretKey()));
	}

	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		logger.info("toApp" + arg0);

	}

	protected class OKCoinMessageCracker extends MessageCracker {

		private OKCoinFixSession session;

		public OKCoinMessageCracker(OKCoinFixSession session) {
			this.session = session;
		}

		@Override
		public void onMessage(MarketDataIncrementalRefresh message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			// logger.info("onMessageMarketDataIncrementalRefresh: " + message);
			Iterator<Field<?>> messageIterator = message.iterator();
			List<MDEntry> entryLists = Lists.newArrayList();
			Contract contract = null;
			long sendingTime = message.getHeader().getUtcTimeStamp(OKCoinFixUtil.ENTRYTAG_SENDINGTIME).getTime();
			// logger.info("sending time: " + sendingTime);
			while (messageIterator.hasNext()) {
				Field messageField = messageIterator.next();
				List<Group> groups = message.getGroups(messageField.getField());
				switch (messageField.getField()) {
				case 55:
					contract = session.getRequestedContract((String) messageField.getObject());
					break;
				case 5:
					break;
				}
				for (Group group : groups) {
					Iterator groupIterator = group.iterator();
					MDEntry entry = new CommonMDEntry();
					while (groupIterator.hasNext()) {
						Field field = (Field) groupIterator.next();
						switch (field.getTag()) {
						case FixMarketDataIncrementalRefreshTag.MDENTRYTYPE:
							entry.setType(FixUtil.convertMDEntryTypeForBidAsk((String) field.getObject()));
							break;
						case FixMarketDataIncrementalRefreshTag.MDENTRYPX:
							entry.setPrice(Double.valueOf((String) field.getObject()));
							break;
						case FixMarketDataIncrementalRefreshTag.MDENTRYSIZE:
							entry.setAmount(Double.valueOf((String) field.getObject()));
							break;
						case FixMarketDataIncrementalRefreshTag.MDUPDATEACTION:
							entry.setAction(FixUtil.convertMDEntryAction((String) field.getObject()));
							break;
						default:
							logger.warn("onMessage: field tag not found " + message);
						}
					}
					entryLists.add(entry);
				}

			}
			long time = System.currentTimeMillis();
			for (MDEntry item : entryLists) {
				item.setDepth(10);
				item.setContractID(contract.getContractID());
				item.setContract(contract);
				item.setLocalTime(time);
				item.setSeqNum(randSeq);
				item.setServerTime(sendingTime);
				// this.session.fireOnMarketMDEntryUpdate("", item);
			}

			this.session.fireOnMarketMDEntryUpdate("", entryLists);
			// logger.info("EntryList parsed. Time Used: " +
			// (System.currentTimeMillis() - t1));
		}

		@Override
		public void onMessage(MarketDataRequestReject message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageMarketDataRequestReject: " + message);
		}

		@Override
		public void onMessage(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageOrderCancelRequest: " + message);
			/*
			 * Iterator<Field<?>> messageIterator = message.iterator(); long
			 * sendingTime = message.getHeader().getUtcTimeStamp(OKCoinFixUtil.
			 * ENTRYTAG_SENDINGTIME).getTime(); Execution execution = new
			 * CommonExecution(); while (messageIterator.hasNext()) { Field
			 * messageField = messageIterator.next(); List<Group> groups =
			 * message.getGroups(messageField.getField()); switch
			 * (messageField.getField()) { case
			 * OKCoinFixUtil.EXECUTIONTAG_CLORDID:
			 * execution.setCustomerOrderID(messageField
			 * .getObject().toString()); break; case
			 * OKCoinFixUtil.EXECUTIONTAG_EXCEID:
			 * execution.setExecutionID(messageField.getObject().toString());
			 * break; case OKCoinFixUtil.EXECUTIONTAG_ORDERID:
			 * execution.setOrderID(messageField.getObject().toString()); break;
			 * case OKCoinFixUtil.EXECUTIONTAG_ORDSTATUS:
			 * execution.setOrderStatus
			 * (OKCoinFixUtil.convertOrderStatus(messageField
			 * .getObject().toString())); break; case
			 * OKCoinFixUtil.EXECUTIONTAG_SIDE:
			 * execution.setOrderSide(OKCoinFixUtil
			 * .convertOrderSide(messageField.getObject().toString())); break;
			 * case OKCoinFixUtil.EXECUTIONTAG_TEXT:
			 * execution.setText(messageField.getObject().toString()); break;
			 * //case OKCoinFixUtil.EXECUTIONTAG_TRANSACTTIME: //
			 * execution.setTransactTime
			 * (Long.valueOf(messageField.getObject().toString())); // break;
			 * case OKCoinFixUtil.EXECUTIONTAG_TRANSACTTIME:
			 * execution.setTransactTime
			 * (Long.valueOf(messageField.getObject().toString())); break;
			 * default: logger.warn("onMessageExecutionReport: No such field: "
			 * + messageField); break; } } session.fireOnExecution(execution);
			 */
		}

		@Override
		public void onMessage(Heartbeat message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.warn("onMessageHeartbeat: " + message);
		}

		@Override
		public void onMessage(Logon message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.warn("onMessageLogon: " + message);
		}

		@Override
		public void onMessage(Logout message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.warn("onMessageLogout: " + message);
		}

		@Override
		public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageMarketDataRequest: " + message);
		}

		@Override
		public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageMarketDataSnapshotFullRefresh: " + message);
			String symbol = message.getSymbol().getObject();
			Iterator<Field<?>> messageIterator = message.iterator();
			List<MDEntry> entryLists = Lists.newArrayList();
			Contract contract = null;
			contract = session.getRequestedContract(symbol);
			// int seq = message.getHeader().getInt(OKCoinFixUtil.ENTRYTAG_SEQ);
			long sendingTime = message.getHeader().getUtcTimeStamp(OKCoinFixUtil.ENTRYTAG_SENDINGTIME).getTime();
			// logger.info("sending time: " + sendingTime);
			while (messageIterator.hasNext()) {
				Field messageField = messageIterator.next();
				List<Group> groups = message.getGroups(messageField.getField());
				switch (messageField.getField()) {
				case FixMarketDataSnapshotFullRefreshTag.SYMBOL:
					contract = session.getRequestedContract((String) messageField.getObject());
					break;
				case 5:
					break;
				default:
					logger.warn("onMessage: messageField tag not found " + messageField.getTag() + " message: "
							+ message);
					break;
				}
				for (Group group : groups) {
					Iterator groupIterator = group.iterator();
					MDEntry entry = new CommonMDEntry();
					while (groupIterator.hasNext()) {
						Field field = (Field) groupIterator.next();
						switch (field.getTag()) {
						case FixMarketDataSnapshotFullRefreshTag.MDENTRYTYPE:
							entry.setType(FixUtil.convertMDEntryTypeForBidAsk((String) field.getObject()));
							break;
						case FixMarketDataSnapshotFullRefreshTag.MDENTRYPX:
							entry.setPrice(Double.valueOf((String) field.getObject()));
							break;
						case FixMarketDataSnapshotFullRefreshTag.MDENTRYSIZE:
							entry.setAmount(Double.valueOf((String) field.getObject()));
							break;
						case OKCoinFixUtil.EXECUTIONTAG_SIDE:
							entry.setType((FixUtil.convertMDEntrySideForTrade(field.getObject().toString())));
							break;
						default:
							logger.warn("onMessage: field tag not found " + field.getTag() + " message: " + message);
							break;
						}
					}
					entryLists.add(entry);
				}

			}
			long time = System.currentTimeMillis();
			for (MDEntry item : entryLists) {
				item.setDepth(10);
				item.setAction(OKCoinFixUtil.convertMDEntryAction(OKCoinFixMDEntryAction.NEW));
				item.setContractID(contract.getContractID());
				item.setContract(contract);
				item.setLocalTime(time);
				item.setSeqNum(randSeq);
				item.setServerTime(sendingTime);
				// this.session.fireOnMarketMDEntryUpdate("", item);
			}

			this.session.fireOnMarketMDEntryUpdate("", entryLists);
			// logger.info("EntryList parsed. Time Used: " +
			// (System.currentTimeMillis() - t1));
		}

		@Override
		public void onMessage(DontKnowTrade message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.warn("onMessageDontKnowTrade: " + message);
		}

		@Override
		public void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.error("onMessageMessage: " + message);
			MsgType msgType = new MsgType();
			message.getHeader().getField(msgType);
			String msgTypeValue = msgType.getValue();
			if (msgTypeValue.equals(MSG_RESP_ACCOUNT)) {
				Iterator<Field<?>> messageIterator = message.iterator();
				long sendingTime = message.getHeader().getUtcTimeStamp(OKCoinFixUtil.ENTRYTAG_SENDINGTIME).getTime();
				while (messageIterator.hasNext()) {
					Field messageField = messageIterator.next();
					List<Group> groups = message.getGroups(messageField.getField());
					switch (messageField.getField()) {
					case OKCoinFixUtil.TAG_ACCOUNT_APIKEY:
						account.setAccountAccessKey(messageField.getObject().toString());
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_CURRENCY:
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_ACCREQID:
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_BALANCE:
						// String data = messageField.getObject().toString();
						// String[] items = data.split("/");
						// double cny = Double.valueOf(items[0]);
						// double btc = Double.valueOf(items[1]);
						// double ltc = Double.valueOf(items[2]);
						// account.setAccountValue(Currency.CNY,
						// Double.valueOf(messageField.getObject().toString()));
						// account.setAccountValue(Currency.CNY,
						// Double.valueOf(messageField.getObject().toString()));
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_FREEBTC:
						account.setAccountValue(Currency.BTC, Double.valueOf(messageField.getObject().toString()));
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_FREELTC:
						account.setAccountValue(Currency.LTC, Double.valueOf(messageField.getObject().toString()));
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_FREEUSD:
						account.setAccountValue(Currency.CNY, Double.valueOf(messageField.getObject().toString()));
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_FROZENBTC:
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_FROZENLTC:
						break;
					case OKCoinFixUtil.TAG_ACCOUNT_FROZENUSD:
						break;
					default:
						logger.warn("onMessageExecutionReport: No such field: " + messageField);
						break;
					}
				}
				session.fireOnAccountInfo(account);
			}
		}

		@Override
		public void onMessage(OrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageOrderCancelReplaceRequest: " + message);
		}

		@Override
		public void onMessage(OrderMassCancelReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageOrderMassCancelReport: " + message);
		}

		@Override
		public void onMessage(OrderMassCancelRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageOrderMassCancelRequest: " + message);
		}

		@Override
		public void onMessage(ResendRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageResendRequest: " + message);
		}

		@Override
		public void onMessage(TestRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageTestRequest: " + message);
		}

		@Override
		public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageNewOrderSingle: " + message);
		}

		@Override
		public void onMessage(NewOrderCross message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageNewOrderCross: " + message);
		}

		@Override
		public void onMessage(NewOrderList message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageNewOrderList: " + message);
		}

		@Override
		public void onMessage(NewOrderMultileg message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageNewOrderMultileg: " + message);
		}

		@Override
		public void onMessage(OrderMassStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageOrderMassStatusRequest: " + message);
		}

		@Override
		public void onMessage(Reject message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.error("onMessageReject: " + message);
		}

		@Override
		public void onMessage(OrderCancelReject message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageOrderCancelReject: " + message);

			Iterator<Field<?>> messageIterator = message.iterator();
			long sendingTime = message.getHeader().getUtcTimeStamp(OKCoinFixUtil.ENTRYTAG_SENDINGTIME).getTime();
			Execution execution = new CommonExecution();
			while (messageIterator.hasNext()) {
				Field messageField = messageIterator.next();
				List<Group> groups = message.getGroups(messageField.getField());
				switch (messageField.getField()) {
				case FixOrderCancelRejectTag.CLORDID:
					execution.setCustomerOrderID(messageField.getObject().toString());
					break;
				case OKCoinFixUtil.EXECUTIONTAG_EXCEID:
					execution.setExecutionID(messageField.getObject().toString());
					break;
				case FixOrderCancelRejectTag.ORDERID:
					execution.setOrderID(messageField.getObject().toString());
					break;
				case FixOrderCancelRejectTag.ORDSTATUS:
					execution.setOrderStatus(FixUtil.convertOrderStatus(messageField.getObject().toString()));
					break;
				case FixOrderCancelRejectTag.SIDE:
					execution.setOrderSide(FixUtil.convertOrderSide(messageField.getObject().toString()));
					break;
				case FixOrderCancelRejectTag.TEXT:
					execution.setText(messageField.getObject().toString());
					break;
				case FixOrderCancelRejectTag.CXLREJREASON:
					break;
				case FixOrderCancelRejectTag.CXLREJRESPONSETO:
					break;
				case FixOrderCancelRejectTag.TRANSACTTIME:
					execution.setTransactTime(Long.valueOf(messageField.getObject().toString()));
					break;
				default:
					logger.warn("onMessageExecutionReport: No such field: " + messageField);
					break;
				}
			}
			session.fireOnExecution(execution);
		}

		@Override
		public void onMessage(ExecutionReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageExecutionReport: " + message);
			Iterator<Field<?>> messageIterator = message.iterator();
			long sendingTime = message.getHeader().getUtcTimeStamp(OKCoinFixUtil.ENTRYTAG_SENDINGTIME).getTime();
			Execution execution = new CommonExecution();
			while (messageIterator.hasNext()) {
				Field messageField = messageIterator.next();
				List<Group> groups = message.getGroups(messageField.getField());
				switch (messageField.getField()) {
				case FixExecutionReportTag.AVGPX:
					execution.setAveragePrice(Double.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.LASTPX:
					// execution.setAveragePrice(Double.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.LASTQTY:
					// execution.setAveragePrice(Double.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.ORDTYPE:
					// execution.setAveragePrice(Double.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.ORIGCLORDID:
					break;
				case FixExecutionReportTag.CLORDID:
					execution.setCustomerOrderID(messageField.getObject().toString());
					break;
				case FixExecutionReportTag.CUMQTY:
					break;
				case FixExecutionReportTag.EXECID:
					execution.setExecutionID(messageField.getObject().toString());
					break;
				case FixExecutionReportTag.ORDERID:
					if (messageField.getObject().toString().equals("refused")) {
						execution.setOrderStatus(OrderStatus.REJECTED);
					} else
						execution.setOrderID(messageField.getObject().toString());
					break;
				case FixExecutionReportTag.ORDERQTY:
					execution.setOrderAmount(Double.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.ORDSTATUS:
					execution.setOrderStatus(FixUtil.convertOrderStatus(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.PRICE:
					execution.setOrderPrice(Double.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.SIDE:
					execution.setOrderSide(FixUtil.convertOrderSide(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.SYMBOL:
					execution.setSymbol(messageField.getObject().toString());
					break;
				case FixExecutionReportTag.TEXT:
					execution.setText(messageField.getObject().toString());
					break;
				case FixExecutionReportTag.TRANSACTTIME:
					execution.setTransactTime(Long.valueOf(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.ORDREJREASON:
					execution.setOrderRejReason(OKCoinFixUtil.convertRejectReason(messageField.getObject().toString()));
					break;
				case FixExecutionReportTag.EXECTYPE:
					break;
				case FixExecutionReportTag.LEAVESQTY:
					execution.setLeavesAmount(Double.valueOf(messageField.getObject().toString()));
					break;
				default:
					logger.warn("onMessageExecutionReport: No such field: " + messageField);
					break;
				}
			}
			logger.info(execution);
			session.fireOnExecution(execution);
		}

		@Override
		public void onMessage(CrossOrderCancelRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageCrossOrderCancelRequest: " + message);
		}

		@Override
		public void onMessage(DerivativeSecurityList message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageDerivativeSecurityList: " + message);
		}

		@Override
		public void onMessage(DerivativeSecurityListRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageDerivativeSecurityListRequest: " + message);
		}

		@Override
		public void onMessage(Email message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageEmail: " + message);
		}

		@Override
		public void onMessage(IndicationOfInterest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageIndicationOfInterest: " + message);
		}

		@Override
		public void onMessage(ListCancelRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageListCancelRequest: " + message);
		}

		@Override
		public void onMessage(ListExecute message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageListExecute: " + message);
		}

		@Override
		public void onMessage(ListStatus message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageListStatus: " + message);
		}

		@Override
		public void onMessage(ListStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageListStatusRequest: " + message);
		}

		@Override
		public void onMessage(ListStrikePrice message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageListStrikePrice: " + message);
		}

		@Override
		public void onMessage(MultilegOrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageMultilegOrderCancelReplaceRequest: " + message);
		}

		@Override
		public void onMessage(NetworkStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageNetworkStatusRequest: " + message);
		}

		@Override
		public void onMessage(NetworkStatusResponse message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageNetworkStatusResponse: " + message);
		}

		@Override
		public void onMessage(News message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageNews: " + message);
		}

		@Override
		public void onMessage(PositionMaintenanceReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessagePositionMaintenanceReport: " + message);
		}

		@Override
		public void onMessage(PositionMaintenanceRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessagePositionMaintenanceRequest: " + message);
		}

		@Override
		public void onMessage(PositionReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessagePositionReport: " + message);
		}

		@Override
		public void onMessage(QuoteStatusReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageQuoteStatusReport: " + message);
		}

		@Override
		public void onMessage(QuoteStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageQuoteStatusRequest: " + message);
		}

		@Override
		public void onMessage(RegistrationInstructions message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageRegistrationInstructions: " + message);
		}

		@Override
		public void onMessage(RegistrationInstructionsResponse message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageRegistrationInstructionsResponse: " + message);
		}

		@Override
		public void onMessage(RequestForPositions message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageRequestForPositions: " + message);
		}

		@Override
		public void onMessage(RequestForPositionsAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageRequestForPositionsAck: " + message);
		}

		@Override
		public void onMessage(RFQRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageRFQRequest: " + message);
		}

		@Override
		public void onMessage(SecurityDefinition message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSecurityDefinition: " + message);
		}

		@Override
		public void onMessage(SecurityDefinitionRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSecurityDefinitionRequest: " + message);
		}

		@Override
		public void onMessage(SecurityList message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageSecurityList: " + message);
		}

		@Override
		public void onMessage(SecurityListRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSecurityListRequest: " + message);
		}

		@Override
		public void onMessage(SecurityStatus message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSecurityStatus: " + message);
		}

		@Override
		public void onMessage(SecurityStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSecurityStatusRequest: " + message);
		}

		@Override
		public void onMessage(SecurityTypeRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSecurityTypeRequest: " + message);
		}

		@Override
		public void onMessage(SecurityTypes message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageSecurityTypes: " + message);
		}

		@Override
		public void onMessage(SequenceReset message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageSequenceReset: " + message);
		}

		@Override
		public void onMessage(SettlementInstructionRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSettlementInstructionRequest: " + message);
		}

		@Override
		public void onMessage(SettlementInstructions message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageSettlementInstructions: " + message);
		}

		@Override
		public void onMessage(TradeCaptureReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageTradeCaptureReport: " + message);
		}

		@Override
		public void onMessage(TradeCaptureReportAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageTradeCaptureReportAck: " + message);
		}

		@Override
		public void onMessage(TradeCaptureReportRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageTradeCaptureReportRequest: " + message);
		}

		@Override
		public void onMessage(TradeCaptureReportRequestAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageTradeCaptureReportRequestAck: " + message);
		}

		@Override
		public void onMessage(TradingSessionStatus message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageTradingSessionStatus: " + message);
		}

		@Override
		public void onMessage(TradingSessionStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageTradingSessionStatusRequest: " + message);
		}

		@Override
		public void onMessage(UserRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageUserRequest: " + message);
		}

		@Override
		public void onMessage(UserResponse message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageUserResponse: " + message);
		}

		@Override
		public void onMessage(MassQuote message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageMassQuote: " + message);
		}

		@Override
		public void onMessage(MassQuoteAcknowledgement message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageMassQuoteAcknowledgement: " + message);
		}

		@Override
		public void onMessage(Advertisement message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageAdvertisement: " + message);
		}

		@Override
		public void onMessage(AllocationInstruction message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageAllocationInstruction: " + message);
		}

		@Override
		public void onMessage(AllocationInstructionAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageAllocationInstructionAck: " + message);
		}

		@Override
		public void onMessage(AllocationReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageAllocationReport: " + message);
		}

		@Override
		public void onMessage(AllocationReportAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageAllocationReportAck: " + message);
		}

		@Override
		public void onMessage(AssignmentReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageMassQuoteAcknowledgement: " + message);
		}

		@Override
		public void onMessage(BidRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageBidRequest: " + message);
		}

		@Override
		public void onMessage(BidResponse message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageBidResponse: " + message);
		}

		@Override
		public void onMessage(BusinessMessageReject message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageBusinessMessageReject: " + message);
		}

		@Override
		public void onMessage(CollateralAssignment message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageCollateralAssignment: " + message);
		}

		@Override
		public void onMessage(CollateralInquiry message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageCollateralInquiry: " + message);
		}

		@Override
		public void onMessage(CollateralInquiryAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageCollateralInquiryAck: " + message);
		}

		@Override
		public void onMessage(CollateralReport message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageCollateralReport: " + message);
		}

		@Override
		public void onMessage(CollateralRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageCollateralRequest: " + message);
		}

		@Override
		public void onMessage(CollateralResponse message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageCollateralResponse: " + message);
		}

		@Override
		public void onMessage(Confirmation message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageConfirmation: " + message);
		}

		@Override
		public void onMessage(ConfirmationAck message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageConfirmationAck: " + message);
		}

		@Override
		public void onMessage(ConfirmationRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageConfirmationRequest: " + message);
		}

		@Override
		public void onMessage(CrossOrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageCrossOrderCancelReplaceRequest: " + message);
		}

		@Override
		public void onMessage(QuoteCancel message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.warn("onMessageQuoteCancel: " + message);
		}

		@Override
		public void onMessage(QuoteRequest message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageQuoteRequest: " + message);
		}

		@Override
		public void onMessage(QuoteRequestReject message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.warn("onMessageQuoteRequestReject: " + message);
		}

		@Override
		public void onMessage(QuoteResponse message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageQuoteResponse: " + message);
		}

		@Override
		public void onMessage(OrderStatusRequest message, SessionID sessionID) throws FieldNotFound,
				UnsupportedMessageType, IncorrectTagValue {
			logger.info("onMessageOrderStatusRequest: " + message);
		}

		@Override
		public void onMessage(Quote message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
				IncorrectTagValue {
			logger.info("onMessageQuote: " + message);
		}

	}

}
