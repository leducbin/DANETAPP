package com.movideo.baracus.clientimpl;

import com.movideo.baracus.client.BaracusClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;

public class BaracusClientBuilder
{

	private String apiKey;
	private String apiVersion = "1.0.0";
	private String url;
	private String locale = "en_US";
	private Level clientloglevel = Level.BODY;

	public BaracusClientBuilder(String apiKey)
	{
		this.apiKey = apiKey;
	}

	public BaracusClientBuilder setUrl(String url)
	{
		this.url = url;
		return this;
	}

	public BaracusClientBuilder setApiVersion(String apiVersion)
	{
		this.apiVersion = apiVersion;
		return this;
	}

	public BaracusClientBuilder setLocale(String locale)
	{
		this.locale = locale;
		return this;
	}

	public BaracusClientBuilder setOkHttpClientLogLevel(Level clientloglevel)
	{
		this.clientloglevel = clientloglevel;
		return this;
	}

	public BaracusClient build()
	{
		return new BaracusClientImpl(apiKey, apiVersion, url, locale, clientloglevel);
	}

}
