package org.smartregister.chw.agyw.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.agyw.R;
import org.smartregister.chw.agyw.contract.AGYWProfileContract;
import org.smartregister.chw.agyw.custom_views.BaseAGYWFloatingMenu;
import org.smartregister.chw.agyw.dao.AGYWDao;
import org.smartregister.chw.agyw.domain.MemberObject;
import org.smartregister.chw.agyw.interactor.BaseAGYWProfileInteractor;
import org.smartregister.chw.agyw.listener.OnClickFloatingMenu;
import org.smartregister.chw.agyw.presenter.BaseAGYWProfilePresenter;
import org.smartregister.chw.agyw.util.AGYWUtil;
import org.smartregister.chw.agyw.util.Constants;
import org.smartregister.domain.AlertStatus;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public class BaseAGYWProfileActivity extends BaseProfileActivity implements AGYWProfileContract.View, AGYWProfileContract.InteractorCallBack {
    protected MemberObject memberObject;
    protected AGYWProfileContract.Presenter profilePresenter;
    protected CircleImageView imageView;
    protected TextView textViewName;
    protected TextView textViewGender;
    protected TextView textViewLocation;
    protected TextView textViewUniqueID;
    protected TextView textViewRecordAgyw;
    protected TextView textViewRecordAnc;
    protected TextView textview_positive_date;
    protected TextView textView_package_status;
    protected TextView textview_graduate;
    protected View view_last_visit_row;
    protected View view_most_due_overdue_row;
    protected View view_family_row;
    protected View view_positive_date_row;
    protected RelativeLayout rlLastVisit;
    protected RelativeLayout rlUpcomingServices;
    protected RelativeLayout rlFamilyServicesDue;
    protected RelativeLayout visitStatus;
    protected ImageView imageViewCross;
    protected TextView textViewUndo;
    protected RelativeLayout rlAgywPositiveDate;
    protected TextView textViewVisitDone;
    protected RelativeLayout visitDone;
    protected LinearLayout recordVisits;
    protected TextView textViewVisitDoneEdit;
    protected TextView textViewRecordAncNotDone;
    protected BaseAGYWFloatingMenu baseAGYWFloatingMenu;
    protected TextView textViewUICID;
    private TextView tvUpComingServices;
    private TextView tvFamilyStatus;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private ProgressBar progressBar;

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, BaseAGYWProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_agyw_profile);
        Toolbar toolbar = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        toolbar.setNavigationOnClickListener(v -> BaseAGYWProfileActivity.this.finish());
        appBarLayout = this.findViewById(R.id.collapsing_toolbar_appbarlayout);
        if (Build.VERSION.SDK_INT >= 21) {
            appBarLayout.setOutlineProvider(null);
        }

        textViewName = findViewById(R.id.textview_name);
        textViewGender = findViewById(R.id.textview_gender);
        textViewLocation = findViewById(R.id.textview_address);
        textViewUniqueID = findViewById(R.id.textview_id);
        view_last_visit_row = findViewById(R.id.view_last_visit_row);
        view_most_due_overdue_row = findViewById(R.id.view_most_due_overdue_row);
        view_family_row = findViewById(R.id.view_family_row);
        view_positive_date_row = findViewById(R.id.view_positive_date_row);
        imageViewCross = findViewById(R.id.tick_image);
        tvUpComingServices = findViewById(R.id.textview_name_due);
        tvFamilyStatus = findViewById(R.id.textview_family_has);
        textview_positive_date = findViewById(R.id.textview_positive_date);
        rlLastVisit = findViewById(R.id.rlLastVisit);
        rlUpcomingServices = findViewById(R.id.rlUpcomingServices);
        rlFamilyServicesDue = findViewById(R.id.rlFamilyServicesDue);
        rlAgywPositiveDate = findViewById(R.id.rlAgywPositiveDate);
        textViewVisitDone = findViewById(R.id.textview_visit_done);
        visitStatus = findViewById(R.id.record_visit_not_done_bar);
        visitDone = findViewById(R.id.visit_done_bar);
        recordVisits = findViewById(R.id.record_visits);
        progressBar = findViewById(R.id.progress_bar);
        textViewRecordAncNotDone = findViewById(R.id.textview_record_anc_not_done);
        textViewVisitDoneEdit = findViewById(R.id.textview_edit);
        textViewRecordAgyw = findViewById(R.id.textview_record_agyw);
        textViewRecordAnc = findViewById(R.id.textview_record_anc);
        textViewUndo = findViewById(R.id.textview_undo);
        imageView = findViewById(R.id.imageview_profile);
        textView_package_status = findViewById(R.id.package_status);
        textview_graduate = findViewById(R.id.textview_graduate);
        textViewUICID = findViewById(R.id.textview_uic_id);

        textViewRecordAncNotDone.setOnClickListener(this);
        textViewVisitDoneEdit.setOnClickListener(this);
        rlLastVisit.setOnClickListener(this);
        rlUpcomingServices.setOnClickListener(this);
        rlFamilyServicesDue.setOnClickListener(this);
        rlAgywPositiveDate.setOnClickListener(this);
        textViewRecordAgyw.setOnClickListener(this);
        textViewRecordAnc.setOnClickListener(this);
        textViewUndo.setOnClickListener(this);
        textview_graduate.setOnClickListener(this);

        imageRenderHelper = new ImageRenderHelper(this);
        memberObject = AGYWDao.getMember(baseEntityId);
        initializePresenter();
        profilePresenter.fillProfileData(memberObject);
        setupViews();
    }

    @Override
    protected void setupViews() {
        initializeFloatingMenu();
        initializePackageStatusView();
        initializeGraduateServicesView();
        recordAnc(memberObject);
        recordPnc(memberObject);
        showUICID(memberObject.getBaseEntityId());
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
    }

    public void initializeGraduateServicesView() {
        boolean isEligibleToGraduate = AGYWDao.isEligibleToGraduateServices(memberObject.getBaseEntityId(), memberObject.getAge());
        if (isEligibleToGraduate) {
            textview_graduate.setVisibility(View.VISIBLE);
        } else {
            textview_graduate.setVisibility(View.GONE);
        }
    }

    protected void initializePackageStatusView() {
        String status = getAGYWPackageStatus();
        if (StringUtils.isNotBlank(status)) {
            textView_package_status.setVisibility(View.VISIBLE);
            textView_package_status.setText(status);
        }
    }

    protected String getAGYWPackageStatus() {
        int status_id = AGYWDao.getPackageStatus(memberObject.getBaseEntityId(), memberObject.getAge());
        if (status_id != 0)
            return getResources().getString(status_id);
        return "";
    }

    protected void showUICID(String baseEntityId) {
        String UIC_ID = AGYWDao.getUIC_ID(baseEntityId);
        if (StringUtils.isNotBlank(UIC_ID)) {
            textViewUICID.setVisibility(View.VISIBLE);
            textViewUICID.setText(getString(R.string.uic_id, UIC_ID.toUpperCase(Locale.ROOT)));
        } else {
            textViewUICID.setVisibility(View.GONE);
        }
    }

    @Override
    public void recordAnc(MemberObject memberObject) {
        //implement
    }

    @Override
    public void recordPnc(MemberObject memberObject) {
        //implement
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.title_layout) {
            onBackPressed();
        } else if (id == R.id.rlLastVisit) {
            this.openMedicalHistory();
        } else if (id == R.id.rlUpcomingServices) {
            this.openUpcomingService();
        } else if (id == R.id.rlFamilyServicesDue) {
            this.openFamilyDueServices();
        } else if (id == R.id.textview_record_agyw) {
            this.startAGYWServices();
        } else if (id == R.id.textview_graduate) {
            profilePresenter.graduateServices(memberObject.getBaseEntityId());
            finish();
        }
    }

    protected void startAGYWServices() {
        BaseAGYWServicesActivity.startMe(this, memberObject.getBaseEntityId());
    }


    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        profilePresenter = new BaseAGYWProfilePresenter(this, new BaseAGYWProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    public void initializeFloatingMenu() {
        baseAGYWFloatingMenu = new BaseAGYWFloatingMenu(this, memberObject);
        checkPhoneNumberProvided(StringUtils.isNotBlank(memberObject.getPhoneNumber()));
        if (showReferralView()) {
            baseAGYWFloatingMenu.findViewById(R.id.refer_to_facility_layout).setVisibility(View.VISIBLE);
        } else {
            baseAGYWFloatingMenu.findViewById(R.id.refer_to_facility_layout).setVisibility(View.GONE);
        }
        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            if (viewId == R.id.agyw_fab) {
                //Animates the actual FAB
                baseAGYWFloatingMenu.animateFAB();
            } else if (viewId == R.id.call_layout) {
                baseAGYWFloatingMenu.launchCallWidget();
                baseAGYWFloatingMenu.animateFAB();
            } else if (viewId == R.id.refer_to_facility_layout) {
                startReferralForm();
            } else {
                Timber.d("Unknown FAB action");
            }
        };

        baseAGYWFloatingMenu.setFloatMenuClickListener(onClickFloatingMenu);
        baseAGYWFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(baseAGYWFloatingMenu, linearLayoutParams);
    }

    private void checkPhoneNumberProvided(boolean hasPhoneNumber) {
        BaseAGYWFloatingMenu.redrawWithOption(baseAGYWFloatingMenu, hasPhoneNumber);
    }

    protected boolean showReferralView() {
        //in chw return true; in hf return false;
        return true;
    }

    public void startReferralForm() {
        //implement in chw
    }

    @Override
    public void hideView() {
        textViewRecordAgyw.setVisibility(View.GONE);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void setProfileViewWithData() {
        textViewName.setText(String.format("%s %s %s, %d", memberObject.getFirstName(),
                memberObject.getMiddleName(), memberObject.getLastName(), memberObject.getAge()));
        textViewGender.setText(AGYWUtil.getGenderTranslated(this, memberObject.getGender()));
        textViewLocation.setText(memberObject.getAddress());
        textViewUniqueID.setText(memberObject.getUniqueId());

        if (memberObject.getAgywTestDate() != null) {
            textview_positive_date.setText(getString(R.string.agyw_positive) + " " + formatTime(memberObject.getAgywTestDate()));
        }
    }

    @Override
    public void setOverDueColor() {
        textViewRecordAgyw.setBackground(getResources().getDrawable(R.drawable.record_btn_selector_overdue));
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {
        //fetch profile data
    }

    @Override
    public void showProgressBar(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        showProgressBar(false);
        rlLastVisit.setVisibility(View.GONE);
    }

    @Override
    public void refreshUpComingServicesStatus(String service, AlertStatus status, Date date) {
        showProgressBar(false);
        if (status == AlertStatus.complete)
            return;
        view_most_due_overdue_row.setVisibility(View.GONE);
        rlUpcomingServices.setVisibility(View.GONE);

        if (status == AlertStatus.upcoming) {
            tvUpComingServices.setText(AGYWUtil.fromHtml(getString(R.string.vaccine_service_upcoming, service, dateFormat.format(date))));
        } else {
            tvUpComingServices.setText(AGYWUtil.fromHtml(getString(R.string.vaccine_service_due, service, dateFormat.format(date))));
        }
    }

    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        showProgressBar(false);
        if (status == AlertStatus.complete) {
            setFamilyStatus(getString(R.string.family_has_nothing_due));
        } else if (status == AlertStatus.normal) {
            setFamilyStatus(getString(R.string.family_has_services_due));
        } else if (status == AlertStatus.urgent) {
            tvFamilyStatus.setText(AGYWUtil.fromHtml(getString(R.string.family_has_service_overdue)));
        }
    }

    private void setFamilyStatus(String familyStatus) {
        view_family_row.setVisibility(View.GONE);
        rlFamilyServicesDue.setVisibility(View.GONE);
        tvFamilyStatus.setText(familyStatus);
    }

    @Override
    public void openMedicalHistory() {
        //implement
    }

    @Override
    public void openUpcomingService() {
        //implement
    }

    @Override
    public void openFamilyDueServices() {
        //implement
    }

    @Nullable
    private String formatTime(Date dateTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
            return formatter.format(dateTime);
        } catch (Exception e) {
            Timber.d(e);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            profilePresenter.saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
            finish();
        }
    }
}
