package com.movideo.baracus.client;

import java.util.List;

import com.movideo.baracus.clientimpl.UserAuthenticationRequest;
import com.movideo.baracus.model.VOD.Credit;
import com.movideo.baracus.model.VOD.Subscription;
import com.movideo.baracus.model.ads.Advertisement;
import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.collection.Collections;
import com.movideo.baracus.model.media.Media;
import com.movideo.baracus.model.media.ProductStream;
import com.movideo.baracus.model.media.StreamReport;
import com.movideo.baracus.model.metadata.Content;
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

public interface BaracusClient
{
	Playlists getPlaylists(String accessToken, Integer page, Integer limit) throws Exception;

	Playlist getPlaylistById(String accessToken, String id) throws Exception;

	Collections getCollections(String accessToken, Integer page, Integer limit) throws Exception;

	Collection getCollectionById(String accessToken, String id) throws Exception;

	Products getProducts(String accessToken, List<String> types, List<String> offerings, List<String> genres, List<String> originCountries, Integer minYear, Integer maxYear, Integer page, Integer limit) throws Exception;

	Product getProductById(String accessToken, String id) throws Exception;

	Products getChildProducts(String accessToken, String id, Integer page, Integer limit) throws Exception;

	Products getRelatedProducts(String accessToken, String id, Integer page, Integer limit) throws Exception;

	User getUser(String accessToken) throws Exception;

	User authenticateUser(UserAuthenticationRequest request) throws Exception;

	Purchases getUserPurchases(String accessToken, Integer page, Integer limit) throws Exception;

	Favourite getFavourites(String accessToken, Integer page, Integer limit) throws Exception;

	User createUser(String accessToken, User user) throws Exception;

	User updateUser(String accessToken, User user) throws Exception;

	Device addDevice(String accessToken, Device device) throws Exception;

	Favourite addFavourite(String accessToken, String productId) throws Exception;

	List<Order> getOrders(String accessToken, Integer page, Integer limit) throws Exception;

	Favourite deleteFavourite(String accessToken, String favouriteId) throws Exception;

	Media getMediaStreams(String acessToken, String productId, String variant, String deviceType, String deviceId) throws Exception;

	ProductStream getProductStreams(String accessToken, String productId, String variant, String deviceType, String deviceId, String episodeId) throws Exception;

	void getMediaStreams(String acessToken, String productId, String variant, String deviceType, String deviceId, final ICallback<Media> callback);

	void getAds(String productId, final ICallback<Advertisement> callback);

	Advertisement getAds(String productId) throws Exception;

	List<String> getCountries(String accessToken, List<String> types) throws Exception;

	List<String> getGenres(String accessToken, List<String> types) throws Exception;

	User resetPassword(String accessToken, String userName) throws Exception;

	Products searchProducts(String accessToken, String query, List<String> types, List<String> offerings, List<String> genres, List<String> originCountries, Integer minYear, Integer maxYear, Integer page, Integer limit) throws Exception;

	List<Content> getContent(String section, String device) throws Exception;

	List<Credit> getCredit(String accessToken) throws Exception;

	List<Subscription> getSubscriptionsEwallet(String accessToken) throws Exception;

	List<Subscription> getSubscriptionCredit(String accessToken) throws Exception;

	void reportPlaying(String accessToken, int productId, long currentPlaybackPosition, String streamCode, ICallback<StreamReport> streamReportCallback);

	void reportPause(String accessToken, int productId, long currentPlaybackPosition, String streamCode, ICallback<StreamReport> streamReportCallback);

	void reportStop(String accessToken, int productId, long currentPlaybackPosition, String streamCode, ICallback<StreamReport> streamReportCallback);

	void purchaseProduct(String accessToken, Integer productId, ProductPurchase productPurchase) throws Exception;
}
