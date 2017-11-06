package com.movideo.baracus.clientimpl;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.movideo.baracus.client.BaracusClient;
import com.movideo.baracus.client.BaracusService;
import com.movideo.baracus.exceptions.BaracusException;
import com.movideo.baracus.exceptions.NotFoundException;
import com.movideo.baracus.exceptions.UnauthorisedException;
import com.movideo.baracus.model.VOD.Credit;
import com.movideo.baracus.model.VOD.Subscription;
import com.movideo.baracus.model.ads.Advertisement;
import com.movideo.baracus.model.ads.CuePoint;
import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.collection.Collections;
import com.movideo.baracus.model.common.DataList;
import com.movideo.baracus.model.media.Media;
import com.movideo.baracus.model.media.ProductStream;
import com.movideo.baracus.model.media.StreamReport;
import com.movideo.baracus.model.metadata.Content;
import com.movideo.baracus.model.metadata.Metadata;
import com.movideo.baracus.model.metadata.MetadataInfo;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.playlist.Playlists;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.baracus.model.user.Device;
import com.movideo.baracus.model.user.Favourite;
import com.movideo.baracus.model.user.Order;
import com.movideo.baracus.model.user.ProductPurchase;
import com.movideo.baracus.model.user.Purchases;
import com.movideo.baracus.model.user.User;
import com.movideo.baracus.util.ICallback;
import com.movideo.baracus.xml.SmilSAXHandler;
import com.movideo.baracus.xml.XmlParser;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class BaracusClientImpl implements BaracusClient
{

	private BaracusService baracusService;
	private XmlParser xmlParser;
	private ProductStreamParser productStreamParser;

	public BaracusClientImpl(String apiKey, String apiVersion, String url, String locale, Level clientloglevel)
	{
		baracusService = new BaracusServiceBuilder(apiKey, apiVersion, url, locale, clientloglevel).build();
		xmlParser = new XmlParser();
		productStreamParser = new ProductStreamParser();
	}

	//Added by Thanh Tam
	public Playlists getPlaylists(String accessToken, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getPlaylists(accessToken, page, limit).execute());
	}

	public Playlist getPlaylistById(String accessToken, String id) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getPlaylistById(accessToken, id).execute());
	}
	//End added by Thanh Tam

	public Collections getCollections(String accessToken, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getCollections(accessToken, page, limit).execute());
	}

	public Collection getCollectionById(String accessToken, String id) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getCollectionById(accessToken, id).execute());
	}

	public Products getProducts(String accessToken, List<String> types, List<String> offerings, List<String> genres, List<String> originCountries, Integer minYear, Integer maxYear, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getProducts(accessToken, types, offerings, genres, originCountries, minYear, maxYear, page, limit).execute());
	}

	public Products searchProducts(String accessToken, String query, List<String> types, List<String> offerings, List<String> genres, List<String> originCountries, Integer minYear, Integer maxYear, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.search(accessToken, query, types, offerings, genres, originCountries, minYear, maxYear, page, limit).execute());
	}

	public Product getProductById(String accessToken, String id) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getProductById(accessToken, id).execute());
	}

	public Products getChildProducts(String accessToken, String id, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getChildProducts(accessToken, id, page, limit).execute());
	}

	public Products getRelatedProducts(String accessToken, String id, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getRelatedProducts(accessToken, id, page, limit).execute());
	}

	public User getUser(String accessToken) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getUser(accessToken).execute());
	}

	public User authenticateUser(UserAuthenticationRequest request) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.authenticateUser(request).execute());
	}

	public Purchases getUserPurchases(String accessToken, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getUserPurchases(accessToken, page, limit).execute());
	}

	public Favourite getFavourites(String accessToken, Integer page, Integer limit) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.getFavourites(accessToken, page, limit).execute());
	}

	//Added by Thanh Tam
	public List<Order> getOrders(String accessToken, Integer page, Integer limit) throws Exception {
		DataList<Order> result = BaracusServiceBuilder.getResult(baracusService.getOrders(accessToken,page,limit).execute());
		if(result==null)
			return null;
		return result.getData();
	}
	//End added by Thanh Tam

	public User createUser(String accessToken, User user) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.createUser(accessToken, user).execute());
	}

	public User updateUser(String accessToken, User user) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.updateUser(accessToken, user).execute());
	}

	public Device addDevice(String accessToken, Device device) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.addDevice(accessToken, device).execute());
	}

	public Favourite addFavourite(String accessToken, String productId) throws Exception
	{
		Favourite favourite = new Favourite();
		favourite.setProductId(productId);
		return BaracusServiceBuilder.getResult(baracusService.addFavourite(accessToken, favourite).execute());
	}

	public Favourite deleteFavourite(String accessToken, String favouriteId) throws Exception
	{
		return BaracusServiceBuilder.getResult(baracusService.deleteFavourite(accessToken, favouriteId).execute());
	}

	public List<String> getCountries(String accessToken, List<String> types) throws Exception
	{
		return getMetadataList(BaracusServiceBuilder.getResult(baracusService.getCountries(accessToken, types).execute()));
	}

	public List<String> getGenres(String accessToken, List<String> types) throws Exception
	{
		return getMetadataList(BaracusServiceBuilder.getResult(baracusService.getGenres(accessToken, types).execute()));
	}

	public List<String> getMetadataList(Metadata metadata)
	{
		List<String> list = new ArrayList<String>();
		for(MetadataInfo info : metadata.getMetadataList())
		{
			list.add(info.getValue());
		}
		return list;
	}

	public User resetPassword(String accessToken, String userName) throws Exception
	{
		ResetPasswordRequest request = new ResetPasswordRequest(userName);
		return BaracusServiceBuilder.getResult(baracusService.resetPassword(accessToken, request).execute());
	}

	@Override
	public Media getMediaStreams(String accessToken, String productId, String variant, String deviceType, String deviceId) throws Exception
	{
		Response<ResponseBody> response = baracusService.getStreams(accessToken, productId, variant, deviceType, deviceId).execute();
		if(!response.isSuccess())
		{
			if(response.code() == 401)
			{
				throw new UnauthorisedException(response.code() + " " + response.message());
			}
			else if(response.code() == 404)
			{
				throw new NotFoundException(response.code() + " " + response.message());
			}

			throw new BaracusException(response.code() + " " + response.message());
		}

		Media media = xmlParser.parse(response.body().byteStream(), new SmilSAXHandler());
		media.setProductId(productId);
		media.setPlayerToken(response.headers().get("Movideo-Player-Token"));
		return media;
	}

	@Override
	public ProductStream getProductStreams(String accessToken, String productId, String variant, String deviceType, String deviceId, String episodeId) throws Exception {
		Response<ResponseBody> response = baracusService.getProductStream(accessToken, productId, variant, deviceType, deviceId, episodeId).execute();
		if(!response.isSuccess())
		{
			if(response.code() == 401)
			{
				throw new UnauthorisedException(response.code() + " " + response.message());
			}
			else if(response.code() == 404)
			{
				throw new NotFoundException(response.code() + " " + response.message());
			}

			throw new BaracusException(response.code() + " " + response.message());
		}

		ProductStream productStream = productStreamParser.parse(response.body());
		productStream.setProductId(productId);
		productStream.setEpisodeId(episodeId);
		return productStream;
	}

	@Override
	public void getMediaStreams(String accessToken, final String productId, String variant, String deviceType, String deviceId, final ICallback<Media> callback)
	{
		baracusService.getStreams(accessToken, productId, variant, deviceType, deviceId).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
				if (!response.isSuccess()) {

					if (response.code() == 401) {
						callback.onFailure(new UnauthorisedException("Invalid Api Key"));

					} else if (response.code() == 404) {
						callback.onFailure(new NotFoundException("The resource does not exist"));
					} else {
						callback.onFailure(new Exception("Internal Server Error"));
					}
				}

				try {
					Media media = xmlParser.parse(response.body().byteStream(), new SmilSAXHandler());
					media.setProductId(productId);
					callback.onSuccess(media);
				} catch (IOException e) {
					callback.onFailure(e);
				} catch (SAXException e) {
					callback.onFailure(e);
				} catch (ParserConfigurationException e) {
					callback.onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable t) {
				callback.onFailure(t);
			}
		});
	}

	public Advertisement getAds(String productId) throws Exception
	{
		Advertisement advertisement = BaracusServiceBuilder.getResult(baracusService.getAds(productId).execute());
		if(advertisement.getCuePoints() != null)
		{
			int i = 0;
			for(CuePoint cuePoint : advertisement.getCuePoints())
			{
				cuePoint.setIndex(i++);
			}
		}

		return advertisement;
	}

	public void getAds(String productId, final ICallback<Advertisement> callback)
	{
		baracusService.getAds(productId).enqueue(new Callback<Advertisement>()
		{
			@Override
			public void onResponse(Response<Advertisement> response, Retrofit retrofit)
			{
				try
				{
					Advertisement advertisement = BaracusServiceBuilder.getResult(response);
					callback.onSuccess(advertisement);
				}
				catch(Exception e)
				{
					callback.onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable t)
			{
				callback.onFailure(t);
			}
		});
	}

	public List<Content> getContent(String section, String device) throws Exception
	{
		DataList<Content> result = BaracusServiceBuilder.getResult(baracusService.getContent(section, device).execute());
		if (result == null) {
			return null;
		}

		return result.getData();
	}

	//Added by Thanh Tam
	@Override
	public List<Credit> getCredit(String accessToken) throws Exception {
		DataList<Credit> result = BaracusServiceBuilder.getResult(baracusService.getCreditsList(accessToken).execute());
		if(result==null){
			return null;
		}
		return result.getData();
	}

	@Override
	public List<Subscription> getSubscriptionsEwallet(String accessToken) throws Exception {
		DataList<Subscription> result = BaracusServiceBuilder.getResult(baracusService.getSubscriptionEwalletList(accessToken).execute());
		if(result==null){
			return null;
		}
		return result.getData();
	}

	@Override
	public List<Subscription> getSubscriptionCredit(String accessToken) throws Exception {
		DataList<Subscription> result = BaracusServiceBuilder.getResult(baracusService.getSubscriptionCreditList(accessToken).execute());
		if(result==null){
			return null;
		}
		return result.getData();
	}
	//End added by Thanh Tam

	@Override
	public void reportPlaying(String accessToken, final int productId, final long currentPlaybackPosition, final String streamCode, final ICallback<StreamReport> callback ) {
		String playbackPositionInSecond = String.valueOf(currentPlaybackPosition / 1000);
		StreamReport streamReport = new StreamReport(playbackPositionInSecond, streamCode);
		baracusService.reportPlaying(accessToken, productId, streamReport )
						.enqueue(new StreamReportResponseCallback(callback, streamReport));
	}

	@Override
	public void reportPause(String accessToken, final int productId, final long currentPlaybackPosition, final String streamCode, final ICallback<StreamReport> callback ) {
		String playbackPositionInSecond = String.valueOf(currentPlaybackPosition / 1000);
		StreamReport streamReport = new StreamReport(playbackPositionInSecond, streamCode);
		baracusService.reportPause(accessToken, productId, streamReport)
				.enqueue(new StreamReportResponseCallback(callback, streamReport));
	}

	@Override
	public void reportStop(String accessToken, final int productId, final long currentPlaybackPosition, final String streamCode, final ICallback<StreamReport> callback ) {
		String playbackPositionInSecond = String.valueOf(currentPlaybackPosition / 1000);
		StreamReport streamReport = new StreamReport(playbackPositionInSecond, streamCode);
		baracusService.reportStop(accessToken, productId, streamReport)
				.enqueue(new StreamReportResponseCallback(callback, streamReport));
	}

	@Override
	public void purchaseProduct(String accessToken, Integer productId, ProductPurchase productPurchase) throws Exception {
		Response<ResponseBody> response = baracusService.purchaseProduct(accessToken, productId, productPurchase).execute();
		if (! response.isSuccess()){
			Log.e(BaracusClient.class.getName(), response.errorBody().string());
			throw new Exception(response.errorBody().string());
		}
	}

	class StreamReportResponseCallback implements Callback<ResponseBody>{
		private ICallback<StreamReport> callback;
		private StreamReport streamReport;

		StreamReportResponseCallback(ICallback<StreamReport> callback, StreamReport streamReport) {
			this.callback = callback;
			this.streamReport = streamReport;
		}

		@Override
		public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
			if (!response.isSuccess()) {
				if (response.code() == 401) {
					callback.onFailure(new UnauthorisedException(buildErrorString("Invalid Api Key", response, streamReport)));
				} else if (response.code() == 404) {
					callback.onFailure(new NotFoundException(buildErrorString("The resource does not exist", response, streamReport)));
				} else {
					callback.onFailure(new Exception(buildErrorString("Server Error, error code: ", response, streamReport)));
				}
			}
			else {
				callback.onSuccess(streamReport);
			}
		}

		private String buildErrorString(String initialError, Response<ResponseBody> response, StreamReport streamReport){
			StringBuffer buffer = new StringBuffer(initialError);
			buffer.append(", error code: ");
			buffer.append(response.code());
			try
			{
				if (response.errorBody() != null){
					buffer.append(". Error content: " + response.errorBody().string().toString());
				}
				if (response.body() != null){
					buffer.append(". Body content: " + response.body().string().toString());
				}
			}
			catch (Exception ex){
				Log.e("Stream Response Parsing", ex.getMessage());
				ex.printStackTrace();
			}


			buffer.append(". Stream Report: " + streamReport.toString());

			return buffer.toString();
		}

		@Override
		public void onFailure(Throwable t) {
			callback.onFailure(t);
		}
	}

}