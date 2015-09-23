package com.quant.okcoin.strategy;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.quant.core.annotation.AfterStrategy;
import com.quant.core.annotation.BeforeStrategy;
import com.quant.core.api.Account;
import com.quant.core.api.Contract;
import com.quant.core.api.Currency;
import com.quant.core.api.Execution;
import com.quant.core.api.MDEntry;
import com.quant.core.api.Order;
import com.quant.core.api.OrderBook;
import com.quant.core.api.OrderSide;
import com.quant.core.api.OrderStatus;
import com.quant.core.api.OrderType;
import com.quant.core.api.StrategyDetail;
import com.quant.core.api.Tick;
import com.quant.core.api.impl.AbstractStrategy;
import com.quant.core.api.impl.CommonOrder;
import com.quant.core.api.impl.CommonOrderBook;
import com.quant.core.connection.Session;
import com.quant.core.math.MathUtils;
import com.quant.core.service.api.DataAccessService;
import com.quant.core.service.api.OrderManagementService;
import com.quant.core.service.api.TimeSeriesManagementService;
import com.quant.okcoin.fix.OKCoinFixSessionFactory;

public class OKCoinStrategy extends AbstractStrategy {

	private static final Logger logger = Logger.getLogger(OKCoinStrategy.class);

	@Inject
	private OrderManagementService orderManagementService;
	
	@Inject TimeSeriesManagementService timeSeriesManagementService;
	
	@Inject
	private OKCoinFixSessionFactory okSessionFactory;

	@Inject
	private DataAccessService dataAccessService;

	private Session okSession;

	private Account okAccount;

	private Contract okBtc;

	private Map<String, Order> orderList = Maps.newConcurrentMap();

	private OrderBook orderBook = new CommonOrderBook(10);

	private volatile int orderID = 100;

	public List<Session> getSessionList() {
		// TODO Auto-generated method stub
		return Lists.newArrayList(okSession);
	}

	public void prepareStrategy() throws Exception {
		setStrategyDetail(dataAccessService.selectOne(StrategyDetail.class, "1", true));
	}

	@BeforeStrategy
	public void beforeStrategy() throws Exception {
		okAccount = dataAccessService.selectOne(Account.class, "2", true);
		okBtc = dataAccessService.selectOne(Contract.class, "4", true);
		okSession = okSessionFactory.openSession();
		orderBook.setContract(okBtc);
		okSession.login("1", okAccount);

	}
	@AfterStrategy
	public void afterStrategy() throws Exception {
		okSession.logout(okAccount);

	}

	public void reset() {

	}

	public void onLogin(Session session) {
		logger.info("onLogon.");
		while (true) {
			if (okSession.isLogin(okAccount)) {
				okSession.requestMarketDepth("2", okBtc, 10, true);
				break;
			}
		}
	}

	public void onLogout(Session session) {
		logger.info("onLogout.");
		okSession.login("", okAccount);
	}

	public void onAccountInfo(Account account) {
		// TODO Auto-generated method stub
		logger.error(account);
	}

	public void onMarketDepth() {
		// TODO Auto-generated method stub

	}

	public void onMarketTick(String requestID, Tick tick) {
		// TODO Auto-generated method stub

	}

	public void onMarketMDEntryUpdate(String requestID, MDEntry mdEntry) {
		// TODO Auto-generated method stub

	}

	int id = 1;

	private List<String> removedOrders = Lists.newArrayList();
	private List<Double> midPrice = Lists.newArrayList();
	public void onMarketMDEntryUpdate(String requestID, List<MDEntry> list) {
		// TODO Auto-generated method stub
		orderBook.addMDEntry(list);
		if (!orderBook.isValid()) {
			logger.error("Order Book Invalid: " + orderBook);
			return;
		}
		
		timeSeriesManagementService.update(orderBook);
		
		okSession.requestAccountInfo(String.valueOf(id++));
		double money = okAccount.getAccountValue(Currency.CNY);
		double btc = okAccount.getAccountValue(Currency.BTC);
		// if(true)
		// logger.error("Bid: " + orderBook.getBid(0) + " Ask: " +
		// orderBook.getAsk(0));
		// else
		// return;
		removedOrders.clear();
		for (Entry<String, Order> entry : orderList.entrySet()) {
			Order temp = entry.getValue();
			if (temp.getOrderStatus().equals(OrderStatus.COMPLETED)
					|| temp.getOrderStatus().equals(OrderStatus.CANCELLED)) {
				removedOrders.add(entry.getKey());
			}
		}
		for (String id : removedOrders) {
			orderList.remove(id);
		}
		for (Entry<String, Order> entry : orderList.entrySet()) {
			Order temp = entry.getValue();
			if (!(temp.getOrderStatus().equals(OrderStatus.SUBMITTED) || temp.getOrderStatus().equals(
					OrderStatus.PARTIAL_COMPLETED)))
				continue;
			if (temp.getOrderSide().equals(OrderSide.LONG)) {
				if (temp.getOrderPrice() < orderBook.getBid(0) - 0.3)
					okSession.cancelOrder(String.valueOf(id++), temp);
			} else if (temp.getOrderSide().equals(OrderSide.SHORT)) {
				if (temp.getOrderPrice() > orderBook.getAsk(0) + 0.3)
					okSession.cancelOrder(String.valueOf(id++), temp);
			}
		}
		
		midPrice.add(new Double((orderBook.getAsk(0)+orderBook.getBid(0))/2));
		if(midPrice.size() < 50)
			return;
		double currentPrice = midPrice.get(midPrice.size()-1);
		double beforePrice = midPrice.get(midPrice.size()-51);
		DecimalFormat df = new DecimalFormat("######0.00");
//		double ma = this.getMa(midPrice, 50);
//		double last = midPrice.get(midPrice.size()-1);
		//okSession.requestAccountInfo(String.valueOf(id++));
		if (currentPrice-beforePrice<-0.15 && money > 500) {
			Order order = new CommonOrder();
			order.setCustomerOrderID(String.valueOf(id++));
			order.setContract(okBtc);
			order.setOrderSide(OrderSide.LONG);
			order.setOrderAmount(MathUtils.randomValue(0.03, 0.06, 0.001));
			order.setOrderPrice(orderBook.getBid(1)+0.01);
			order.setOrderType(OrderType.LMT);
			order.setOrderStatus(OrderStatus.PENDING_SUBMIT);
			okSession.sendOrder("", order);
			orderList.put(order.getCustomerOrderID(), order);
		}
		if (currentPrice-beforePrice>0.15 && money < 1600) {
			Order order2 = new CommonOrder();
			order2.setCustomerOrderID(String.valueOf(id++));
			order2.setContract(okBtc);
			order2.setOrderSide(OrderSide.SHORT);
			order2.setOrderAmount(MathUtils.randomValue(0.03, 0.06, 0.001));
			order2.setOrderPrice(orderBook.getAsk(1) - 0.01);
			order2.setOrderType(OrderType.LMT);
			order2.setOrderStatus(OrderStatus.PENDING_SUBMIT);
			okSession.sendOrder("", order2);
			orderList.put(order2.getCustomerOrderID(), order2);
		}
		// if(orderBook.getBid(0)-order.getOrderPrice()<-0.15) {
		// okSession.sendOrder("", order2);
		// orderList.put(order2.getCustomerOrderID(), order2);}
//		for (int i = 0; i < 9; i++) {
//			double price = Double.valueOf(df.format(orderBook.getBid(i)));
//			double nextPrice = Double.valueOf(df.format(orderBook.getBid(i + 1)));
//			logger.warn("i=" + i + "price:" + price + " next: "+nextPrice);
//			while (price - 0.01 > nextPrice+0.005) {
//				price = price - 0.01;
//				Order order = new CommonOrder();
//				order.setCustomerOrderID(String.valueOf(id++));
//				order.setContract(okBtc);
//				order.setOrderSide(OrderSide.LONG);
//				order.setOrderAmount(MathUtils.randomValue(0.01, 0.02, 0.001));
//				order.setOrderPrice(price);
//				order.setOrderType(OrderType.LMT);
//				order.setOrderStatus(OrderStatus.PENDING_SUBMIT);
//				okSession.sendOrder("", order);
//				orderList.put(order.getCustomerOrderID(), order);
//			}
//		}
//		for (int i = 0; i < 9; i++) {
//			double price = Double.valueOf(df.format(orderBook.getAsk(i)));
//			double nextPrice = Double.valueOf(df.format(orderBook.getAsk(i + 1)));
//			logger.warn("i=" + i + "price:" + price + " next: "+nextPrice);
//			while (price + 0.01 < nextPrice-0.005) {
//				price = price + 0.01;
//				Order order = new CommonOrder();
//				order.setCustomerOrderID(String.valueOf(id++));
//				order.setContract(okBtc);
//				order.setOrderSide(OrderSide.SHORT);
//				order.setOrderAmount(MathUtils.randomValue(0.01, 0.011, 0.001));
//				order.setOrderPrice(price);
//				order.setOrderType(OrderType.LMT);
//				order.setOrderStatus(OrderStatus.PENDING_SUBMIT);
//				okSession.sendOrder("", order);
//				orderList.put(order.getCustomerOrderID(), order);
//			}
//		}
	}

	public void onExecution(Execution execution) {
		orderManagementService.updateOrder(execution);
		logger.info(execution);
	}

	public void onMessage(String message) {
		// TODO Auto-generated method stub

	}

	public void onMarketOrderBookUpdate(String requestID, OrderBook orderBook) {
		// TODO Auto-generated method stub

	}
	
	private double getMa(List<Double> prices, int ma) {
		double p = 0;
		for(int i = 0; i < ma; i++) {
			p += prices.get(prices.size()-1-i);
		}
		return p/(double)ma;
	}

}
