package com.movideo.baracus.client;

import java.util.List;

import com.movideo.baracus.clientimpl.ResetPasswordRequest;
import com.movideo.baracus.clientimpl.UserAuthenticationRequest;
import com.movideo.baracus.model.VOD.Credit;
import com.movideo.baracus.model.VOD.Subscription;
import com.movideo.baracus.model.ads.Advertisement;
import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.collection.Collections;
import com.movideo.baracus.model.common.DataList;
import com.movideo.baracus.model.media.StreamReport;
import com.movideo.baracus.model.metadata.Content;
import com.movideo.baracus.model.metadata.Metadata;
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
import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface BaracusService
{
	@GET("products/{id}/playback/streams")
	Call<ResponseBody> getProductStream(@Header("Movideo-Auth") String accessToken, @Path("id") String id, @Query("variant") String variant, @Query("device_type") String deviceType, @Query("device_id") String deviceId, @Query("episode_id") String episodeId);

	@GET("playlist")
	Call<Playlists> getPlaylists(@Header("Movideo-Auth") String accessToken, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@GET("playlist/list/{id}")
	Call<Playlist> getPlaylistById(@Header("Movideo-Auth") String accessToken, @Path("id") String id) throws Exception;

	@GET("collections")
	Call<Collections> getCollections(@Header("Movideo-Auth") String accessToken, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@GET("collections/{id}")
	Call<Collection> getCollectionById(@Header("Movideo-Auth") String accessToken, @Path("id") String id) throws Exception;

	@GET("products")
	Call<Products> getProducts(@Header("Movideo-Auth") String accessToken, @Query("types") List<String> types, @Query("offerings") List<String> offerings, @Query("genres") List<String> genres, @Query("origin_countries") List<String> originCountries, @Query("min_year") Integer minYear, @Query("max_year") Integer maxYear, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@GET("products/{id}")
	Call<Product> getProductById(@Header("Movideo-Auth") String accessToken, @Path("id") String id) throws Exception;

	@GET("products/{id}/episodes")
	Call<Products> getChildProducts(@Header("Movideo-Auth") String accessToken, @Path("id") String id, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@GET("products/{id}/related")
	Call<Products> getRelatedProducts(@Header("Movideo-Auth") String accessToken, @Path("id") String id, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@POST("user")
	Call<User> createUser(@Header("Movideo-Auth") String accessToken, @Body User user) throws Exception;

	@PUT("user")
	Call<User> updateUser(@Header("Movideo-Auth") String accessToken, @Body User user) throws Exception;

	@POST("user/authenticate")
	Call<User> authenticateUser(@Body UserAuthenticationRequest request) throws Exception;

	@GET("user")
	Call<User> getUser(@Header("Movideo-Auth") String accessToken) throws Exception;

	@GET("user/purchases")
	Call<Purchases> getUserPurchases(@Header("Movideo-Auth") String accessToken, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@POST("user/devices")
	Call<Device> addDevice(@Header("Movideo-Auth") String accessToken, @Body Device device) throws Exception;

	@GET("user/favourites")
	Call<Favourite> getFavourites(@Header("Movideo-Auth") String accessToken, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@GET("user/orders")
	Call<DataList<Order>> getOrders(@Header("Movideo-Auth") String accessToken, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@POST("user/favourites")
	Call<Favourite> addFavourite(@Header("Movideo-Auth") String accessToken, @Body Favourite userFavourites) throws Exception;

	@DELETE("user/favourites/{favourite_id}")
	Call<Favourite> deleteFavourite(@Header("Movideo-Auth") String accessToken, @Path("favourite_id") String favouriteId) throws Exception;

	@GET("metadata/countries")
	Call<Metadata> getCountries(@Header("Movideo-Auth") String accessToken, @Query("types") List<String> types) throws Exception;

	@GET("products/{id}/playback/streams")
	Call<ResponseBody> getStreams(@Header("Movideo-Auth") String accessToken, @Path("id") String productId, @Query("variant") String variant, @Query("device_type") String deviceType, @Query("device_id") String deviceId);

	@GET("products/{id}/ads")
	Call<Advertisement> getAds(@Path("id") String productId);

	@GET("metadata/genres")
	Call<Metadata> getGenres(@Header("Movideo-Auth") String accessToken, @Query("types") List<String> types) throws Exception;

	@POST("user/resetpassword")
	Call<User> resetPassword(@Header("Movideo-Auth") String accessToken, @Body ResetPasswordRequest request) throws Exception;
	
	@GET("products/search")
	Call<Products> search(@Header("Movideo-Auth") String accessToken, @Query("q") String query, @Query("types") List<String> types, @Query("offerings") List<String> offerings, @Query("genres") List<String> genres, @Query("origin_countries") List<String> originCountries, @Query("min_year") Integer minYear, @Query("max_year") Integer maxYear, @Query("page") Integer page, @Query("limit") Integer limit) throws Exception;

	@GET("data/rails/{section}")
	Call<DataList<Content>> getContent(@Path("section") String section, @Query("device") String device);

	@PUT("products/{id}")
	Call<ResponseBody> reportPlaying(@Header("Movideo-Auth") String accessToken, @Path("id") int productId, @Body StreamReport report);

	@PUT("products/{id}/pause")
	Call<ResponseBody> reportPause(@Header("Movideo-Auth") String accessToken, @Path("id") int productId, @Body StreamReport report);

	@PUT("products/{id}/stop")
	Call<ResponseBody> reportStop(@Header("Movideo-Auth") String accessToken, @Path("id") int productId, @Body StreamReport report);

	@POST("products/{id}/purchase")
	Call<ResponseBody> purchaseProduct(@Header("Movideo-Auth") String accessToken, @Path("id") Integer productId, @Body ProductPurchase productPurchase);

	@GET("/credits")
	Call<DataList<Credit>> getCreditsList(@Header("Movideo-Auth") String accessToken) throws Exception;

	@GET("/subscriptions")
	Call<DataList<Subscription>> getSubscriptionEwalletList(@Header("Movideo-Auth") String accessToken) throws Exception;

	@GET("/subscriptions/credits")
	Call<DataList<Subscription>> getSubscriptionCreditList(@Header("Movideo-Auth") String accessToken) throws Exception;

}
