package com.satmatgroup.digiepay.recharge_services

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.satmatgroup.digiepay.R
import com.satmatgroup.digiepay.adapters_recyclerview.OperatorListAdapter
import com.satmatgroup.digiepay.model.OperatorsModel
import com.satmatgroup.digiepay.model.UserModel
import com.satmatgroup.digiepay.network_calls.AppApiCalls
import com.satmatgroup.digiepay.network_calls.AppApiUrl
import com.satmatgroup.digiepay.reports.recharge_reports.AllRechargeReportsActivity
import com.satmatgroup.digiepay.utils.AppCommonMethods
import com.satmatgroup.digiepay.utils.AppConstants
import com.satmatgroup.digiepay.utils.AppConstants.Companion.OPERATOR_DTH
import com.satmatgroup.digiepay.utils.AppConstants.Companion.TRUE
import com.satmatgroup.digiepay.utils.AppPrefs
import com.satmatgroup.digiepay.utils.toast
import kotlinx.android.synthetic.main.activity_dth_recharge.*
import kotlinx.android.synthetic.main.activity_dth_recharge.view.*
import kotlinx.android.synthetic.main.fragment_mobile_postpaid.view.*
import kotlinx.android.synthetic.main.fragment_mobile_prepaid.view.*
import kotlinx.android.synthetic.main.layout_dialog_confirmpin.*
import kotlinx.android.synthetic.main.layout_dialog_dthinfo.*
import kotlinx.android.synthetic.main.layout_list_bottomsheet.view.*
import org.json.JSONObject
import java.util.*

class DthRechargeActivity : AppCompatActivity(), AppApiCalls.OnAPICallCompleteListener,
    OperatorListAdapter.ListAdapterListener {

    lateinit var operatorAdapter: OperatorListAdapter
    var operatorsModelArrayList = ArrayList<OperatorsModel>()
    var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var userModel: UserModel

    var operator_code: String = ""
    lateinit var dialog: Dialog
    private val DTH_INFO: String = "DTH_INFO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.status_bar, this.theme)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        setContentView(R.layout.activity_dth_recharge)


        getOperatorApi(OPERATOR_DTH)
        custToolbar.ivBackBtn.setOnClickListener {
            onBackPressed()
        }
        initViews()


        tvChooseOperator.setOnClickListener {
            showOperatorsBottomSheet()

        }

        cvRechargeDthBtn.setOnClickListener {

            if (etDthNumber.text.isNullOrEmpty()) {

                etDthNumber.requestFocus()
                etDthNumber.error =
                    getString(R.string.error_invalid_dth)
                return@setOnClickListener

            } else if (etAmountDth.text.isNullOrEmpty()) {

                etAmountDth.requestFocus()
                etAmountDth.error =
                    getString(R.string.error_invalid_amount)
                return@setOnClickListener
            } else if (tvChooseOperator.text.isNullOrEmpty()) {

                tvChooseOperator.requestFocus()
                tvChooseOperator.error =
                    getString(R.string.error_select_operator)
                return@setOnClickListener
            } else {

                checkIfSameRecharge(
                    userModel.cus_mobile, etDthNumber.text.toString(),
                    etAmountDth.text.toString(), operator_code
                )


            }


        }

        cvBrowsePlans.setOnClickListener {
            if (etDthNumber.text.toString().isEmpty()) {

                etDthNumber.requestFocus()
                etDthNumber.setError("Invalid Dth")
            } else if (tvChooseOperator.text.toString().isEmpty()) {
                tvChooseOperator.requestFocus()
                tvChooseOperator.setError("Invalid Operator")

            } else {

                DthCusInfo(
                    etDthNumber.text.toString(),
                    tvChooseOperator.text.toString(),
                    AppPrefs.getStringPref("deviceId", this).toString(),
                    AppPrefs.getStringPref("deviceName", this).toString(),
                    userModel.cus_pin,
                    userModel.cus_pass,
                    userModel.cus_mobile,
                    userModel.cus_type
                )
            }
        }

    }

    fun initViews() {

        cvWalletBalanceDth.setBackgroundResource(R.drawable.bg_leftcurved)
        cvBrowsePlans.setBackgroundResource(R.drawable.bg_rightcurved)

        val gson = Gson()
        val json = AppPrefs.getStringPref(AppConstants.USER_MODEL, this)
        userModel = gson.fromJson(json, UserModel::class.java)
        getBalanceApi(userModel.cus_mobile)


    }


    //API CALL FUNCTION DEFINITION
    private fun getOperatorApi(
        operator_type: String
    ) {
        progress_bar.visibility = View.VISIBLE
        if (AppCommonMethods(this).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                this,
                AppConstants.OPERATOR_API,
                this
            )
            mAPIcall.getOperators(operator_type)

        } else {
            this.toast(getString(R.string.error_internet))
        }
    }

    private fun getBalanceApi(
        cus_id: String
    ) {
        progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(this).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                this,
                AppConstants.BALANCE_API,
                this
            )
            mAPIcall.getBalance(cus_id)

        } else {
            this.toast(getString(R.string.error_internet))
        }
    }

    private fun checkIfSameRecharge(
        cus_id: String,
        rec_mobile: String,
        amount: String,
        operator: String
    ) {
        progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(this).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                this,
                AppConstants.CHECK_SAME_RECHARGE_API,
                this
            )
            mAPIcall.checkIfSameRecharge(cus_id, rec_mobile, amount, operator)

        } else {
            this.toast(getString(R.string.error_internet))
        }
    }

    private fun verifyPin(
        cus_mobile: String,
        pin: String
    ) {
        progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(this).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                this,
                AppConstants.VERFY_PIN_API,
                this
            )
            mAPIcall.verifyPin(cus_mobile, pin)

        } else {
            this.toast(getString(R.string.error_internet))
        }
    }

    private fun rechargeApi(
        cus_id: String,
        rec_mobile: String,
        amount: String,
        operator: String,
        cus_type: String,
    ) {
        progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(this).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                this,
                AppConstants.RECHARGE_API,
                this
            )
            mAPIcall.rechargeApi(cus_id, rec_mobile, amount, operator, cus_type)

        } else {
            this.toast(getString(R.string.error_internet))
        }
    }

    override fun onAPICallCompleteListner(item: Any?, flag: String?, result: String) {
        if (flag.equals(AppConstants.OPERATOR_API)) {
            Log.e(AppConstants.OPERATOR_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            Log.e(AppConstants.STATUS, status)
            if (status.contains("true")) {
                progress_bar.visibility = View.GONE

                val cast = jsonObject.getJSONArray("result")

                for (i in 0 until cast.length()) {
                    val notifyObjJson = cast.getJSONObject(i)
                    val operatorname = notifyObjJson.getString("operatorname")
                    Log.e("operator_name ", operatorname)
                    val operatorsModel = Gson()
                        .fromJson(
                            notifyObjJson.toString(),
                            OperatorsModel::class.java
                        )

                    operatorsModelArrayList.add(operatorsModel)
                }


            } else {

                progress_bar.visibility = View.GONE


            }
        }
        if (flag.equals(AppConstants.BALANCE_API)) {
            progress_bar.visibility = View.GONE
            Log.e(AppConstants.BALANCE_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            val messageCode = jsonObject.getString(AppConstants.MESSAGE)

            //   val token = jsonObject.getString(AppConstants.TOKEN)
            Log.e(AppConstants.STATUS, status)
            Log.e(AppConstants.MESSAGE, messageCode)
            if (status.contains(AppConstants.TRUE)) {


                tvWalletBalance.text =
                    "${getString(R.string.Rupee)} ${jsonObject.getString(AppConstants.WALLETBALANCE)}"
                /* tvAepsBalance.text =
                     "${getString(R.string.Rupee)} ${jsonObject.getString(AEPSBALANCE)}"*/


            } else {
                progress_bar.visibility = View.GONE

                if (messageCode.equals(getString(R.string.error_expired_token))) {
                    AppCommonMethods.logoutOnExpiredDialog(this)
                } else {
                    this.toast(messageCode.trim())
                }
            }
        }
        if (flag.equals(AppConstants.CHECK_SAME_RECHARGE_API)) {
            Log.e(AppConstants.CHECK_SAME_RECHARGE_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            val message = jsonObject.getString(AppConstants.MESSAGE)
            Log.e(AppConstants.STATUS, status)
            if (status.contains("true")) {
                progress_bar.visibility = View.GONE
                verifyPin(userModel.cus_mobile, AppPrefs.getStringPref("AppPassword",this).toString())
            //confirmPinDialog()

            } else {

                progress_bar.visibility = View.GONE
                showMessageDialog(getString(R.string.error_attention), message)

            }
        }
        if (flag.equals(AppConstants.RECHARGE_API)) {
            Log.e(AppConstants.RECHARGE_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            Log.e(AppConstants.STATUS, status)
            if (status.contains(TRUE)) {
                progress_bar.visibility = View.GONE

                val resultObject = jsonObject.getJSONObject("result")
                val message = resultObject.getString(AppConstants.MESS)

                showSuccessOrFailedDialog(getString(R.string.status), message)


            } else {
                val resultObject = jsonObject.getJSONObject("result")
                val message = resultObject.getString(AppConstants.MESS)
                progress_bar.visibility = View.GONE
                showSuccessOrFailedDialog(getString(R.string.status), message)


            }
        }
        if (flag.equals(AppConstants.VERFY_PIN_API)) {
            Log.e(AppConstants.VERFY_PIN_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            val message = jsonObject.getString(AppConstants.MESSAGE)
            Log.e(AppConstants.STATUS, status)
            if (status.contains(TRUE)) {
                progress_bar.visibility = View.GONE
                rechargeApi(
                    userModel.cus_id, etDthNumber.text.toString(),
                    etAmountDth.text.toString(), operator_code, userModel.cus_type
                )

            } else {

                progress_bar.visibility = View.GONE
                showMessageDialog(getString(R.string.error_attention), message)

            }
        }
        if (flag.equals(DTH_INFO)) {
            Log.e("OFFERS", result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            Log.e(AppConstants.STATUS, status)
            //Log.e(AppConstants.MESSAGE_CODE, messageCode);
            if (status.contains("true")) {
                progress_bar.visibility = View.INVISIBLE

                val resultObject = jsonObject.getJSONObject("result")
                val cast = resultObject.getJSONArray("records")
                for (i in 0 until cast.length()) {
                    val notifyObjJson = cast.getJSONObject(i)


                    showDialogDth(
                        notifyObjJson.getString("customerName"),
                        notifyObjJson.getString("status"),
                        notifyObjJson.getString("planname"),
                        notifyObjJson.getString("Balance"),
                        notifyObjJson.getString("NextRechargeDate"),
                        notifyObjJson.getString("MonthlyRecharge")
                    )

                }
            } else {
                progress_bar.visibility = View.INVISIBLE

            }
        }



    }


    private fun showOperatorsBottomSheet() {
        val view: View = layoutInflater.inflate(R.layout.layout_list_bottomsheet, null)
        view.rvspinner.apply {

            layoutManager = LinearLayoutManager(context)
            view.rvspinner.addItemDecoration(DividerItemDecoration(this@DthRechargeActivity, LinearLayoutManager.VERTICAL))

            operatorAdapter = OperatorListAdapter(
                context, operatorsModelArrayList, this@DthRechargeActivity
            )
            view.rvspinner.adapter = operatorAdapter
        }

        bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetDialog!!.setContentView(view)
        val bottomSheetBehavior: BottomSheetBehavior<*> =
            BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.peekHeight = 600
        bottomSheetDialog!!.show()
    }

    override fun onClickAtOKButton(operatorsModel: OperatorsModel?) {
        if (operatorsModel != null) {
            tvChooseOperator.text = operatorsModel.operatorname


            operator_code = operatorsModel.opcodenew!!.trim()
//            opName = mobileRechargeModal.operatorname!!.trim()
            Glide.with(this)
                .load(AppApiUrl.IMAGE_URL + operatorsModel.operator_image)
                .into(ivOperatorImageDth)
            bottomSheetDialog!!.dismiss()
        }
    }


    private fun clearData() {

        etDthNumber.setText("")
        tvChooseOperator.setText("")
        ivOperatorImageDth.setImageDrawable(resources.getDrawable(R.drawable.icons_cellulartower))
        etAmountDth.setText("")


    }

    override fun onResume() {
        super.onResume()
        clearData()
    }

    private fun showMessageDialog(title: String, message: String) {
        val builder1 =
            AlertDialog.Builder(this)
        builder1.setTitle("" + title)
        builder1.setMessage("" + message)
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "OK"
        ) { dialog, id ->

            dialog.dismiss()
        }
        /*
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/
        val alert11 = builder1.create()
        alert11.show()
    }

    private fun showSuccessOrFailedDialog(title: String, message: String) {
        val builder1 =
            AlertDialog.Builder(this)
        builder1.setTitle("" + title)
        builder1.setMessage("" + message)
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "OK"
        ) { dialog, id ->

            clearData()
            val intent = Intent(this, AllRechargeReportsActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        /*
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/
        val alert11 = builder1.create()
        alert11.show()
    }

    fun confirmPinDialog() {
        dialog = Dialog(this, R.style.ThemeOverlay_MaterialComponents_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_dialog_confirmpin)

        dialog.etPin.requestFocus()
        dialog.tvDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.etPin.setText(AppPrefs.getStringPref("AppPassword",this).toString())

        dialog.getWindow()!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.tvConfirmPin.setOnClickListener {
            if (dialog.etPin.text.toString().isEmpty()) {
                dialog.etPin.requestFocus()
                dialog.etPin.setError("Please Enter Pin")
            } else {

                verifyPin(userModel.cus_mobile,AppPrefs.getStringPref("AppPassword",this).toString())
                dialog.dismiss()
            }

        }

        dialog.show()
    }

    private fun showDialogDth(
        custName: String?,
        custStatus: String?,
        custPlan: String?,
        custBalance: String?,
        custNxtRec: String?,
        custMonthly: String?
    ) {
        val dialog = Dialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_dialog_dthinfo)

        dialog.tvCusName.setText("Name : " + custName)
        dialog.tvCusStatus.setText("Status : " + custStatus)
        dialog.tvCustBalance.setText("Balance : " + resources.getString(R.string.Rupee) + custBalance)
        dialog.tvCustMonthlyRecharge.setText("Monthly Rec : " + custMonthly)
        dialog.tvCustPlan.setText("Plan : " + custPlan)
        dialog.tvCustNxtRec.setText("Next Recharge : " + custNxtRec)


        dialog.ivCloseTab.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun DthCusInfo(
        mobile: String,
        operator: String,
        deviceId: String,
        deviceName: String,
        pin: String,
        pass: String,
        cus_mobile: String,
        cus_type: String
    ) {
        progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(this).isNetworkAvailable) {
            val mAPIcall =
                AppApiCalls(this, DTH_INFO, this)
            mAPIcall.dthInfo(
                mobile, operator, deviceId, deviceName, pin,
                pass, cus_mobile, cus_type
            )
        } else {

            Toast.makeText(this, "Internet Error", Toast.LENGTH_SHORT).show()
        }
    }

}