package com.quant.okcoin.conf;

import com.google.inject.AbstractModule;
import com.quant.core.conf.CommonModule;
import com.quant.okcoin.fix.OKCoinFixSessionFactory;


public class OKCoinModule extends AbstractModule{

	@Override
	protected void configure() {
		install(new CommonModule());
		bind(OKCoinFixSessionFactory.class);
		
	}

}
