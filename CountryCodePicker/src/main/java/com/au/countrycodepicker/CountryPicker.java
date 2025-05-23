package com.au.countrycodepicker;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.au.countrycodepicker.listeners.BottomSheetInteractionListener;
import com.au.countrycodepicker.listeners.OnCountryPickerListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CountryPicker implements BottomSheetInteractionListener, LifecycleObserver {

    // region Countries
    private final Country[] COUNTRIES = {

            new Country("AD", "Andorra", "+376", R.drawable.flag_ad, "EUR"),
            new Country("AE", "United Arab Emirates", "+971", R.drawable.flag_ae, "AED"),
            new Country("AF", "Afghanistan", "+93", R.drawable.flag_af, "AFN"),
            new Country("AG", "Antigua and Barbuda", "+1-268", R.drawable.flag_ag, "XCD"),
            new Country("AI", "Anguilla", "+1-264", R.drawable.flag_ai, "XCD"),
            new Country("AL", "Albania", "+355", R.drawable.flag_al, "ALL"),
            new Country("AM", "Armenia", "+374", R.drawable.flag_am, "AMD"),
            new Country("AN", "Netherlands Antilles", "+599", R.drawable.flag_an, "ANG"),
            new Country("AO", "Angola", "+244", R.drawable.flag_ao, "AOA"),
            new Country("AQ", "Antarctica", "+672", R.drawable.flag_aq, "USD"),
            new Country("AR", "Argentina", "+54", R.drawable.flag_ar, "ARS"),
            new Country("AS", "American Samoa", "+1-684", R.drawable.flag_as, "USD"),
            new Country("AT", "Austria", "+43", R.drawable.flag_at, "EUR"),
            new Country("AU", "Australia", "+61", R.drawable.flag_au, "AUD"),
            new Country("AW", "Aruba", "+297", R.drawable.flag_aw, "AWG"),
            new Country("AZ", "Azerbaijan", "+994", R.drawable.flag_az, "AZN"),
            new Country("BA", "Bosnia and Herzegovina", "+387", R.drawable.flag_ba, "BAM"),
            new Country("BB", "Barbados", "+1-246", R.drawable.flag_bb, "BBD"),
            new Country("BD", "Bangladesh", "+880", R.drawable.flag_bd, "BDT"),
            new Country("BE", "Belgium", "+32", R.drawable.flag_be, "EUR"),
            new Country("BF", "Burkina Faso", "+226", R.drawable.flag_bf, "XOF"),
            new Country("BG", "Bulgaria", "+359", R.drawable.flag_bg, "BGN"),
            new Country("BH", "Bahrain", "+973", R.drawable.flag_bh, "BHD"),
            new Country("BI", "Burundi", "+257", R.drawable.flag_bi, "BIF"),
            new Country("BJ", "Benin", "+229", R.drawable.flag_bj, "XOF"),
            new Country("BL", "Saint Barthelemy", "+590", R.drawable.flag_bl, "EUR"),
            new Country("BM", "Bermuda", "+1-441", R.drawable.flag_bm, "BMD"),
            new Country("BN", "Brunei", "+673", R.drawable.flag_bn, "BND"),
            new Country("BO", "Bolivia", "+591", R.drawable.flag_bo, "BOB"),
            new Country("BR", "Brazil", "+55", R.drawable.flag_br, "BRL"),
            new Country("BS", "Bahamas", "+1-242", R.drawable.flag_bs, "BSD"),
            new Country("BT", "Bhutan", "+975", R.drawable.flag_bt, "BTN"),
            new Country("BW", "Botswana", "+267", R.drawable.flag_bw, "BWP"),
            new Country("BY", "Belarus", "+375", R.drawable.flag_by, "BYR"),
            new Country("BZ", "Belize", "+501", R.drawable.flag_bz, "BZD"),
            new Country("CA", "Canada", "+1", R.drawable.flag_ca, "CAD"),
            new Country("CC", "Cocos Islands", "+61", R.drawable.flag_cc, "AUD"),
            new Country("CD", "Democratic Republic of the Congo", "+243", R.drawable.flag_cd, "CDF"),
            new Country("CF", "Central African Republic", "+236", R.drawable.flag_cf, "XAF"),
            new Country("CG", "Republic of the Congo", "+242", R.drawable.flag_cg, "XAF"),
            new Country("CH", "Switzerland", "+41", R.drawable.flag_ch, "CHF"),
            new Country("CI", "Ivory Coast", "+225", R.drawable.flag_ci, "XOF"),
            new Country("CK", "Cook Islands", "+682", R.drawable.flag_ck, "NZD"),
            new Country("CL", "Chile", "+56", R.drawable.flag_cl, "CLP"),
            new Country("CM", "Cameroon", "+237", R.drawable.flag_cm, "XAF"),
            new Country("CN", "China", "+86", R.drawable.flag_cn, "CNY"),
            new Country("CO", "Colombia", "+57", R.drawable.flag_co, "COP"),
            new Country("CR", "Costa Rica", "+506", R.drawable.flag_cr, "CRC"),
            new Country("CU", "Cuba", "+53", R.drawable.flag_cu, "CUP"),
            new Country("CV", "Cape Verde", "+238", R.drawable.flag_cv, "CVE"),
            new Country("CW", "Curacao", "+599", R.drawable.flag_cw, "ANG"),
            new Country("CX", "Christmas Island", "+61", R.drawable.flag_cx, "AUD"),
            new Country("CY", "Cyprus", "+357", R.drawable.flag_cy, "EUR"),
            new Country("CZ", "Czech Republic", "+420", R.drawable.flag_cz, "CZK"),
            new Country("DE", "Germany", "+49", R.drawable.flag_de, "EUR"),
            new Country("DJ", "Djibouti", "+253", R.drawable.flag_dj, "DJF"),
            new Country("DK", "Denmark", "+45", R.drawable.flag_dk, "DKK"),
            new Country("DM", "Dominica", "+1-767", R.drawable.flag_dm, "XCD"),
            new Country("DO", "Dominican Republic", "+1-809, +1-829, +1-849", R.drawable.flag_do, "DOP"),
            new Country("DZ", "Algeria", "+213", R.drawable.flag_dz, "DZD"),
            new Country("EC", "Ecuador", "+593", R.drawable.flag_ec, "USD"),
            new Country("EE", "Estonia", "+372", R.drawable.flag_ee, "EUR"),
            new Country("EG", "Egypt", "+20", R.drawable.flag_eg, "EGP"),
            new Country("EH", "Western Sahara", "+212", R.drawable.flag_eh, "MAD"),
            new Country("ER", "Eritrea", "+291", R.drawable.flag_er, "ERN"),
            new Country("ES", "Spain", "+34", R.drawable.flag_es, "EUR"),
            new Country("ET", "Ethiopia", "+251", R.drawable.flag_et, "ETB"),
            new Country("FI", "Finland", "+358", R.drawable.flag_fi, "EUR"),
            new Country("FJ", "Fiji", "+679", R.drawable.flag_fj, "FJD"),
            new Country("FK", "Falkland Islands", "+500", R.drawable.flag_fk, "FKP"),
            new Country("FM", "Micronesia", "+691", R.drawable.flag_fm, "USD"),
            new Country("FO", "Faroe Islands", "+298", R.drawable.flag_fo, "DKK"),
            new Country("FR", "France", "+33", R.drawable.flag_fr, "EUR"),
            new Country("GA", "Gabon", "+241", R.drawable.flag_ga, "XAF"),
            new Country("GB", "United Kingdom", "+44", R.drawable.flag_gb, "GBP"),
            new Country("GD", "Grenada", "+1-473", R.drawable.flag_gd, "XCD"),
            new Country("GE", "Georgia", "+995", R.drawable.flag_ge, "GEL"),
            new Country("GG", "Guernsey", "+44-1481", R.drawable.flag_gg, "GGP"),
            new Country("GH", "Ghana", "+233", R.drawable.flag_gh, "GHS"),
            new Country("GI", "Gibraltar", "+350", R.drawable.flag_gi, "GIP"),
            new Country("GL", "Greenland", "+299", R.drawable.flag_gl, "DKK"),
            new Country("GM", "Gambia", "+220", R.drawable.flag_gm, "GMD"),
            new Country("GN", "Guinea", "+224", R.drawable.flag_gn, "GNF"),
            new Country("GQ", "Equatorial Guinea", "+240", R.drawable.flag_gq, "XAF"),
            new Country("GR", "Greece", "+30", R.drawable.flag_gr, "EUR"),
            new Country("GT", "Guatemala", "+502", R.drawable.flag_gt, "GTQ"),
            new Country("GU", "Guam", "+1-671", R.drawable.flag_gu, "USD"),
            new Country("GW", "Guinea-Bissau", "+245", R.drawable.flag_gw, "XOF"),
            new Country("GY", "Guyana", "+592", R.drawable.flag_gy, "GYD"),
            new Country("HK", "Hong Kong", "+852", R.drawable.flag_hk, "HKD"),
            new Country("HN", "Honduras", "+504", R.drawable.flag_hn, "HNL"),
            new Country("HR", "Croatia", "+385", R.drawable.flag_hr, "HRK"),
            new Country("HT", "Haiti", "+509", R.drawable.flag_ht, "HTG"),
            new Country("HU", "Hungary", "+36", R.drawable.flag_hu, "HUF"),
            new Country("ID", "Indonesia", "+62", R.drawable.flag_id, "IDR"),
            new Country("IE", "Ireland", "+353", R.drawable.flag_ie, "EUR"),
            new Country("IL", "Israel", "+972", R.drawable.flag_il, "ILS"),
            new Country("IM", "Isle of Man", "+44-1624", R.drawable.flag_im, "GBP"),
            new Country("IN", "India", "+91", R.drawable.flag_in, "INR"),
            new Country("IO", "British Indian Ocean Territory", "+246", R.drawable.flag_io, "USD"),
            new Country("IQ", "Iraq", "+964", R.drawable.flag_iq, "IQD"),
            new Country("IR", "Iran", "+98", R.drawable.flag_ir, "IRR"),
            new Country("IS", "Iceland", "+354", R.drawable.flag_is, "ISK"),
            new Country("IT", "Italy", "+39", R.drawable.flag_it, "EUR"),
            new Country("JE", "Jersey", "+44-1534", R.drawable.flag_je, "JEP"),
            new Country("JM", "Jamaica", "+1-876", R.drawable.flag_jm, "JMD"),
            new Country("JO", "Jordan", "+962", R.drawable.flag_jo, "JOD"),
            new Country("JP", "Japan", "+81", R.drawable.flag_jp, "JPY"),
            new Country("KE", "Kenya", "+254", R.drawable.flag_ke, "KES"),
            new Country("KG", "Kyrgyzstan", "+996", R.drawable.flag_kg, "KGS"),
            new Country("KH", "Cambodia", "+855", R.drawable.flag_kh, "KHR"),
            new Country("KI", "Kiribati", "+686", R.drawable.flag_ki, "AUD"),
            new Country("KM", "Comoros", "+269", R.drawable.flag_km, "KMF"),
            new Country("KN", "Saint Kitts and Nevis", "+1-869", R.drawable.flag_kn, "XCD"),
            new Country("KP", "North Korea", "+850", R.drawable.flag_kp, "KPW"),
            new Country("KR", "South Korea", "+82", R.drawable.flag_kr, "KRW"),
            new Country("KW", "Kuwait", "+965", R.drawable.flag_kw, "KWD"),
            new Country("KY", "Cayman Islands", "+1-345", R.drawable.flag_ky, "KYD"),
            new Country("KZ", "Kazakhstan", "+7", R.drawable.flag_kz, "KZT"),
            new Country("LA", "Laos", "+856", R.drawable.flag_la, "LAK"),
            new Country("LB", "Lebanon", "+961", R.drawable.flag_lb, "LBP"),
            new Country("LC", "Saint Lucia", "+1-758", R.drawable.flag_lc, "XCD"),
            new Country("LI", "Liechtenstein", "+423", R.drawable.flag_li, "CHF"),
            new Country("LK", "Sri Lanka", "+94", R.drawable.flag_lk, "LKR"),
            new Country("LR", "Liberia", "+231", R.drawable.flag_lr, "LRD"),
            new Country("LS", "Lesotho", "+266", R.drawable.flag_ls, "LSL"),
            new Country("LT", "Lithuania", "+370", R.drawable.flag_lt, "LTL"),
            new Country("LU", "Luxembourg", "+352", R.drawable.flag_lu, "EUR"),
            new Country("LV", "Latvia", "+371", R.drawable.flag_lv, "LVL"),
            new Country("LY", "Libya", "+218", R.drawable.flag_ly, "LYD"),
            new Country("MA", "Morocco", "+212", R.drawable.flag_ma, "MAD"),
            new Country("MC", "Monaco", "+377", R.drawable.flag_mc, "EUR"),
            new Country("MD", "Moldova", "+373", R.drawable.flag_md, "MDL"),
            new Country("ME", "Montenegro", "+382", R.drawable.flag_me, "EUR"),
            new Country("MF", "Saint Martin", "+590", R.drawable.flag_mf, "EUR"),
            new Country("MG", "Madagascar", "+261", R.drawable.flag_mg, "MGA"),
            new Country("MH", "Marshall Islands", "+692", R.drawable.flag_mh, "USD"),
            new Country("MK", "Macedonia", "+389", R.drawable.flag_mk,
                    "MKD"),
            new Country("ML", "Mali", "+223", R.drawable.flag_ml, "XOF"),
            new Country("MM", "Myanmar", "+95", R.drawable.flag_mm, "MMK"),
            new Country("MN", "Mongolia", "+976", R.drawable.flag_mn, "MNT"),
            new Country("MO", "Macao", "+853", R.drawable.flag_mo, "MOP"),
            new Country("MP", "Northern Mariana Islands", "+1-670", R.drawable.flag_mp, "USD"),
            new Country("MR", "Mauritania", "+222", R.drawable.flag_mr, "MRO"),
            new Country("MS", "Montserrat", "+1-664", R.drawable.flag_ms, "XCD"),
            new Country("MT", "Malta", "+356", R.drawable.flag_mt, "EUR"),
            new Country("MU", "Mauritius", "+230", R.drawable.flag_mu, "MUR"),
            new Country("MV", "Maldives", "+960", R.drawable.flag_mv, "MVR"),
            new Country("MW", "Malawi", "+265", R.drawable.flag_mw, "MWK"),
            new Country("MX", "Mexico", "+52", R.drawable.flag_mx, "MXN"),
            new Country("MY", "Malaysia", "+60", R.drawable.flag_my, "MYR"),
            new Country("MZ", "Mozambique", "+258", R.drawable.flag_mz, "MZN"),
            new Country("NA", "Namibia", "+264", R.drawable.flag_na, "NAD"),
            new Country("NC", "New Caledonia", "+687", R.drawable.flag_nc, "XPF"),
            new Country("NE", "Niger", "+227", R.drawable.flag_ne, "XOF"),
            new Country("NG", "Nigeria", "+234", R.drawable.flag_ng, "NGN"),
            new Country("NI", "Nicaragua", "+505", R.drawable.flag_ni, "NIO"),
            new Country("NL", "Netherlands", "+31", R.drawable.flag_nl, "EUR"),
            new Country("NO", "Norway", "+47", R.drawable.flag_no, "NOK"),
            new Country("NP", "Nepal", "+977", R.drawable.flag_np, "NPR"),
            new Country("NR", "Nauru", "+674", R.drawable.flag_nr, "AUD"),
            new Country("NU", "Niue", "+683", R.drawable.flag_nu, "NZD"),
            new Country("NZ", "New Zealand", "+64", R.drawable.flag_nz, "NZD"),
            new Country("OM", "Oman", "+968", R.drawable.flag_om, "OMR"),
            new Country("PA", "Panama", "+507", R.drawable.flag_pa, "PAB"),
            new Country("PE", "Peru", "+51", R.drawable.flag_pe, "PEN"),
            new Country("PF", "French Polynesia", "+689", R.drawable.flag_pf, "XPF"),
            new Country("PG", "Papua New Guinea", "+675", R.drawable.flag_pg, "PGK"),
            new Country("PH", "Philippines", "+63", R.drawable.flag_ph, "PHP"),
            new Country("PK", "Pakistan", "+92", R.drawable.flag_pk, "PKR"),
            new Country("PL", "Poland", "+48", R.drawable.flag_pl, "PLN"),
            new Country("PM", "Saint Pierre and Miquelon", "+508", R.drawable.flag_pm, "EUR"),
            new Country("PN", "Pitcairn", "+64", R.drawable.flag_pn, "NZD"),
            new Country("PR", "Puerto Rico", "+1-787, +1-939", R.drawable.flag_pr, "USD"),
            new Country("PS", "Palestinian", "+970", R.drawable.flag_ps, "ILS"),
            new Country("PT", "Portugal", "+351", R.drawable.flag_pt, "EUR"),
            new Country("PW", "Palau", "+680", R.drawable.flag_pw, "USD"),
            new Country("PY", "Paraguay", "+595", R.drawable.flag_py, "PYG"),
            new Country("QA", "Qatar", "+974", R.drawable.flag_qa, "QAR"),
            new Country("RE", "Reunion", "+262", R.drawable.flag_re, "EUR"),
            new Country("RO", "Romania", "+40", R.drawable.flag_ro, "RON"),
            new Country("RS", "Serbia", "+381", R.drawable.flag_rs, "RSD"),
            new Country("RU", "Russia", "+7", R.drawable.flag_ru, "RUB"),
            new Country("RW", "Rwanda", "+250", R.drawable.flag_rw, "RWF"),
            new Country("SA", "Saudi Arabia", "+966", R.drawable.flag_sa, "SAR"),
            new Country("SB", "Solomon Islands", "+677", R.drawable.flag_sb, "SBD"),
            new Country("SC", "Seychelles", "+248", R.drawable.flag_sc, "SCR"),
            new Country("SD", "Sudan", "+249", R.drawable.flag_sd, "SDG"),
            new Country("SE", "Sweden", "+46", R.drawable.flag_se, "SEK"),
            new Country("SG", "Singapore", "+65", R.drawable.flag_sg, "SGD"),
            new Country("SH", "Saint Helena", "+290", R.drawable.flag_sh,
                    "SHP"),
            new Country("SI", "Slovenia", "+386", R.drawable.flag_si, "EUR"),
            new Country("SJ", "Svalbard and Jan Mayen", "+47", R.drawable.flag_sj, "NOK"),
            new Country("SK", "Slovakia", "+421", R.drawable.flag_sk, "EUR"),
            new Country("SL", "Sierra Leone", "+232", R.drawable.flag_sl, "SLL"),
            new Country("SM", "San Marino", "+378", R.drawable.flag_sm, "EUR"),
            new Country("SN", "Senegal", "+221", R.drawable.flag_sn, "XOF"),
            new Country("SO", "Somalia", "+252", R.drawable.flag_so, "SOS"),
            new Country("SR", "Suriname", "+597", R.drawable.flag_sr, "SRD"),
            new Country("SS", "South Sudan", "+211", R.drawable.flag_ss, "SSP"),
            new Country("ST", "Sao Tome and Principe", "+239", R.drawable.flag_st, "STD"),
            new Country("SV", "El Salvador", "+503", R.drawable.flag_sv, "SVC"),
            new Country("SX", "Sint Maarten", "+1-721", R.drawable.flag_sx, "ANG"),
            new Country("SY", "Syria", "+963", R.drawable.flag_sy, "SYP"),
            new Country("SZ", "Swaziland", "+268", R.drawable.flag_sz, "SZL"),
            new Country("TC", "Turks and Caicos Islands", "+1-649", R.drawable.flag_tc, "USD"),
            new Country("TD", "Chad", "+235", R.drawable.flag_td, "XAF"),
            new Country("TG", "Togo", "+228", R.drawable.flag_tg, "XOF"),
            new Country("TH", "Thailand", "+66", R.drawable.flag_th, "THB"),
            new Country("TJ", "Tajikistan", "+992", R.drawable.flag_tj, "TJS"),
            new Country("TK", "Tokelau", "+690", R.drawable.flag_tk, "NZD"),
            new Country("TL", "East Timor", "+670", R.drawable.flag_tl, "USD"),
            new Country("TM", "Turkmenistan", "+993", R.drawable.flag_tm, "TMT"),
            new Country("TN", "Tunisia", "+216", R.drawable.flag_tn, "TND"),
            new Country("TO", "Tonga", "+676", R.drawable.flag_to, "TOP"),
            new Country("TR", "Turkey", "+90", R.drawable.flag_tr, "TRY"),
            new Country("TT", "Trinidad and Tobago", "+1-868", R.drawable.flag_tt, "TTD"),
            new Country("TV", "Tuvalu", "+688", R.drawable.flag_tv, "AUD"),
            new Country("TW", "Taiwan", "+886", R.drawable.flag_tw, "TWD"),
            new Country("TZ", "Tanzania", "+255", R.drawable.flag_tz, "TZS"),
            new Country("UA", "Ukraine", "+380", R.drawable.flag_ua, "UAH"),
            new Country("UG", "Uganda", "+256", R.drawable.flag_ug, "UGX"),
            new Country("US", "United States", "+1", R.drawable.flag_us, "USD"),
            new Country("UY", "Uruguay", "+598", R.drawable.flag_uy, "UYU"),
            new Country("UZ", "Uzbekistan", "+998", R.drawable.flag_uz, "UZS"),
            new Country("VA", "Vatican", "+379", R.drawable.flag_va, "EUR"),
            new Country("VC", "Saint Vincent and the Grenadines", "+1-784", R.drawable.flag_vc, "XCD"),
            new Country("VE", "Venezuela", "+58", R.drawable.flag_ve, "VEF"),
            new Country("VG", "British Virgin Islands", "+1-284", R.drawable.flag_vg, "USD"),
            new Country("VI", "U.S. Virgin Islands", "+1-340", R.drawable.flag_vi, "USD"),
            new Country("VN", "Vietnam", "+84", R.drawable.flag_vn, "VND"),
            new Country("VU", "Vanuatu", "+678", R.drawable.flag_vu, "VUV"),
            new Country("WF", "Wallis and Futuna", "+681", R.drawable.flag_wf, "XPF"),
            new Country("WS", "Samoa", "+685", R.drawable.flag_ws, "WST"),
            new Country("XK", "Kosovo", "+383", R.drawable.flag_xk, "EUR"),
            new Country("YE", "Yemen", "+967", R.drawable.flag_ye, "YER"),
            new Country("YT", "Mayotte", "+262", R.drawable.flag_yt, "EUR"),
            new Country("ZA", "South Africa", "+27", R.drawable.flag_za, "ZAR"),
            new Country("ZM", "Zambia", "+260", R.drawable.flag_zm, "ZMW"),
            new Country("ZW", "Zimbabwe", "+263", R.drawable.flag_zw, "USD"),
    };
    // endregion

    // region Variables
    public static final int SORT_BY_NONE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_ISO = 2;
    public static final int SORT_BY_DIAL_CODE = 3;
    public static final int THEME_OLD = 1;
    public static final int THEME_NEW = 2;
    private int theme;

    private int style;
    private Context context;
    private int sortBy = SORT_BY_NAME;
    private OnCountryPickerListener onCountryPickerListener;
    private boolean canSearch = true;

    private List<Country> countries;
    private EditText searchEditText;
    private RecyclerView countriesRecyclerView;
    private LinearLayout rootView;
    private int textColor;
    private int hintColor;
    private int backgroundColor;
    private int searchIconId;
    private Drawable searchIcon;
    private CountriesAdapter adapter;
    private List<Country> searchResults;
    private BottomSheetDialogView bottomSheetDialog;
    private Dialog dialog;
    private Boolean showPreferenceCountries = false;

    // endregion

    // region Constructors
    private CountryPicker() {
    }


    CountryPicker(Builder builder) {
        sortBy = builder.sortBy;
        if (builder.onCountryPickerListener != null) {
            onCountryPickerListener = builder.onCountryPickerListener;
        }
        style = builder.style;
        context = builder.context;
        canSearch = builder.canSearch;
        theme = builder.theme;
        countries = new ArrayList<>(Arrays.asList(COUNTRIES));
        sortCountries(countries);
    }
    // endregion


    private Drawable getFlag(String phoneCode) {
        for (Country country : COUNTRIES) {
            if (country.getDialCode().equals(phoneCode))
                return ContextCompat.getDrawable(context, country.getFlag());
        }
        return null;
    }

    // region Listeners
    private void sortCountries(@NonNull List<Country> countries) {
        if (sortBy == SORT_BY_NAME) {
            Collections.sort(countries, new Comparator<Country>() {
                @Override
                public int compare(Country country1, Country country2) {
                    return country1.getName().trim().compareToIgnoreCase(country2.getName().trim());
                }
            });
        } else if (sortBy == SORT_BY_ISO) {
            Collections.sort(countries, new Comparator<Country>() {
                @Override
                public int compare(Country country1, Country country2) {
                    return country1.getCode().trim().compareToIgnoreCase(country2.getCode().trim());
                }
            });
        } else if (sortBy == SORT_BY_DIAL_CODE) {
            Collections.sort(countries, new Comparator<Country>() {
                @Override
                public int compare(Country country1, Country country2) {
                    return country1.getDialCode().trim().compareToIgnoreCase(country2.getDialCode().trim());
                }
            });
        }
    }
    // endregion

    // region Utility Methods
    public void showDialog(@NonNull AppCompatActivity activity, Integer style, Boolean showPreferenceCountries) {
        this.showPreferenceCountries = showPreferenceCountries;

        if (countries == null || countries.isEmpty()) {
            throw new IllegalArgumentException(context.getString(R.string.error_no_countries_found));
        } else {
            activity.getLifecycle().addObserver(this);
            dialog = new Dialog(activity, style);
            View dialogView = activity.getLayoutInflater().inflate(R.layout.country_picker, null);
            if (showPreferenceCountries) {
                setPreferenceCountries(dialogView);
            } else {
                RecyclerView recyclerView = dialogView.findViewById(R.id.rvPreferenceCountries);
                View dividerView = dialogView.findViewById(R.id.viewDivider);
                recyclerView.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
            }
            initiateUi(dialogView);
            setCustomStyle(dialogView);
            setSearchEditText(dialogView);
            setupRecyclerView(dialogView);
            dialog.setContentView(dialogView);
            if (dialog.getWindow() != null) {
//                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
//                params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
//                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//                dialog.getWindow().setAttributes(params);
                if (theme == THEME_NEW) {
                    Drawable background =
                            ContextCompat.getDrawable(context, R.drawable.ic_dialog_new_background);
                    if (background != null) {
                        background.setColorFilter(
                                new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP));
                    }
                    rootView.setBackground(background);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            }
            dialog.show();
        }
    }

    // region BottomSheet Methods
    public void showBottomSheet(AppCompatActivity activity) {
        if (countries == null || countries.isEmpty()) {
            throw new IllegalArgumentException(context.getString(R.string.error_no_countries_found));
        } else {
            activity.getLifecycle().addObserver(this);
            bottomSheetDialog = BottomSheetDialogView.newInstance(theme);
            bottomSheetDialog.setListener(this);
            bottomSheetDialog.show(activity.getSupportFragmentManager(), "bottomsheet");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void dismissDialogs() {
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
        }
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    public void setPreferenceCountries(View sheetView) {
        RecyclerView recyclerView = sheetView.findViewById(R.id.rvPreferenceCountries);
        View dividerView = sheetView.findViewById(R.id.viewDivider);
        if (getCountryByLocale(Locale.getDefault()) != null) {
            recyclerView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.VISIBLE);
            ArrayList<Country> preferenceList = new ArrayList<>();
            preferenceList.add(getCountryByLocale(Locale.getDefault()));
            recyclerView.setAdapter(new CountriesAdapter(context, preferenceList,
                    country -> {
                        if (onCountryPickerListener != null) {
                            onCountryPickerListener.onSelectCountry(country);
                            if (bottomSheetDialog != null) {
                                bottomSheetDialog.dismiss();
                            }
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            dialog = null;
                            bottomSheetDialog = null;
                            textColor = 0;
                            hintColor = 0;
                            backgroundColor = 0;
                            searchIconId = 0;
                            searchIcon = null;
                        }
                    },
                    textColor));
            recyclerView.setLayoutManager(new LinearLayoutManager((context)));
        } else {
            recyclerView.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setupRecyclerView(View sheetView) {
        searchResults = new ArrayList<>();
        searchResults.addAll(countries);
        sortCountries(searchResults);
        adapter = new CountriesAdapter(sheetView.getContext(), searchResults,
                country -> {
                    if (onCountryPickerListener != null) {
                        onCountryPickerListener.onSelectCountry(country);
                        if (bottomSheetDialog != null) {
                            bottomSheetDialog.dismiss();
                        }
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        dialog = null;
                        bottomSheetDialog = null;
                        textColor = 0;
                        hintColor = 0;
                        backgroundColor = 0;
                        searchIconId = 0;
                        searchIcon = null;
                    }
                },
                textColor);
        countriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(sheetView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        countriesRecyclerView.setLayoutManager(layoutManager);
        countriesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void setSearchEditText(View sheetView) {
        RecyclerView recyclerView = sheetView.findViewById(R.id.rvPreferenceCountries);
        View dividerView = sheetView.findViewById(R.id.viewDivider);
        if (canSearch) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Intentionally Empty
                    if (showPreferenceCountries) {
                        if (searchEditText.getText().toString().trim().length() == 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            dividerView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            dividerView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Intentionally Empty
                }

                @Override
                public void afterTextChanged(Editable searchQuery) {
                    search(searchQuery.toString());
                }
            });
            searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    InputMethodManager imm = (InputMethodManager) searchEditText.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    }
                    return true;
                }
            });
        } else {
            searchEditText.setVisibility(View.GONE);
        }
    }

    private void search(String searchQuery) {
        searchResults.clear();
        for (Country country : countries) {
            if (country.getName().toLowerCase(Locale.ENGLISH).contains(searchQuery.toLowerCase())) {
                searchResults.add(country);
            }
        }
        sortCountries(searchResults);
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void setCustomStyle(View sheetView) {
        if (style != 0) {
            int[] attrs =
                    {
                            android.R.attr.textColor, android.R.attr.textColorHint, android.R.attr.background,
                            android.R.attr.drawable
                    };
            TypedArray ta = sheetView.getContext().obtainStyledAttributes(style, attrs);
            textColor = ta.getColor(0, Color.BLACK);
            hintColor = ta.getColor(1, Color.GRAY);
            backgroundColor = ta.getColor(2, Color.WHITE);
            searchIconId = ta.getResourceId(3, R.drawable.ic_search);
//            searchEditText.setTextColor(textColor);
//            searchEditText.setHintTextColor(hintColor);
            searchIcon = ContextCompat.getDrawable(searchEditText.getContext(), searchIconId);
            if (searchIconId == R.drawable.ic_search) {
                searchIcon.setColorFilter(new PorterDuffColorFilter(hintColor, PorterDuff.Mode.SRC_ATOP));
            }
            searchEditText.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
            rootView.setBackgroundColor(backgroundColor);
            ta.recycle();
        }
    }

    @Override
    public void initiateUi(View sheetView) {
        searchEditText = sheetView.findViewById(R.id.country_code_picker_search);
        countriesRecyclerView = sheetView.findViewById(R.id.countries_recycler_view);
        rootView = sheetView.findViewById(R.id.rootView);
    }
    // endregion

    public Country getCountryFromSIM() {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null
                && telephonyManager.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
            return getCountryByISO(telephonyManager.getSimCountryIso());
        }
        return null;
    }

    public Country getCountryByLocale(@NonNull Locale locale) {
        String countryIsoCode = locale.getCountry();
        return getCountryByISO(countryIsoCode);
    }

    public Country getCountryByName(@NonNull String countryName) {
        Collections.sort(countries, new NameComparator());
        Country country = new Country();
        country.setName(countryName);
        int i = Collections.binarySearch(countries, country, new NameComparator());
        if (i < 0) {
            return null;
        } else {
            return countries.get(i);
        }
    }

    public Country getCountryByISO(@NonNull String countryIsoCode) {
        Collections.sort(countries, new ISOCodeComparator());
        Country country = new Country();
        country.setCode(countryIsoCode);
        int i = Collections.binarySearch(countries, country, new ISOCodeComparator());
        if (i < 0) {
            return null;
        } else {
            return countries.get(i);
        }
    }

    public Country getCountryByDialCode(@NonNull String dialCode) {
        Collections.sort(countries, new DialCodeComparator());
        Country country = new Country();
        country.setDialCode(dialCode);
        int i = Collections.binarySearch(countries, country, new DialCodeComparator());
        if (i < 0) {
            return null;
        } else {
            return countries.get(i);
        }
    }
    // endregion

    // region Builder
    public static class Builder {
        private Context context;
        private int sortBy = SORT_BY_NONE;
        private boolean canSearch = true;
        private OnCountryPickerListener onCountryPickerListener;
        private int style;
        private int theme = THEME_NEW;

        public Builder with(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public Builder style(@NonNull @StyleRes int style) {
            this.style = style;
            return this;
        }

        public Builder sortBy(@NonNull int sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder listener(@NonNull OnCountryPickerListener onCountryPickerListener) {
            this.onCountryPickerListener = onCountryPickerListener;
            return this;
        }

        public Builder canSearch(@NonNull boolean canSearch) {
            this.canSearch = canSearch;
            return this;
        }

        public Builder theme(@NonNull int theme) {
            this.theme = theme;
            return this;
        }

        public CountryPicker build() {
            return new CountryPicker(this);
        }
    }
    // endregion

    // region Comparators
    private static class ISOCodeComparator implements Comparator<Country> {
        @Override
        public int compare(Country country, Country nextCountry) {
            return country.getCode().compareToIgnoreCase(nextCountry.getCode());
        }
    }

    private static class NameComparator implements Comparator<Country> {
        @Override
        public int compare(Country country, Country nextCountry) {
            return country.getName().compareToIgnoreCase(nextCountry.getName());
        }
    }

    public static class DialCodeComparator implements Comparator<Country> {
        @Override
        public int compare(Country country, Country nextCountry) {
            return country.getDialCode().compareTo(nextCountry.getDialCode());
        }
    }
    // endregion

    public static Country getCountryName(String countryCode) {
        CountryPicker countryPicker = new CountryPicker();
        for (Country country : countryPicker.COUNTRIES) {
            if (Objects.equals(country.getCode(), countryCode)) {
                return country;
            }
        }
        return null;
    }
}
