package com.bixterprise.gateway.utils;

public interface GatewayInteface {

	@FunctionalInterface
	public interface IAddable<E>{
		public void add(E e);
	}
}
