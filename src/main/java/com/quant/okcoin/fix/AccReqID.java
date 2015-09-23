package com.quant.okcoin.fix;


import quickfix.StringField;
/**
 * OKCOIN鑷畾涔�
 * @author OKCOIN
 */
public class AccReqID extends StringField{

	private static final long serialVersionUID = -3564308206005258232L;

	public AccReqID() {
		super(8000);
	}
	
	public AccReqID(String data) {
		super(8000,data);
	}

	
}
