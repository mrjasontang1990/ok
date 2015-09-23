package com.quant.okcoin.strategy;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.quant.okcoin.conf.OKCoinModule;


public class OKCoinStrategyManagerTest {
	public static final Logger logger = Logger.getLogger(OKCoinStrategyManagerTest.class);
	@Inject public OKCoinStrategyManager okManager;
	@Inject public OKCoinStrategy okStrategy;
	
	public static void main(String[] args) throws InterruptedException {
		Injector injector = Guice.createInjector(new OKCoinModule());
		OKCoinStrategyManagerTest okTest = injector.getInstance(OKCoinStrategyManagerTest.class);
//		okTest.okManager.setStrategy(okTest.okStrategy);
//		okTest.okManager.start();
//		Thread.sleep(60000000);
//		okTest.okManager.stop();
		
		try {
			okTest.okStrategy.beforeStrategy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		while(true) {
//			Thread.sleep(5000);
//		}
		
	}
	
	
}
