package com.movideo.whitelabel;

import android.util.Log;

import com.movideo.baracus.client.BaracusClient;
import com.movideo.baracus.clientimpl.BaracusClientBuilder;
import com.movideo.baracus.clientimpl.ProductRepo;
import com.movideo.baracus.clientimpl.UserAuthenticationRequest;
import com.movideo.baracus.exceptions.UnauthorisedException;
import com.movideo.baracus.model.VOD.Credit;
import com.movideo.baracus.model.VOD.Subscription;
import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.collection.Collections;
import com.movideo.baracus.model.media.ProductStream;
import com.movideo.baracus.model.metadata.Content;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.playlist.Playlists;
import com.movideo.baracus.model.product.Offerings;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.baracus.model.user.Favourite;
import com.movideo.baracus.model.user.Order;
import com.movideo.baracus.model.user.ProductPurchase;
import com.movideo.baracus.model.user.Purchases;
import com.movideo.baracus.model.user.User;
import com.movideo.baracus.util.StreamReportCallback;
import com.movideo.whitelabel.util.PlaybackHandler;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * Handles all backend communications to get any content. Implemented as a singleton.
 */
public class ContentHandler {

    public static final String KEY_AVOD_HOME_CONTENT = "avod_home_content";
    public static final String KEY_SVOD_HOME_CONTENT = "svod_home_content";
    public static final String KEY_TVOD_HOME_CONTENT = "tvod_home_content";
    public static final String KEY_GENRES_LIST = "genres_list_content";

    public static final String API_KEY = "457"/*"7pKRHcepcHK4XV1x7Gw6c4s65574KnLF"*/;
    //public static final String API_URL = "http://danetmiddleware.54aqfb5idd.ap-southeast-1.elasticbeanstalk.com/";/*danet.vn/api/*/
    //public static final String API_URL = "http://danetmiddleware-prod.ap-southeast-1.elasticbeanstalk.com";
    //public static final String API_URL = "http://middleware.danet.vn/";
    //public static final String API_URL = "http://api.danet.vn/";
    public static final String API_URL = "http://api.danet.vn/";
    private static final String API_VERSION = "1.0.0";
    private static final String API_LOCALE = "vi_VN";
    private static final String DEVICE = "android";

    private static ContentHandler instance;

    private BaracusClient baracusClient;

    private ContentHandler() {
        baracusClient = new BaracusClientBuilder(API_KEY)
                .setUrl(API_URL)
                .setApiVersion(API_VERSION)
                .setLocale(API_LOCALE)
                .setOkHttpClientLogLevel(HttpLoggingInterceptor.Level.BASIC)
                .build();
    }

    public static ContentHandler getInstance() {
        if (instance == null)
            instance = new ContentHandler();
        return instance;
    }

    private String getSessionToken() {

        if (WhiteLabelApplication.getInstance().isUserLoggedIn()) {
            return WhiteLabelApplication.getInstance().getAccessToken();
        } else {
            return null;
        }
    }

    /**
     * Returns list of all products.
     *
     * @param types           {@link List<String>} List of content types.
     * @param offering        {@link List<String>} List of offerings.
     * @param genres          {@link List<String>} List of genres.
     * @param originCountries {@link List<String>} List of origin countries.
     * @param minYear         Minimum year.
     * @param maxYear         Maximum year.
     * @param page            Page number starting from 1.
     * @param limit           Number of items per page.
     * @return {@link List<Product>}
     * @throws Exception
     */
    public List<Product> getAllProducts(List<String> types, List<String> offering, List<String> genres,
                                        List<String> originCountries, Integer minYear, Integer maxYear,
                                        Integer page, Integer limit) throws Exception {
        Products products = null;
        try {
            products = baracusClient.getProducts(getSessionToken(), types, offering, genres, originCountries, minYear, maxYear, page, limit);
        } catch (UnauthorisedException e) {
            updateSessionToken();
            products = baracusClient.getProducts(getSessionToken(), types, offering, genres, originCountries, minYear, maxYear, page, limit);
        }
        return products.getProductList();
    }

    /**
     * Returns collection for the given product id.
     *
     * @param collectionId Collection Id.
     * @return {@link Collection}
     * @throws Exception
     */
    public Collection getCollectionById(String collectionId) throws Exception {
        Collection collection = null;
        try {
            collection = baracusClient.getCollectionById(getSessionToken(), collectionId);

        } catch (UnauthorisedException e) {
            updateSessionToken();
            collection = baracusClient.getCollectionById(getSessionToken(), collectionId);
        }

         if (collection != null){
             if (collection.getProductList() != null){
                 //cache all collection's products - TODO: dangerous
                 for (Product product :
                         collection.getProductList()) {
                     if (ProductRepo.instance().getProduct(product.getId()) == null){
                         ProductRepo.instance().saveProduct(product);
                     }
                 }
             }
         }

        return collection;
    }

    /**
     * Returns collection for the given product id.
     *
     * @param playlistId Playlist Id.
     * @return {@link Playlist}
     * @throws Exception
     */
    public Playlist getPlaylistById(String playlistId) throws Exception {
        Playlist playlist = null;
        try {
            playlist = baracusClient.getPlaylistById(getSessionToken(), playlistId);
        } catch (UnauthorisedException e) {
            updateSessionToken();
            playlist = baracusClient.getPlaylistById(getSessionToken(), playlistId);
        }

        if (playlist != null){
            if (playlist.getProductList() != null){
                //cache all playlist's products - TODO: dangerous
                for (Product product :
                        playlist.getProductList()) {
                    if (ProductRepo.instance().getProduct(product.getId()) == null){
                        ProductRepo.instance().saveProduct(product);
                    }
                }
            }
        }

        return playlist;
    }
    /**
     * Returns list of all collections.
     *
     * @return {@link List<Collection>}
     * @throws Exception
     */
    public List<Collection> getAllCollections(int page, int limit) throws Exception {
        Collections collections = null;
        try {
            collections = baracusClient.getCollections(getSessionToken(), page, limit);
        } catch (UnauthorisedException e) {
            updateSessionToken();
            collections = baracusClient.getCollections(getSessionToken(), page, limit);
        }
        return collections.getCollectionList();
    }

    /**
     * Returns list of all playlist.
     *
     * @return {@link List<Playlist>}
     * @throws Exception
     */
    public List<Playlist> getAllPlaylists(int page, int limit) throws Exception {
        Playlists playlists = null;
        try {
            playlists = baracusClient.getPlaylists(getSessionToken(), page, limit);
        } catch (UnauthorisedException e) {
            updateSessionToken();
            playlists = baracusClient.getPlaylists(getSessionToken(), page, limit);
        }
        return playlists.getPlaylist();
    }

    /**
     * Returns a wish list of products for given user's access token.
     *
     * @return {@link List<Product>}
     * @throws Exception
     */
    public List<Product> getWishList(int page, int limit) throws Exception {
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();

        Favourite favourite = baracusClient.getFavourites(accessToken, page, limit);
        return favourite.getFavouriteProducts();
    }

    /**
     * Delete items from wish list. Provide the ids in a list.
     * Returns the updated wish list.
     *
     * @param wishListIds {@link List<String>} list of ids.
     * @return {@link List<Product>}
     * @throws Exception
     */
    public List<Product> deleteWishListItems(List<String> wishListIds) throws Exception {
        Favourite favourite = null;
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();

        for (String id : wishListIds) {
            favourite = baracusClient.deleteFavourite(accessToken, id);
        }
        return favourite.getFavouriteProducts();
    }

    /**
     * Returns a list of products that user rent.
     *
     * @return {@link List<Product>}
     * @throws Exception
     */
    public List<Product> getRentedItemList(int page, int limit) throws Exception {
        String accessToken = WhiteLabelApplication.getInstance().getUser().getAccessToken();

        Purchases purchases = baracusClient.getUserPurchases(accessToken, page, limit);

        return purchases.getPurchasedProducts();
    }

    /**
     * Returns Products for the given authentication token and product id.
     *
     * @param authToken
     * @param productId
     * @return {@link Products}
     * @throws Exception
     */
    public Products getChildProducts(String authToken, String productId) throws Exception {
        return baracusClient.getChildProducts(authToken, productId, 1, 10);//TODO implement pagination in to the method signature and relevant usage.
    }

    public ProductStream getProductStream(String productId, String variant, String deviceType, String deviceId, String episodeId) throws Exception{
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        return baracusClient.getProductStreams(accessToken, productId, variant, deviceType, deviceId, episodeId);
    }

    /**
     * Returns Products for the given authentication token and product id.
     *
     * @param authToken
     * @param productId
     * @return {@link Products}
     * @throws Exception
     */
    public Products getRelatedProducts(String authToken, String productId) throws Exception {
        return baracusClient.getRelatedProducts(authToken, productId, 1, 10);//TODO implement pagination in to the method signature and relevant usage.
    }

    /**
     * Returns Favourite for the given authentication token and product id.
     *
     * @param authToken
     * @param productId
     * @return {@link Favourite}
     * @throws Exception
     */
    public Favourite addFavourite(String authToken, String productId) throws Exception {
        return baracusClient.addFavourite(authToken, productId);
    }

    /**
     * Returns product for the given authentication token and product id.
     *
     * @param authToken
     * @param productId Product id.
     * @return {@link Product}
     * @throws Exception
     */
    public Product getProductById(String authToken, String productId) throws Exception {
        return getProductById(authToken, productId, false);
    }


    public Product getProductById(String authToken, String productId, Boolean cached) throws Exception {
        Product product;
        if (cached)
        {
            product = ProductRepo.instance().getProduct(Integer.parseInt(productId));
            if (product != null)
                return product;
        }

        product =  baracusClient.getProductById(authToken, productId);

        if (product != null){
            ProductRepo.instance().saveProduct(product);
        }

        return product;
    }

    /**
     * Returns product for the given authentication token.
     *
     * @param authToken
     * @return {@link Collections}
     * @throws Exception
     */
    public Collections getCollections(String authToken) throws Exception {


        return baracusClient.getCollections(authToken, 1, 10);//TODO implement pagination in to the method signature and relevant usage.

    }

    /**
     * Returns product for the given authentication token.
     *
     * @param authToken
     * @return {@link Playlists}
     * @throws Exception
     */
    public Playlists getPlaylists(String authToken) throws Exception {


        return baracusClient.getPlaylists(authToken, 1, 10);//TODO implement pagination in to the method signature and relevant usage.

    }

    /**
     * Returns user for the given UserAuthenticationRequest.
     *
     * @param userAuthenticationRequest
     * @return {@link User}
     * @throws Exception
     */
    public User authenticateUser(UserAuthenticationRequest userAuthenticationRequest) throws Exception {
        return baracusClient.authenticateUser(userAuthenticationRequest);
    }

    /**
     * Updates the session key by sending an authentication request.
     *
     * @throws Exception
     */
    public void updateSessionToken() throws Exception {
        User user = WhiteLabelApplication.getInstance().getUser();
        UserAuthenticationRequest userAuthenticationRequest = new UserAuthenticationRequest(user.getProvider(), user.getIdentifier(), user.getPassword());
        user = authenticateUser(userAuthenticationRequest);
        WhiteLabelApplication.getInstance().setUser(user);
        WhiteLabelApplication.getInstance().setAccessToken(user.getAccessToken());
    }

    /**
     * Returns user for the given authentication token.
     *
     * @param authToken
     * @return {@link User}
     * @throws Exception
     */
    public User getUser(String authToken) throws Exception {
        return baracusClient.getUser(authToken);
    }

    /**
     * Update the user.
     *
     * @param authToken
     * @param user      {@link User}
     * @return {@link User}
     * @throws Exception
     */
    public User updateUser(String authToken, User user) throws Exception {
        return baracusClient.updateUser(authToken, user);
    }

    /**
     * Returns user for the given identifier.
     *
     * @param identifier
     * @return {@link User}
     * @throws Exception
     */
    public User resetPassword(String authToken, String identifier) throws Exception {
        return baracusClient.resetPassword(authToken, identifier);
    }

    /**
     * Returns search result for given query.
     *
     * @param query           Query string.
     * @param types           {@link List<String>} List of content types.
     * @param offering        {@link List<String>} List of offerings.
     * @param genres          {@link List<String>} List of genres.
     * @param originCountries {@link List<String>} List of origin countries.
     * @param minYear         Minimum year.
     * @param maxYear         Maximum year.
     * @param page            Page number starting from 1.
     * @param limit           Number of items per page.
     * @return {@link List<Product>}
     * @throws Exception
     */
    public Products getSearchResult(String query, List<String> types, List<String> offering, List<String> genres, List<String> originCountries, Integer minYear, Integer maxYear, Integer page, Integer limit) throws Exception {
        Products products = null;
        try {
            products = baracusClient.searchProducts(getSessionToken(), query, types, offering, genres, originCountries, minYear, maxYear, page, limit);
        } catch (UnauthorisedException e) {
            updateSessionToken();
            products = baracusClient.searchProducts(getSessionToken(), query, types, offering, genres, originCountries, minYear, maxYear, page, limit);
        }
        return products;
    }

    /**
     * Returns content for given section
     *
     * @param section Content section
     * @return {@link Content}
     * @throws Exception
     */
    public List<Content> getContentRails(String section) throws Exception {

        return baracusClient.getContent(section, DEVICE);
    }

    /**
     * Return genres for given types.
     *
     * @param types {@link List<String>} Eg: movie, series...
     * @return {@link List<String>} list  of genres.
     * @throws Exception
     */
    public List<String> getGenres(List<String> types) throws Exception {
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();

        List<String> genres = baracusClient.getGenres(accessToken, types);

        return genres;
    }

    /**
     * Return countries for given types.
     *
     * @param types {@link List<String>} Eg: movie, series...
     * @return {@link List<String>} list  of countries.
     * @throws Exception
     */
    public List<String> getCountries(List<String> types) throws Exception {
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        List<String> countries = baracusClient.getCountries(accessToken, types);
        return countries;
    }

    //Added by Thanh Tam
    /**
     * Returns list of all orders.
     *
     * @return {@link List<Order>}
     * @throws Exception
     */

    public List<Order> getAllOrders(String authToken) throws Exception {
        List<Order> orders = null;
        try{
            orders = baracusClient.getOrders(authToken,1,10);
        } catch (UnauthorisedException e){
            updateSessionToken();
            orders = baracusClient.getOrders(authToken,1,10);
        }

        return orders;
    }
    /**
     * Returns list of all credits.
     *
     * @return {@link List<Credit>}
     * @throws Exception
     */
    public List<Credit> getAllCredits(String authToken) throws Exception {
        List<Credit> credits = null;
        try {
            credits = baracusClient.getCredit(authToken);
        } catch (UnauthorisedException e) {
            updateSessionToken();
            credits = baracusClient.getCredit(authToken);
        }
        return credits;
    }

    /**
     * Returns list of all subscriptions according to payment type.
     *
     * @return {@link List<Subscription>}
     * @throws Exception
     */
    public List<Subscription> getAllSubscriptions(String authToken,String type) throws Exception {
        List<Subscription> subscriptions = null;
        if(type.equals("1")) { //ewallet
            try {
                subscriptions = baracusClient.getSubscriptionsEwallet(authToken);
            } catch (UnauthorisedException e) {
                updateSessionToken();
                subscriptions = baracusClient.getSubscriptionsEwallet(authToken);
            }
        }
        else{
            try {
                subscriptions = baracusClient.getSubscriptionCredit(authToken);
            } catch (UnauthorisedException e) {
                updateSessionToken();
                subscriptions = baracusClient.getSubscriptionCredit(authToken);
            }
        }
        return subscriptions;
    }
    //End added by Thanh Tam

    public void reportPlaying(int productId, long currentPlaybackPosition, String streamCode) throws Exception{
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        baracusClient.reportPlaying(accessToken, productId, currentPlaybackPosition, streamCode, new StreamReportCallback());
    }

    public void reportPause(int productId, long currentPlaybackPosition, String streamCode) throws Exception{
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        baracusClient.reportPause(accessToken, productId, currentPlaybackPosition, streamCode, new StreamReportCallback());
    }

    public void reportStop(int productId, long currentPlaybackPosition, String streamCode) throws Exception{
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        baracusClient.reportStop(accessToken, productId, currentPlaybackPosition, streamCode, new StreamReportCallback());
    }

    public void purchaseProductWithCredit(Product product) throws Exception {
        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
        Offerings offerings = PlaybackHandler.getHDCreditOffering(product);
        String paymentType = "credits";
        ProductPurchase productPurchase = new ProductPurchase(offerings.getId(), paymentType);
        baracusClient.purchaseProduct(accessToken, product.getId(), productPurchase);
    }
}
