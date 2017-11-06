package com.movideo.whitelabel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movideo.baracus.model.metadata.Content;
import com.movideo.whitelabel.adapter.MenuItemAdapter;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.enums.MenuType;
import com.movideo.whitelabel.fragment.HomePageFragment;
import com.movideo.whitelabel.fragment.MovieTrailersFragment;
import com.movideo.whitelabel.fragment.MyLibraryPageFragment;
import com.movideo.whitelabel.fragment.SearchFragment;
import com.movideo.whitelabel.fragment.UserDetailsFragment;
import com.movideo.whitelabel.model.MenuItem;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

/**
 * Handles the menu functionality.
 */
public class MenuHandler implements DrawerLayout.DrawerListener {

    @Bind(R.id.imageViewMenuHeaderAVODIndicator)
    ImageView avodIndicator;
    @Bind(R.id.imageViewMenuHeaderSVODIndicator)
    ImageView svodIndicator;
    @Bind(R.id.imageViewMenuHeaderTVODIndicator)
    ImageView tvodIndicator;
    @Bind(R.id.layoutMenuHeaderAVOD)
    LinearLayout avodLayout;
    @Bind(R.id.layoutMenuHeaderSVOD)
    LinearLayout svodLayout;
    @Bind(R.id.layoutMenuHeaderTVOD)
    LinearLayout tvodLayout;
    @BindDimen(R.dimen.menu_footer_height)
    int footerHeight;

    private boolean userLoggedIn;
    private boolean isMenuOpen;
    private Activity activity;
    private DrawerLayout drawerLayout;
    private ExpandableListView menuList;
    private MenuItemAdapter adaptor;
    private LicenseType licenseType;
    private List<MenuItem> menuItems;
    private HashMap<String, List<MenuItem>> subMenuItems = new HashMap<>();
    private Button buttonIcon;
    private ImageView menuIcon;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private FrameLayout containerLayout;
    private RelativeLayout menuLayout;


    private Map<LicenseType, List<MenuItem>> menuItemsByLicenseType = new HashMap<>();
    /**
     * Construct the menu with the given parameters.
     *
     * @param activity        {@link Context}.
     * @param drawerLayout    {@link DrawerLayout}.
     * @param menuList        {@link ExpandableListView}.
     * @param menuIcon        {@link ImageView} of menu opening image.
     * @param fragmentManager {@link FragmentManager}
     */
    public MenuHandler(Activity activity, DrawerLayout drawerLayout, ExpandableListView menuList, ImageView menuIcon, Button buttonViewIcon, FrameLayout containerLayout, FragmentManager fragmentManager) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.menuList = menuList;
        this.menuIcon = menuIcon;
        this.buttonIcon = buttonViewIcon;
        this.licenseType = LicenseType.AVOD;
        this.fragmentManager = fragmentManager;
        this.containerLayout = containerLayout;
        userLoggedIn = WhiteLabelApplication.getInstance().isUserLoggedIn();

        init();
    }

    /**
     * Initialize the menu
     */
    private void init() {
        isMenuOpen = false;

        loadMenuItem();

        menuLayout = (RelativeLayout) menuList.getParent();

        menuList.bringToFront();
        menuIcon.bringToFront();
        menuLayout.bringToFront();

        drawerLayout.setScrimColor(activity.getResources().getColor(R.color.transparent_black_50));

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View menuHeader = inflater.inflate(R.layout.layout_menu_header, menuList, false);

        ButterKnife.bind(this, menuHeader);
        setMenuProfile(licenseType);

        loadMenuFooter();

        menuList.addHeaderView(menuHeader);

        List<MenuItem> menu = new ArrayList<>();
        menu.addAll(menuItems);
        adaptor = new MenuItemAdapter(activity, menu, subMenuItems);
        menuList.setAdapter(adaptor);

        menuList.setOnGroupClickListener(new DrawerGroupClickListener());
        menuList.setOnChildClickListener(new DrawerChildClickListener());

        drawerLayout.setDrawerListener(this);

        drawerLayout.closeDrawer(menuLayout);

        buttonIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpen) {
                    drawerLayout.closeDrawer(menuLayout);
                } else {
                    drawerLayout.openDrawer(menuLayout);
                }
            }
        });

        avodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (licenseType != LicenseType.AVOD || !(currentFragment instanceof HomePageFragment)) {
                    licenseType = LicenseType.AVOD;
                    WhiteLabelApplication.getInstance().setLicenseType(licenseType);
                    navigateToHomeFragment(licenseType);
                }
            }
        });

        svodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (licenseType != LicenseType.SVOD || !(currentFragment instanceof HomePageFragment)) {
                    licenseType = LicenseType.SVOD;
                    WhiteLabelApplication.getInstance().setLicenseType(licenseType);
                    navigateToHomeFragment(licenseType);
                }
            }
        });

        tvodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (licenseType != LicenseType.TVOD || !(currentFragment instanceof HomePageFragment)) {
                    licenseType = LicenseType.TVOD;
                    WhiteLabelApplication.getInstance().setLicenseType(licenseType);
                    navigateToHomeFragment(licenseType);
                }
            }
        });
    }

    /**
     * Call this method on resume.
     */
    public void onResume() {
        drawerLayout.closeDrawer(menuLayout);
        userLoggedIn = WhiteLabelApplication.getInstance().isUserLoggedIn();
        loadMenuFooter();
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        menuIcon.setTranslationX(drawerView.getWidth() * slideOffset);
        containerLayout.setTranslationX(drawerView.getWidth() * slideOffset);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        isMenuOpen = true;
        menuLayout.bringToFront();
        menuList.bringToFront();
        menuIcon.bringToFront();
        buttonIcon.bringToFront();
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        isMenuOpen = false;
        menuIcon.bringToFront();
        buttonIcon.bringToFront();
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    private void loadMenuItem() {

        menuItems = menuItemsByLicenseType.get(licenseType);

        if (menuItems == null || menuItems.isEmpty()) {

            menuItems = new ArrayList<>();

            int i = 0;
            MenuItem menuItem = new MenuItem(Integer.toString(++i), "Tìm kiếm", 0, MenuType.NORMAL);
            menuItems.add(menuItem);

            final List<Content> contentList = PreferenceHelper.getContentList(activity, licenseType);
            List<Content> menuContentList = null;
//            for (Content content : contentList) {
//                if (content.getType() == Content.Type.navigation) {
//                    menuContentList = content.getItems();
//                    break;
//                }
//            }
//
//            if (menuContentList != null && !menuContentList.isEmpty()) {
//                for (Content menuContent : menuContentList) {
//                    if (menuContent.getName().equalsIgnoreCase("movies"))
//                        menuItems.add(new MenuItem(Integer.toString(++i), "Tất cả", 0, MenuType.NORMAL, menuContent));
//                    if (menuContent.getName().equalsIgnoreCase("TV Shows") && (licenseType == LicenseType.AVOD))
//                        menuItems.add(new MenuItem(Integer.toString(++i), "Show truyền hình", 0, MenuType.NORMAL, menuContent));
//                }
//            }
            if(contentList!=null) {
                for (Content content : contentList) {
                    if (content.getType() == Content.Type.subnavigation) {
                        menuContentList = content.getItems();
                        break;
                    }
                }
            }
            if (menuContentList != null && !menuContentList.isEmpty()) {
                for (Content menuContent : menuContentList) {
//                    if (menuContent.getCategory().equalsIgnoreCase("movies"))
//                        menuItems.add(new MenuItem(Integer.toString(++i), "Tất cả", 0, MenuType.NORMAL, menuContent));
//                    if (menuContent.getCategory().equalsIgnoreCase("series") && (licenseType == LicenseType.AVOD))
                    menuItems.add(new MenuItem(Integer.toString(++i), menuContent.getTitle(), 0, MenuType.NORMAL, menuContent));
                }
            }

            else {
                if (licenseType == LicenseType.AVOD) {
                    menuItems.add(new MenuItem("4", "Phim tình cảm", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Phim tình cảm", "Tâm lý", null, 0, null)));
                    menuItems.add(new MenuItem("4", "Hài", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Hài", "hài", null, 0, null)));
                    menuItems.add(new MenuItem("4", "Phim gia đình", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Gia đình", "gia đình", null, 0, null)));
                } else {
                    menuItems.add(new MenuItem("4", "Hành động", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Hành động", "hành động", null, 0, null)));
                    menuItems.add(new MenuItem("5", "Hài", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Hài", "Hài", null, 0, null)));
                    menuItems.add(new MenuItem("6", "Tâm lý", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Tâm lý", "tâm lý", null, 0, null)));
                    menuItems.add(new MenuItem("7", "Lãng mạn", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Lãng mạn", "lãng mạn", null, 0, null)));
                    menuItems.add(new MenuItem("8", "Kinh dị", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Kinh dị", "kinh dị", null, 0, null)));
                    menuItems.add(new MenuItem("9", "Viễn  tưởng", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Viễn tưởng", "viễn tưởng", null, 0, null)));
                    menuItems.add(new MenuItem("10", "Phiêu lưu", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Phiêu lưu", "phiêu lưu", null, 0, null)));
                    menuItems.add(new MenuItem("11", "Gia đình", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Gia đình", "gia đình", null, 0, null)));
                    menuItems.add(new MenuItem("12", "Trẻ em", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Trẻ em", "trẻ em", null, 0, null)));
                    menuItems.add(new MenuItem("13", "Hoạt hình", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Hoạt hình", "hoạt hình", null, 0, null)));
                    menuItems.add(new MenuItem("14", "Võ thuật", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Võ thuật", "võ thuật", null, 0, null)));
                    menuItems.add(new MenuItem("15", "Điều tra - Tội phạm", 0, MenuType.NORMAL, new Content(Content.Type.genre, "Điều tra - Tội phạm", "điều tra - tội phạm", null, 0, null)));

                }
            }
            menuItemsByLicenseType.put(licenseType, menuItems);
        }
    }

    private void loadMenuFooter() {
        int footerTotalHeight = footerHeight;

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View menuFooter = menuLayout.findViewById(R.id.relativeLayoutMenuFooter);

        LinearLayout footerLayout = (LinearLayout) menuFooter.findViewById(R.id.linearLayoutFooter);
        ImageView filter = (ImageView) menuFooter.findViewById(R.id.imageViewMenuFooterFilter);

        footerLayout.removeAllViews();

        if (userLoggedIn) {
            View menuItemMyLibrary = inflater.inflate(R.layout.list_item_menu_footer, footerLayout, false);

            ImageView iconMyLibrary = (ImageView) menuItemMyLibrary.findViewById(R.id.imageViewListItemMenuFooterIcon);
            TextView titleMyLibrary = (TextView) menuItemMyLibrary.findViewById(R.id.textViewListItemMenuFooterTitle);
            RelativeLayout layoutMyLibrary = (RelativeLayout) menuItemMyLibrary.findViewById(R.id.relativeLayoutListItemMenuFooter);

            iconMyLibrary.setImageResource(R.drawable.icon_my_library);
            titleMyLibrary.setText(R.string.label_menu_footer_my_library_title);
            layoutMyLibrary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(menuLayout);
                        }
                    });
                    onMyLibraryItemClick();
                }
            });

            View menuItemUserProfile = inflater.inflate(R.layout.list_item_menu_footer, footerLayout, false);

            ImageView iconUserProfile = (ImageView) menuItemUserProfile.findViewById(R.id.imageViewListItemMenuFooterIcon);
            TextView titleUserProfile = (TextView) menuItemUserProfile.findViewById(R.id.textViewListItemMenuFooterTitle);
            RelativeLayout layoutUserProfile = (RelativeLayout) menuItemUserProfile.findViewById(R.id.relativeLayoutListItemMenuFooter);

            iconUserProfile.setImageResource(R.drawable.icon_profile);
            titleUserProfile.setText(WhiteLabelApplication.getInstance().getUserFullName());
            layoutUserProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(menuLayout);
                        }
                    });
                    onUserProfileItemClick();
                }
            });

            footerLayout.addView(menuItemMyLibrary);
            footerLayout.addView(menuItemUserProfile);

            footerTotalHeight = footerHeight * 2;
        } else {
            View menuItemLogin = inflater.inflate(R.layout.list_item_menu_footer, footerLayout, false);

            ImageView iconLogIn = (ImageView) menuItemLogin.findViewById(R.id.imageViewListItemMenuFooterIcon);
            TextView titleLogIn = (TextView) menuItemLogin.findViewById(R.id.textViewListItemMenuFooterTitle);
            RelativeLayout layoutLogIn = (RelativeLayout) menuItemLogin.findViewById(R.id.relativeLayoutListItemMenuFooter);

            iconLogIn.setImageResource(R.drawable.icon_profile);
            titleLogIn.setText(R.string.label_sign_in_sign_up);
            layoutLogIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            drawerLayout.closeDrawer(menuLayout);
                        }
                    });
                    onLogInItemClick();
                }
            });

            footerLayout.addView(menuItemLogin);
        }

        ViewGroup.LayoutParams params = menuFooter.getLayoutParams();
        params.height = footerTotalHeight;
        menuFooter.setLayoutParams(params);

        ViewGroup.LayoutParams filterLayoutParams = filter.getLayoutParams();
        filterLayoutParams.height = footerTotalHeight;
        filter.setLayoutParams(filterLayoutParams);
    }

    private void setMenuProfile(LicenseType licenseType) {
        switch (licenseType) {

            case AVOD:
                menuLayout.setBackgroundResource(R.drawable.menu_bg_green);
                avodIndicator.setImageAlpha(255);
                svodIndicator.setImageAlpha(0);
                tvodIndicator.setImageAlpha(0);
                menuIcon.setImageResource(R.drawable.menu_icon_green);
                break;
            case SVOD:
                menuLayout.setBackgroundResource(R.drawable.menu_bg_blue);
                svodIndicator.setImageAlpha(255);
                avodIndicator.setImageAlpha(0);
                tvodIndicator.setImageAlpha(0);
                menuIcon.setImageResource(R.drawable.menu_icon_blue);
                break;
            case TVOD:
                menuLayout.setBackgroundResource(R.drawable.menu_bg_red);
                tvodIndicator.setImageAlpha(255);
                avodIndicator.setImageAlpha(0);
                svodIndicator.setImageAlpha(0);
                menuIcon.setImageResource(R.drawable.menu_icon_red);
                break;
        }
    }

    /**
     * Handle page navigation.
     *
     * @param fragment New page fragment.
     */
    public void navigateToPage(Fragment fragment) {
        Log.d("fragment",fragment.toString());
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout_main, fragment)
                .addToBackStack(fragment.toString())
                .commit();

        currentFragment = null;

        currentFragment = fragment;
    }

    /**
     * Navigate to {@link HomePageFragment}
     *
     * @param licenseType {@link HomePageFragment}
     */
    private void navigateToHomeFragment(final LicenseType licenseType) {

        new Handler(Looper.myLooper()).post(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = HomePageFragment.newInstance(licenseType);
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                //navigateToPage(fragment);
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_main, fragment)
                        .commit();

                currentFragment = null;

                currentFragment = fragment;
            }
        });
        new Handler(Looper.myLooper()).post(new Runnable() {
            @Override
            public void run() {
                setMenuProfile(licenseType);
                loadMenuItem();
                adaptor.setNewMenuItemList(menuItems, subMenuItems);
                drawerLayout.closeDrawer(menuLayout);
            }
        });
    }

    private void selectMenuItemAndNavigate(int groupPosition) {

        Fragment fragment = null;

        if (menuItems.isEmpty())
            return;

        MenuItem menuItem = menuItems.get(groupPosition);

        //if ("Search".equals(menuItem.getTitle())) {
        if ("1".equals(menuItem.getId())) {
            fragment = SearchFragment.newInstance();
        }
//        else if (menuItem.getContent() != null) {
//            if (menuItem.getContent().getType() == Content.Type.content) {
//                ArrayList<String> contentList = new ArrayList<>();
//                contentList.add(menuItem.getContent().getIdentifier());
//                fragment = MovieTrailersFragment.newInstance(menuItem.getTitle(), contentList, null);
//            } else if (menuItem.getContent().getType() == Content.Type.genre) {
//                ArrayList<String> genres = new ArrayList<>();
//                genres.add(menuItem.getContent().getIdentifier());
//                fragment = MovieTrailersFragment.newInstance(menuItem.getTitle(), null, genres);
//            }
//        }
        else{
            ArrayList<String> genres = new ArrayList<>();
            ArrayList<String> categories = new ArrayList<>();
            genres.add(menuItem.getContent().getGenre());
            categories.add(menuItem.getContent().getCategory());
            fragment = MovieTrailersFragment.newInstance(menuItem.getTitle(), categories, genres);
        }

        if (fragment != null)
            navigateToPage(fragment);
    }

    private void selectSubMenuItemAndNavigate(int groupPosition, int childPosition) {
        Fragment fragment = null;
        MenuItem menuItem = menuItems.get(groupPosition);
        MenuItem subMenuItem = subMenuItems.get(menuItem.getId()).get(childPosition);

        if ("Genre".equals(menuItem.getTitle())) {
            ArrayList<String> list = new ArrayList<>();
            list.add(subMenuItem.getTitle());
            fragment = MovieTrailersFragment.newInstance(subMenuItem.getTitle(), null, list);
        }
        if (fragment != null)
            navigateToPage(fragment);
    }

    private void onMyLibraryItemClick() {
        MyLibraryPageFragment fragment = MyLibraryPageFragment.newInstance(licenseType);
        navigateToPage(fragment);
    }

    private void onUserProfileItemClick() {
        UserDetailsFragment fragment = new UserDetailsFragment();
        navigateToPage(fragment);
    }

    private void onLogInItemClick() {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    private class DrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {

        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {

            if (0 == adaptor.getChildrenCount(groupPosition)) {
                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        selectMenuItemAndNavigate(groupPosition);
                    }
                });
                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawer(menuLayout);
                    }
                });
            }
            return false;
        }
    }

    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

            if (adaptor.isChildSelectable(groupPosition, childPosition)) {
                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        selectSubMenuItemAndNavigate(groupPosition, childPosition);
                    }
                });
                new Handler(Looper.myLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        drawerLayout.closeDrawer(menuLayout);
                    }
                });
            }
            return false;
        }
    }
}
