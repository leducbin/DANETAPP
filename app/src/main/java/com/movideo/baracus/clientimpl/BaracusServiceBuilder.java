package com.movideo.baracus.clientimpl;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.movideo.baracus.client.BaracusService;
import com.movideo.baracus.exceptions.NotFoundException;
import com.movideo.baracus.exceptions.UnauthorisedException;
import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.common.Image;
import com.movideo.baracus.model.common.ImageDetails;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.product.Product;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class BaracusServiceBuilder
{

	private String apiVersion;
	private String url;
	private String locale;
	private String authorization;
	private BaracusService baracusService;
	private Level clientloglevel;

	private static final String[] DATE_FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ssZ",
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd",
			"EEE MMM dd HH:mm:ss z yyyy",
			"HH:mm:ss",
			"MM/dd/yyyy HH:mm:ss aaa",
			"yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
			"yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
			"yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
			"MMM d',' yyyy H:mm:ss a"
	};

	public BaracusServiceBuilder(String apiKey, String apiVersion, String url, String locale, Level clientloglevel)
	{
		this.apiVersion = apiVersion;
		this.url = url;
		this.locale = locale;
		this.authorization = "Basic " + new String(Base64.encodeBase64((apiKey + ":").getBytes()));
		this.clientloglevel = clientloglevel;
	}

	public OkHttpClient getClient()
	{
		Interceptor interceptor = new Interceptor()
		{
			public Response intercept(Chain chain) throws IOException
			{
				Request newRequest = chain.request().newBuilder().addHeader("Authorization", authorization).addHeader("Accept", "application/json").addHeader("Accept-Language", locale).addHeader("Movideo-API-Version", apiVersion).build();
				return chain.proceed(newRequest);
			}
		};

		OkHttpClient client = new OkHttpClient();
		client.interceptors().add(interceptor);
		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(clientloglevel);
		client.interceptors().add(loggingInterceptor);

		return client;
	}

	public static <T> T getResult(retrofit.Response<T> response) throws Exception
	{
		try
		{
			if(response.isSuccess())
			{
				T body = response.body();
				if (body.getClass() == Product.class){
					Product product = (Product) body;
					ImageRepo.instance().saveAllProductImages(product);

				}

				if (body.getClass() == Collection.class){
					Collection collection = (Collection) body;
					List<Product> products = collection.getProductList();
					if (null != products && products.size() > 0){
						for (Product product : products) {
							ImageRepo.instance().saveAllProductImages(product);
						}
					}
				}

				if (body.getClass() == Playlist.class){
					Playlist playlist = (Playlist) body;
					List<Product> products = playlist.getProductList();
					if (null != products && products.size() > 0){
						for (Product product : products) {
							ImageRepo.instance().saveAllProductImages(product);
						}
					}
				}

				return response.body();
			}
			else
			{
				if(response.code() == 401)
				{
					throw new UnauthorisedException(response.code() + " " + response.message());

				}
				else if(response.code() == 404)
				{
					throw new NotFoundException(response.code() + " " + response.message());
				}
			}
		}
		catch(Exception ex)
		{
			throw ex;
		}
		return null;
	}



	public BaracusService build()
	{
		// Creates the json object which will manage the information received
		GsonBuilder builder = new GsonBuilder();
		// Register an adapter to manage the date types as long values
		Gson gson = builder.registerTypeAdapter(Date.class, new DateDeserializer()).create();
		Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create(gson)).client(getClient()).build();
		baracusService = retrofit.create(BaracusService.class);
		return baracusService;
	}
	private class DateDeserializer implements JsonDeserializer<Date> {

		@Override
		public Date deserialize(JsonElement jsonElement, Type typeOF,
								JsonDeserializationContext context) throws JsonParseException {
			try {
				long milliseconds = Long.parseLong(jsonElement.getAsString());
				return new Date(jsonElement.getAsJsonPrimitive().getAsLong());
			} catch (NumberFormatException ex) {

			}
			for (String format : DATE_FORMATS) {
				try {
					return new SimpleDateFormat(format, Locale.US).parse(jsonElement.getAsString());
				} catch (ParseException e) {
				}
			}
			throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()
					+ "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
		}
	}
}