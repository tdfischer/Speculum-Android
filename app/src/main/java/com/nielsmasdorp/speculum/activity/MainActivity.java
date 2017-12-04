package com.nielsmasdorp.speculum.activity;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nielsmasdorp.speculum.chart.GLChart;
import com.nielsmasdorp.speculum.R;
import com.nielsmasdorp.speculum.SpeculumApplication;
import com.nielsmasdorp.speculum.models.Configuration;
import com.nielsmasdorp.speculum.models.RedditPost;
import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.models.octoprint.Job;
import com.nielsmasdorp.speculum.presenters.MainPresenter;
import com.nielsmasdorp.speculum.services.SNMPService;
import com.nielsmasdorp.speculum.util.ASFObjectStore;
import com.nielsmasdorp.speculum.util.Constants;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener {

    // @formatter:off
    @BindView(R.id.iv_current_weather) ImageView ivWeatherCondition;
    @BindView(R.id.tv_current_temp) TextView tvWeatherTemperature;
    @BindView(R.id.weather_layout) LinearLayout llWeatherLayout;
    @BindView(R.id.iv_listening) ImageView ivListening;

    @Nullable @BindView(R.id.tv_summary) TextView tvWeatherSummary;
    @Nullable @BindView(R.id.calendar_layout) LinearLayout llCalendarLayout;
    @Nullable @BindView(R.id.reddit_layout) LinearLayout llRedditLayout;
    @Nullable @BindView(R.id.iv_forecast_weather1) ImageView ivDayOneIcon;
    @Nullable @BindView(R.id.tv_forecast_temp1) TextView tvDayOneTemperature;
    @Nullable @BindView(R.id.tv_forecast_date1) TextView tvDayOneDate;
    @Nullable @BindView(R.id.iv_forecast_weather2) ImageView ivDayTwoIcon;
    @Nullable @BindView(R.id.tv_forecast_temp2) TextView tvDayTwoTemperature;
    @Nullable @BindView(R.id.tv_forecast_date2) TextView tvDayTwoDate;
    @Nullable @BindView(R.id.iv_forecast_weather3) ImageView ivDayThreeIcon;
    @Nullable @BindView(R.id.tv_forecast_temp3) TextView tvDayThreeTemperature;
    @Nullable @BindView(R.id.tv_forecast_date3) TextView tvDayThreeDate;
    @Nullable @BindView(R.id.iv_forecast_weather4) ImageView ivDayFourIcon;
    @Nullable @BindView(R.id.tv_forecast_temp4) TextView tvDayFourTemperature;
    @Nullable @BindView(R.id.tv_forecast_date4) TextView tvDayFourDate;
    @Nullable @BindView(R.id.tv_calendar_event) TextView tvCalendarEvent;
    @Nullable @BindView(R.id.tv_reddit_post_title) TextView tvRedditPostTitle;
    @Nullable @BindView(R.id.tv_reddit_post_votes) TextView tvRedditPostVotes;
    @Nullable @BindView(R.id.tv_printer_status) TextView tvPrinterStatus;
    @Nullable @BindView(R.id.pb_printer_progress) ProgressBar pbPrinterProgress;
    @Nullable @BindView(R.id.tv_printer_eta) TextView tvPrinterETA;
    @Nullable @BindView(R.id.lc_net_activity) GLChart lcNetActivity;

    @BindString(R.string.old_config_found_snackbar) String oldConfigFound;
    @BindString(R.string.old_config_found_snackbar_back) String getOldConfigFoundBack;
    @BindString(R.string.give_command) String giveCommand;
    @BindString(R.string.listening) String listening;
    @BindString(R.string.command_understood) String commandUnderstood;
    @BindString(R.string.executing) String executing;
    @BindString(R.string.last_updated) String lastUpdated;

    // @formatter:on

    @Inject
    MainPresenter presenter;

    @Inject
    ASFObjectStore<Configuration> objectStore;

    MaterialDialog mapDialog;

    ObjectAnimator pulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verbose_layout);
        ((SpeculumApplication) getApplication()).createMainComponent(this).inject(this);
        Assent.setActivity(this, this);
        objectStore.setObject(new Configuration.Builder().build());

        LineData lineData = new LineData();
        LineDataSet downloadSet = new LineDataSet(null, "Download");
        downloadSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        downloadSet.setColor(Color.GREEN);
        downloadSet.setDrawValues(false);
        downloadSet.setLineWidth(1.5f);
        downloadSet.setFillAlpha(65);
        downloadSet.setFillColor(Color.GREEN);
        downloadSet.setDrawCircles(false);
        downloadSet.setDrawFilled(true);
        lineData.addDataSet(downloadSet);

        LineDataSet uploadSet = new LineDataSet(null, "Upload");
        uploadSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        uploadSet.setColor(Color.YELLOW);
        uploadSet.setDrawValues(false);
        uploadSet.setDrawFilled(true);
        uploadSet.setLineWidth(1.5f);
        uploadSet.setFillAlpha(65);
        uploadSet.setFillColor(Color.YELLOW);
        uploadSet.setDrawCircles(false);
        lineData.addDataSet(uploadSet);

        for(int i = 0; i < 200; i++) {
            uploadSet.addEntry(new Entry(i, 0));
            downloadSet.addEntry(new Entry(i, 0));
        }

        Configuration configuration = objectStore.get();
        boolean didLoadOldConfig = getIntent().getBooleanExtra(Constants.SAVED_CONFIGURATION_IDENTIFIER, false);

        /*ViewStub viewStub = configuration.isSimpleLayout() ?
                (ViewStub) findViewById(R.id.stub_simple) :
                (ViewStub) findViewById(R.id.stub_verbose);
        if (null != viewStub) viewStub.inflate();*/

        ButterKnife.bind(this);

        //never sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (didLoadOldConfig)
            showConfigurationSnackbar();

        presenter.setConfiguration(configuration);

        /*LineChart chart = (LineChart) findViewById(R.id.lc_net_activity);
        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setRenderer(new GLRendererWrapper(chart.getRenderer(), chart.getAnimator(), chart.getViewPortHandler()));*/
        //GLSurfaceView glSurfaceView;);
        //GLChart glChart;
        //glChart.setData(lineData);
    }

    private void showConfigurationSnackbar() {
        Snackbar
                .make(llWeatherLayout, oldConfigFound, Snackbar.LENGTH_LONG)
                .setAction(getOldConfigFoundBack, view -> onBackPressed())
                .show();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUI() {
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mDecorView.setOnSystemUiVisibilityChangeListener(this);
    }

    public void showListening() {
        ivListening.setVisibility(View.VISIBLE);
        ivListening.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
    }

    public void hideListening() {
        ivListening.clearAnimation();
        ivListening.setVisibility(View.INVISIBLE);
    }

    @SuppressWarnings("all")
    public void displayCurrentWeather(Weather weather, boolean isSimpleLayout) {

        // Current simple weather
        this.ivWeatherCondition.setImageResource(weather.getIconId());
        this.tvWeatherTemperature.setText(weather.getTemperature());

        if (!isSimpleLayout) {
            // Forecast
            this.tvDayOneDate.setText(weather.getForecast().get(0).getDate());
            this.tvDayOneTemperature.setText(weather.getForecast().get(0).getTemperature());
            this.ivDayOneIcon.setImageResource(weather.getForecast().get(0).getIconId());
            this.tvDayTwoDate.setText(weather.getForecast().get(1).getDate());
            this.tvDayTwoTemperature.setText(weather.getForecast().get(1).getTemperature());
            this.ivDayTwoIcon.setImageResource(weather.getForecast().get(1).getIconId());
            this.tvDayThreeDate.setText(weather.getForecast().get(2).getDate());
            this.tvDayThreeTemperature.setText(weather.getForecast().get(2).getTemperature());
            this.ivDayThreeIcon.setImageResource(weather.getForecast().get(2).getIconId());
            this.tvDayFourDate.setText(weather.getForecast().get(3).getDate());
            this.tvDayFourTemperature.setText(weather.getForecast().get(3).getTemperature());
            this.ivDayFourIcon.setImageResource(weather.getForecast().get(2).getIconId());
        } else {
            this.tvWeatherSummary.setText(weather.getSummary());
        }

        if (this.llWeatherLayout.getVisibility() != View.VISIBLE) {
            this.llWeatherLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("all")
    public void displayTopRedditPost(RedditPost redditPost) {
        tvRedditPostTitle.setText(redditPost.getTitle());
        tvRedditPostVotes.setText(redditPost.getUps() + "");
        if (this.llRedditLayout.getVisibility() != View.VISIBLE)
            this.llRedditLayout.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("all")
    public void displayCalendarEvents(String events) {
        this.tvCalendarEvent.setText(events);
        if (this.llCalendarLayout.getVisibility() != View.VISIBLE)
            this.llCalendarLayout.setVisibility(View.VISIBLE);
    }

    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Assent.setActivity(this, this);
        hideSystemUI();
        presenter.start(Assent.isPermissionGranted(Assent.READ_CALENDAR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.finish();
        if (isFinishing())
            Assent.setActivity(this, null);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            hideSystemUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((SpeculumApplication) getApplication()).releaseMainComponent();
    }

    float lastDownload = 0;
    float lastUpload = 0;

    ArrayList<Float> downloadSeries;

    public void displayNetActivity(SNMPService.NetActivity activity) {
        if (downloadSeries == null) {
            downloadSeries = new ArrayList<>(200);
            for(int i = 0; i < 200; i++) {
                downloadSeries.add(0f);
            }
        }

        if (lastDownload == 0) {
            lastDownload = activity.download;
            lastUpload = activity.upload;
        }

        float downloadDelta = activity.download - lastDownload;
        float uploadDelta = activity.upload - lastUpload;
        lastDownload = activity.download;
        lastUpload = activity.upload;

        lcNetActivity.push(lastDownload);

        /*downloadSeries.add(downloadDelta);

        while(downloadSeries.size() > 200) {
            downloadSeries.remove(0);
        }

        float[] chartData = new float[downloadSeries.size()];
        for(int i = 0; i < downloadSeries.size(); i++) {
            chartData[i] = downloadSeries.get(i);
        }*/

        //lcNetActivity.setData(chartData);

        /*LineData data = lcNetActivity.getData();
        ILineDataSet downloadSet = data.getDataSetByIndex(0);
        ILineDataSet uploadSet = data.getDataSetByIndex(1);

        if (lastDownload == 0) {
            lastDownload = activity.download;
            lastUpload = activity.upload;
        }

        float downloadDelta = activity.download - lastDownload;
        float uploadDelta = activity.upload - lastUpload;
        lastDownload = activity.download;
        lastUpload = activity.upload;


        downloadSet.addEntry(new Entry(downloadSet.getXMax()+1, downloadDelta));
        uploadSet.addEntry(new Entry(uploadSet.getXMax()+1, -uploadDelta));
        while (downloadSet.getEntryCount() > 200) {
            downloadSet.removeFirst();
        }
        while (uploadSet.getEntryCount() > 200) {
            uploadSet.removeFirst();
        }

        data.notifyDataChanged();
        lcNetActivity.notifyDataSetChanged();
        lcNetActivity.setAutoScaleMinMaxEnabled(true);
        lcNetActivity.setVisibleXRangeMaximum(250);
        lcNetActivity.moveViewToX(downloadSet.getEntryCount());*/
    }

    public void displayPrinterJob(Job job) {
        tvPrinterStatus.setText("Printer is " + job.getState() + ".");
        if (job.getProgress().getCompletion() != null) {
            pbPrinterProgress.setProgress((int) ((double)job.getProgress().getCompletion()));
            pbPrinterProgress.setVisibility(View.VISIBLE);
            int printTime = (int) ((double) job.getProgress().getPrintTimeLeft());
            String printEta = DateUtils.formatElapsedTime(printTime) + " remaining";
            tvPrinterETA.setText(printEta);
            tvPrinterETA.setVisibility(View.VISIBLE);
        } else {
            pbPrinterProgress.setVisibility(View.INVISIBLE);
            tvPrinterETA.setVisibility(View.INVISIBLE);
        }
    }
}
