package com.movideo.whitelabel.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.collection.Collections;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.playlist.Playlists;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.adapter.ShortClipsFragmentAdapter;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AutoFitRecyclerView;
import com.movideo.whitelabel.view.MarginDecoration;
import com.movideo.whitelabel.view.ProgressView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShortClipsFragment extends Fragment {

    @Bind(R.id.recycler_view)
    AutoFitRecyclerView autofitRecyclerView;
    private ProgressView progressView;

    /**
     * Empty public constructor
     */
    public ShortClipsFragment() {
        // Required empty public constructor
    }

    public static ShortClipsFragment newInstance() {
        ShortClipsFragment fragment = new ShortClipsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressView = new ProgressView(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_short_clips, container, false);
        ButterKnife.bind(this, view);
        autofitRecyclerView.setHasFixedSize(true);
        autofitRecyclerView.addItemDecoration(new MarginDecoration(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (NetworkAvailability.chkStatus(getActivity())) {
            ShortClipsProductsParser shortClipsProductsParser = new ShortClipsProductsParser();
            ViewUtils.execute(shortClipsProductsParser);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }

    }

    /*Call webservice for load short clip products*/
    public class ShortClipsProductsParser extends AsyncTask<Object, String, Playlists> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
            try {
                authToken = PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class).getAccessToken();
            } catch (Exception e) {
                authToken = null;
            }
        }

//        @Override
//        protected Collections doInBackground(Object... params) {
//            ContentHandler contentHandler = ContentHandler.getInstance();
//            Collections collections = null;
//            try {
//                collections = contentHandler.getCollections(authToken);
//            } catch (Exception e) {
//                error = e.getMessage();
//            }
//            return collections;
//        }

        @Override
        protected Playlists doInBackground(Object... objects) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Playlists playlists = null;
            try{
                playlists = contentHandler.getPlaylists(authToken);
            } catch (Exception e){
                error = e.getMessage();
            }
            return playlists;
        }

        @Override
        protected void onPostExecute(Playlists playlists) {
            super.onPostExecute(playlists);
            if (progressView!=null)
                progressView.dismiss();
            if (error == null && playlists != null) {
                List<Product> tempProductList = new ArrayList<Product>();
                for (Playlist playlist : playlists.getPlaylist()) {
                    tempProductList.addAll(playlist.getProductList());
                }
                autofitRecyclerView.setAdapter(new ShortClipsFragmentAdapter(getActivity(), tempProductList));
            } else {
                if (error != null && error.equals(getActivity().getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_is_logged_in))) {
                    User userInfo = PreferenceHelper.getSharedPrefData(getActivity(), getActivity().getResources().getString(R.string.user_info), User.class);
                    String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                    AuthenticationParser authenticationParser = new AuthenticationParser(getActivity(), new AuthenticationUserResultReceived() {
                        @Override
                        public void onResult(String error, User result) {
                            if (error == null && result != null) {
                                ShortClipsProductsParser shortClipsProductsParser = new ShortClipsProductsParser();
                                ViewUtils.execute(shortClipsProductsParser);
                            } else {
                                Toast.makeText(getActivity(),error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    ViewUtils.execute(authenticationParser, params);
                } else {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
