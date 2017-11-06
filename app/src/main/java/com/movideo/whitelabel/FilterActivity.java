package com.movideo.whitelabel;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movideo.whitelabel.adapter.FilterScreenListItemAdapter;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetCountriesTask;
import com.movideo.whitelabel.communication.GetGenresTask;
import com.movideo.whitelabel.model.FilterItem;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.ClickListener;
import com.movideo.whitelabel.view.RelatedItemTouchListener;
import com.movideo.whitelabel.view.SpaceDecoration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener, ContentRequestListener<List<String>> {

    public static final String KEY_TYPES = "filter_screen_types";
    public static final String KEY_COUNTRIES = "countries";
    public static final String KEY_GENRES = "genres";
    public static final String KEY_MIN_YEAR = "min_year";
    public static final String KEY_MAX_YEAR = "max_year";
    public static final String KEY_SORT_BY = "sort_by";
    public static final String SORT_BY_CODE_RELEASE_DATE = "sort_by_release_date";
    public static final String SORT_BY_CODE_A_Z = "sort_by_a_z";
    public static final String SORT_BY_CODE_Z_A = "sort_by_z_a";

    private static final String TAG = FilterActivity.class.getSimpleName();

    @Bind(R.id.closeImageButton)
    ImageButton closeImageButton;

    @Bind(R.id.countryRecyclerView)
    RecyclerView countryRecyclerView;
    @Bind(R.id.yearRecyclerView)
    RecyclerView yearRecyclerView;
    @Bind(R.id.genresRecyclerView)
    RecyclerView genresRecyclerView;

    @Bind(R.id.releaseDateTextView)
    TextView releaseDateTextView;
    @Bind(R.id.a_zTextView)
    TextView a_zTextView;
    @Bind(R.id.z_aTextView)
    TextView z_aTextView;

    @Bind(R.id.resetTextView)
    TextView resetTextView;

    @Bind(R.id.applyFilterButton)
    Button applyFilterButton;

    @Bind(R.id.progressBarCountry)
    ProgressBar progressBarCountry;
    @Bind(R.id.progressBarGenres)
    ProgressBar progressBarGenres;

    private List<String> countryListTemp;
    private List<String> genresListTemp;

    private List<FilterItem> yearList;
    private List<FilterItem> countryList;
    private List<FilterItem> genresList;

    private List<String> contentTypes;

    private String minYear;
    private String maxYear;
    private String sortBy;
    private String selectedMinYear;
    private String selectedMaxYear;
    private String selectedSortBy;
    private List<String> selectedCountries;
    private List<String> selectedGenres;


    private FilterScreenListItemAdapter genresListItemAdapter;
    private FilterScreenListItemAdapter countriesListItemAdapter;
    private FilterScreenListItemAdapter yearsListItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        contentTypes = getIntent().getStringArrayListExtra(KEY_TYPES);
        selectedGenres = getIntent().getStringArrayListExtra(KEY_GENRES);
        selectedCountries = getIntent().getStringArrayListExtra(KEY_COUNTRIES);
        selectedMinYear = getIntent().getStringExtra(KEY_MIN_YEAR);
        selectedMaxYear = getIntent().getStringExtra(KEY_MAX_YEAR);
        selectedSortBy = getIntent().getStringExtra(KEY_SORT_BY);

        closeImageButton.setOnClickListener(this);
//        releaseDateTextView.setOnClickListener(this);
//        a_zTextView.setOnClickListener(this);
//        z_aTextView.setOnClickListener(this);
        resetTextView.setOnClickListener(this);
        applyFilterButton.setOnClickListener(this);

        defaultValueSelection();

        setCountryRecyclerView();
        loadCountries();

        setYearRecyclerView();

        setGenresRecyclerView();
        loadGenres();

        setResult(RESULT_CANCELED);
    }

    private void defaultValueSelection() {
        minYear = "1949";

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        maxYear = year + "";

        sortBy = SORT_BY_CODE_RELEASE_DATE;

//        if (!releaseDateTextView.isSelected()) {
//            releaseDateTextView.setSelected(true);
//            a_zTextView.setSelected(false);
//            z_aTextView.setSelected(false);
//
//            sortBy = SORT_BY_CODE_RELEASE_DATE;
//        }
    }

    private void resetValueSelection() {
        minYear = "1949";

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        maxYear = year + "";

        selectedCountries = null;
        selectedGenres = null;
        selectedMinYear = "";
        selectedMaxYear = "";
        selectedSortBy = "";

        setYearsListValues();
        setCountriesListValues();
        setGenresListValues();

//        if (!releaseDateTextView.isSelected()) {
//            releaseDateTextView.setSelected(true);
//            a_zTextView.setSelected(false);
//            z_aTextView.setSelected(false);
//
//            sortBy = SORT_BY_CODE_RELEASE_DATE;
//        }
    }

    private void loadCountries() {
        if (!progressBarCountry.isShown()) {
            progressBarCountry.setVisibility(View.VISIBLE);
        }

        Object[] params = new Object[]{contentTypes};

        GetCountriesTask getCountriesTask = new GetCountriesTask(this);
        getCountriesTask.execute(params);
    }

    /*set country list*/
    private void setCountryRecyclerView() {
        countryRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        countryRecyclerView.setLayoutManager(layoutManager);
        countryRecyclerView.addItemDecoration(new SpaceDecoration(this, R.dimen.list_item_space_country,
                true, true));

        countryRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(this, countryRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                for (int i = 0; i < countryList.size(); i++) {
                    if (position == 0) {
                        if (i == 0) {
                            countryList.set(i, new FilterItem(true, countryList.get(i).getTitle()));
                        } else {
                            countryList.set(i, new FilterItem(false, countryList.get(i).getTitle()));
                        }
                    } else {
                        countryList.set(0, new FilterItem(false, countryList.get(0).getTitle()));
                        if (countryList.get(position).getIsSelected())
                            countryList.set(position, new FilterItem(false, countryList.get(position).getTitle()));
                        else
                            countryList.set(position, new FilterItem(true, countryList.get(position).getTitle()));
                        break;
                    }
                }
                if (position == 0) {
//                    selectedCountries = countryListTemp;
                    selectedCountries = null;
                } else {
                    selectedCountries = new ArrayList<String>();
                    for (FilterItem filterItem : countryList) {
                        if (filterItem.getIsSelected())
                            selectedCountries.add(filterItem.getTitle());
                    }
                    if (selectedCountries.size() == 0) {
                        countryList.set(0, new FilterItem(true, countryList.get(0).getTitle()));
                        selectedCountries = null;
                    }
                }
                countriesListItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void loadGenres() {
        List<String> genres = PreferenceHelper.getSharedPrefData(this, ContentHandler.KEY_GENRES_LIST, List.class);
        if (genres != null
                && genres.size() > 0) {
            progressBarGenres.setVisibility(View.GONE);
            genresListTemp = genres;
//            selectedGenres = null;
            setGenresListValues();
        } else {
            if (!progressBarGenres.isShown()) {
                progressBarGenres.setVisibility(View.VISIBLE);
            }
            List<String> types = new ArrayList<>();
            types.add("movie");
            types.add("series");

            GetGenresTask getGenresTask = new GetGenresTask(getGenresContentListener(), types);
            Utils.executeInMultiThread(getGenresTask);
        }
    }

    /*set genres list*/
    private void setGenresRecyclerView() {
        genresRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        genresRecyclerView.setLayoutManager(layoutManager);
        genresRecyclerView.addItemDecoration(new SpaceDecoration(this, R.dimen.list_item_space_country,
                true, true));

        genresRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(this, genresRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                for (int i = 0; i < genresList.size(); i++) {
                    if (position == 0) {
                        if (i == 0) {
                            genresList.set(i, new FilterItem(true, genresList.get(i).getTitle()));
                        } else {
                            genresList.set(i, new FilterItem(false, genresList.get(i).getTitle()));
                        }
                    } else {
                        genresList.set(0, new FilterItem(false, genresList.get(0).getTitle()));
                        if (genresList.get(position).getIsSelected())
                            genresList.set(position, new FilterItem(false, genresList.get(position).getTitle()));
                        else
                            genresList.set(position, new FilterItem(true, genresList.get(position).getTitle()));
                        break;
                    }
                }
                if (position == 0) {
                    selectedGenres = null;
                } else {
                    selectedGenres = new ArrayList<String>();
                    for (FilterItem filterItem : genresList) {
                        if (filterItem.getIsSelected())
                            selectedGenres.add(filterItem.getTitle());
                    }
                    if (selectedGenres.size() == 0) {
                        genresList.set(0, new FilterItem(true, genresList.get(0).getTitle()));
                        selectedGenres = null;
                    }

                }
                genresListItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    /*set years list*/
    private void setYearRecyclerView() {

        setYearsListValues();

        yearRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        yearRecyclerView.setLayoutManager(layoutManager);
        yearRecyclerView.addItemDecoration(new SpaceDecoration(this, R.dimen.list_item_space_country,
                true, true));

        yearRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(this, yearRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position == 0) {
                    minYear = "1949";
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    maxYear = year + "";
                } else if (position == 1) {
                    minYear = "2010";
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    maxYear = year + "";
                } else {
                    String[] temp = yearList.get(position).getTitle().split("-");
                    minYear = temp[0].trim();
                    maxYear = temp[1].trim();
                }

                for (int i = 0; i < yearList.size(); i++) {
                    if (position == i) {
                        yearList.set(i, new FilterItem(true, yearList.get(i).getTitle()));
                    } else {
                        yearList.set(i, new FilterItem(false, yearList.get(i).getTitle()));
                    }
                }
                yearsListItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void setYearsListValues() {
        yearList = new ArrayList<FilterItem>();

        String[] years = {"All", "2010 & Newer", "2000 - 2009", "1990 - 1999", "1980 - 1989", "1970 - 1979", "1960 - 1969", "1950 - 1959", "1940 - 1949"};
        for (int i = 0; i < years.length; i++) {
            FilterItem filterItem;
            filterItem = new FilterItem(false, years[i]);

            if (i == 0) {
                filterItem.setIsSelected(true);
            } else {
                if (selectedMinYear != null && !selectedMinYear.isEmpty()){
                    String[] temp = years[i].split("-");
                    if(temp[0].trim().equals(selectedMinYear)){
                        filterItem.setIsSelected(true);
                        yearList.get(0).setIsSelected(false);
                    }
                }
            }
            yearList.add(filterItem);
        }

        yearsListItemAdapter = new FilterScreenListItemAdapter(this, yearList);
        yearRecyclerView.setAdapter(yearsListItemAdapter);
    }

    private void setCountriesListValues() {
        if (countryListTemp != null && countryListTemp.size() > 0) {
            countryList = new ArrayList<FilterItem>();
            FilterItem filterItem;

            if (selectedCountries != null && !selectedCountries.isEmpty()) {
                filterItem = new FilterItem(false, "All");
            } else {
                filterItem = new FilterItem(true, "All");
            }
            countryList.add(filterItem);

            for (int i = 0; i < countryListTemp.size(); i++) {
                filterItem = new FilterItem(false, countryListTemp.get(i));

                if (selectedCountries != null && !selectedCountries.isEmpty()) {
                    for (String country : selectedCountries) {
                        if (country.equals(countryListTemp.get(i))) {
                            filterItem.setIsSelected(true);
                            break;
                        }
                    }
                }
                countryList.add(filterItem);
            }
            countriesListItemAdapter = new FilterScreenListItemAdapter(this, countryList);
            countryRecyclerView.setAdapter(countriesListItemAdapter);
        }
    }

    private void setGenresListValues() {
        if (genresListTemp != null && genresListTemp.size() > 0) {
            genresList = new ArrayList<FilterItem>();
            FilterItem filterItem;

            if (selectedGenres != null && !selectedGenres.isEmpty()) {
                filterItem = new FilterItem(false, "All");
            } else {
                filterItem = new FilterItem(true, "All");
            }
            genresList.add(filterItem);

            for (String genres : genresListTemp) {
                filterItem = new FilterItem(false, genres);

                if (selectedGenres != null && !selectedGenres.isEmpty()) {
                    for (String selectedGenres : this.selectedGenres) {
                        if (selectedGenres.equals(genres)) {
                            filterItem.setIsSelected(true);
                            break;
                        }
                    }
                }
                genresList.add(filterItem);
            }
            genresListItemAdapter = new FilterScreenListItemAdapter(this, genresList);
            genresRecyclerView.setAdapter(genresListItemAdapter);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeImageButton:
                onBackPressed();
                break;
            case R.id.releaseDateTextView:
                if (!releaseDateTextView.isSelected()) {
                    releaseDateTextView.setSelected(true);
                    a_zTextView.setSelected(false);
                    z_aTextView.setSelected(false);

                    sortBy = SORT_BY_CODE_RELEASE_DATE;
                }
                break;
            case R.id.a_zTextView:
                if (!a_zTextView.isSelected()) {
                    a_zTextView.setSelected(true);
                    releaseDateTextView.setSelected(false);
                    z_aTextView.setSelected(false);

                    sortBy = SORT_BY_CODE_A_Z;
                }
                break;
            case R.id.z_aTextView:
                if (!z_aTextView.isSelected()) {
                    releaseDateTextView.setSelected(false);
                    a_zTextView.setSelected(false);
                    z_aTextView.setSelected(true);

                    sortBy = SORT_BY_CODE_Z_A;
                }
                break;
            case R.id.resetTextView:
                resetValueSelection();
                break;
            case R.id.applyFilterButton:
                Intent intent = getIntent();
                intent.putStringArrayListExtra(KEY_COUNTRIES, (ArrayList<String>) selectedCountries);
                intent.putStringArrayListExtra(KEY_GENRES, (ArrayList<String>) selectedGenres);
                intent.putExtra(KEY_MIN_YEAR, minYear);
                intent.putExtra(KEY_MAX_YEAR, maxYear);
                intent.putExtra(KEY_SORT_BY, sortBy);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestCompleted(List<String> countries) {
        if (progressBarCountry != null && progressBarCountry.isShown())
            progressBarCountry.setVisibility(View.GONE);
        try {
            if (countries != null && countries.size() > 0) {
                countryListTemp = countries;
//                selectedCountries = null;
                setCountriesListValues();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        if (progressBarCountry != null && progressBarCountry.isShown())
            progressBarCountry.setVisibility(View.GONE);
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    private ContentRequestListener<List<String>> getGenresContentListener() {
        return new ContentRequestListener<List<String>>() {

            @Override
            public void onRequestCompleted(List<String> strings) {
                if (progressBarGenres != null && progressBarGenres.isShown())
                    progressBarGenres.setVisibility(View.GONE);
                try {
                    if (strings != null && strings.size() > 0) {
                        genresListTemp = strings;
//                        selectedGenres = null;
                        setGenresListValues();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            @Override
            public void onRequestFail(Throwable throwable) {
                if (progressBarGenres != null && progressBarGenres.isShown())
                    progressBarGenres.setVisibility(View.GONE);
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        };
    }
}
