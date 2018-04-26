package com.example.brmcmanus.paypalsdkintegration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ShippingAddress;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    //Declare a progressbar
    private ProgressBar spinner;

    //Create a PayPalConfiguration object
    private static PayPalConfiguration config = new PayPalConfiguration()

            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AcnHhNKHn0m8RtnZF2oQ0f8-S6iLSNgJwRijnKKagRbVOQbiLSMXe9HO_GCBa3nkpsj4o4R9xErBrGFc");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        //Initalize Progressbar for when user is waiting for response
        spinner = (ProgressBar)findViewById(R.id.progressBar1);

        //TextView for label
        TextView tv = findViewById(R.id.payLbl);

        //Button to get client token and do transaction
        Button btnPay = findViewById(R.id.btnPay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);
                onBuyPressed();
            }
        });

        //Ensure the spinner is initially set to invisible
        spinner.setVisibility(View.GONE);

        //Start PayPalService when your activity is created
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

    }

    @Override
    public void onDestroy() {

        //Remove the Progressbar
        spinner.setVisibility(View.GONE);

        //Stop PayPalService when your activity is destroyed
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    //Create the payment and launch the payment intent
    public void onBuyPressed() {

        // PAYMENT_INTENT_SALE will cause the payment to complete immediately.
        // Change PAYMENT_INTENT_SALE to
        //   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
        //   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
        //     later via calls from your server.

        PayPalPayment payment = new PayPalPayment(new BigDecimal("10.00"), "GBP", "sample item",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Optional extra methods we can call to get shipping address

        //Get the address from paypal
        //enableShippingAddressRetrieval(payment, true);

        //Get the address from the android app
        //addAppProvidedShippingAddress(payment);

        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, 0);
    }


    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    spinner.setVisibility(View.GONE);
                    Log.i("paymentExample", confirm.toJSONObject().toString(4));
                    //Toast.makeText(getApplicationContext(),confirm.toJSONObject().toString() , Toast.LENGTH_LONG).show();

                    JSONObject result = confirm.toJSONObject();
                    JSONObject resObj = result.getJSONObject("response");
                    String res = resObj.get("state").toString();
                    //Toast.makeText(getApplicationContext(),res, Toast.LENGTH_LONG).show();

                    //Ensure the spinner is initially set to invisible
                    spinner.setVisibility(View.GONE);

                    if(res.equals("approved"))
                    {
                        Toast.makeText(getApplicationContext(),"Payment was successful", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Payment failed", Toast.LENGTH_LONG).show();
                    }
                    // TODO: send 'confirm' to your server for verification.
                    // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                    // for more details.

                } catch (JSONException e) {
                    spinner.setVisibility(View.GONE);
                    Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    Toast.makeText(getApplicationContext(),"An extremely unlikely failure occurred: " + e , Toast.LENGTH_LONG).show();
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            spinner.setVisibility(View.GONE);
            Log.i("paymentExample", "The user canceled.");
            Toast.makeText(getApplicationContext(),"The user canceled." , Toast.LENGTH_LONG).show();
        }
        else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            spinner.setVisibility(View.GONE);
            Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            Toast.makeText(getApplicationContext(),"An invalid Payment or PayPalConfiguration was submitted. Please see the docs." , Toast.LENGTH_LONG).show();
        }
    }


    /*
     * Add app-provided shipping address to payment
     */
    private void addAppProvidedShippingAddress(PayPalPayment paypalPayment) {
        ShippingAddress shippingAddress =
                new ShippingAddress().recipientName("Prime minister").line1("10 Downing Street")
                        .city("London").state("London").postalCode("SW1A2AA").countryCode("GB");
        paypalPayment.providedShippingAddress(shippingAddress);
    }

    /*
     * Enable retrieval of shipping addresses from buyer's PayPal account
     */
    private void enableShippingAddressRetrieval(PayPalPayment paypalPayment, boolean enable) {
        paypalPayment.enablePayPalShippingAddressesRetrieval(enable);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
