package com.bixterprise.gateway.utils;


@FunctionalInterface
public interface Callback<E>{
	public void call(E o);
}