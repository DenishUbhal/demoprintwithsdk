package com.demoprintwithsdk;

import com.printer.aidl.*;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.Vector;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

import com.printer.aidl.PService;
import com.printer.command.EscCommand;
import com.printer.command.LabelCommand;
import com.printer.command.PrinterCom;
import com.printer.command.PrinterUtils;
import com.printer.io.PrinterDevice;
import com.printer.service.PrinterPrintService;

public class MainActivity extends FlutterActivity {

    private static final String DEBUG_TAG = "MainActivity";
    private PService mPService = null;
    private PrinterServiceConnection conn = null;
    public static final String CONNECT_STATUS = "connect.status";
    private int mPrinterIndex = 0;
    private static final String CHANNEL = "test_activity";
    private static final String CHANNEL2 = "printScan";
    private static final int REQUEST_PRINT_RECEIPT = 0xfc;
    private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;
    private static final int REQUEST_PRINT_LABEL = 0xfd;


    protected static final String TAG = "TAG";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    String printText = "",chapterName="",categoryName="",inviteBy="",mobileno="";
    private String mDeviceAddress = "";



    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("ServiceConnection", "onServiceDisconnected() called");
            mPService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPService = PService.Stub.asInterface(service);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e(DEBUG_TAG, "onResume");
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("TAG", action);
            // PrinterCom.ACTION_DEVICE_REAL_STATUS 为广播的IntentFilter
            if (action.equals(PrinterCom.ACTION_DEVICE_REAL_STATUS)) {

                // 业务逻辑的请求码，对应哪里查询做什么操作
                int requestCode = intent.getIntExtra(PrinterCom.EXTRA_PRINTER_REQUEST_CODE, -1);
                // 判断请求码，是则进行业务操作
                if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

                    int status = intent.getIntExtra(PrinterCom.EXTRA_PRINTER_REAL_STATUS, 16);
                    String str;
                    if (status == PrinterCom.STATE_NO_ERR) {
                        str = "Hello";
                    } else {
                        str = "Hello";
                        if ((byte) (status & PrinterCom.STATE_OFFLINE) > 0) {
                            str += "Hello";
                        }
                        if ((byte) (status & PrinterCom.STATE_PAPER_ERR) > 0) {
                            str += "Hello";
                        }
                        if ((byte) (status & PrinterCom.STATE_COVER_OPEN) > 0) {
                            str += "Hello";
                        }
                        if ((byte) (status & PrinterCom.STATE_ERR_OCCURS) > 0) {
                            str += "Hello";
                        }
                        if ((byte) (status & PrinterCom.STATE_TIMES_OUT) > 0) {
                            str += "Hello";
                        }
                    }

                    Toast.makeText(getApplicationContext(), "Hello：" + mPrinterIndex + " Hello：" + str, Toast.LENGTH_SHORT)
                            .show();
                } else if (requestCode == REQUEST_PRINT_LABEL) {
                    int status = intent.getIntExtra(PrinterCom.EXTRA_PRINTER_REAL_STATUS, 16);
                    if (status == PrinterCom.STATE_NO_ERR) {
                        //sendLabel();
                    } else {
                        Toast.makeText(MainActivity.this, "query printer status error", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == REQUEST_PRINT_RECEIPT) {
                    int status = intent.getIntExtra(PrinterCom.EXTRA_PRINTER_REAL_STATUS, 16);
                    if (status == PrinterCom.STATE_NO_ERR) {
                        sendReceipt();
                    } else {
                        Toast.makeText(MainActivity.this, "query printer status error", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (action.equals(PrinterCom.ACTION_RECEIPT_RESPONSE)) {
                /*if (--mTotalCopies > 0) {
                    sendReceiptWithResponse();
                }*/
            } else if (action.equals(PrinterCom.ACTION_LABEL_RESPONSE)) {
                byte[] data = intent.getByteArrayExtra(PrinterCom.EXTRA_PRINTER_LABEL_RESPONSE);
                int cnt = intent.getIntExtra(PrinterCom.EXTRA_PRINTER_LABEL_RESPONSE_CNT, 1);
                String d = new String(data, 0, cnt);
                /**
                 * 这里的d的内容根据RESPONSE_MODE去判断返回的内容去判断是否成功，具体可以查看标签编程手册SET
                 * RESPONSE指令
                 * 该sample中实现的是发一张就返回一次,这里返回的是{00,00001}。这里的对应{Status,######,ID}
                 * 所以我们需要取出STATUS
                 */
                Log.d("LABEL RESPONSE", d);

                /*if (--mTotalCopies > 0 && d.charAt(1) == 0x00) {
                    sendLabelWithResponse();
                }*/
            }
        }
    };
    void sendReceipt() {

        try {
            EscCommand esc = new EscCommand();
            esc.addInitializePrinter();

            /*esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);// 设置打印居中
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽
            //esc.addText("Denish\n","GB2312");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            //esc.addText(" BNI "+chapterName+"\n");
            esc.addText(" BNI "+chapterName);
            //esc.addSetBarcodeHeight((byte) 30);// 打印文字
            esc.addPrintAndLineFeed();


            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印居中
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽

            if (printText.length()<=8){
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
                esc.addText(printText+"\n");
                //esc.addSetBarcodeHeight((byte) 30);// 打印文字
            }else if (printText.length()<=15){
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
                esc.addText(printText+"\n");
            }else{
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
                esc.addText(printText);
            }
            esc.addPrintAndLineFeed();

            //print category Name
            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);// 设置打印居中
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽
            //esc.addText("Denish\n","GB2312");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            esc.addText(" Category   : "+categoryName);
            //esc.addSetBarcodeHeight((byte) 30);// 打印文字
            esc.addPrintAndLineFeed();

            esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);// 设置打印居中
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽
            //esc.addText("Denish\n","GB2312");
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);

            if (printText.length()<=8){
                esc.addText(" Invited By : "+inviteBy+"\n\n\n");
            }else if (printText.length()<=15){
                esc.addText(" Invited By : "+inviteBy+"\n\n");
            }else{
                esc.addText(" Invited By : "+inviteBy+"\n\n");
            }
            //print Invite By
            //esc.addSetBarcodeHeight((byte) 30);// 打印文字
            esc.addPrintAndLineFeed();*/




            /* 打印文字 */
			/*esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.OFF, ENABLE.OFF, ENABLE.OFF);// 取消倍高倍宽
			esc.addSelectJustification(JUSTIFICATION.LEFT);// 设置打印左对齐
			esc.addText("Print text\n"); // 打印文字
			esc.addText("Welcome to use  printer!\n"); // 打印文字*/

            /* 打印繁体中文 需要打印机支持繁体字库 */
			/*String message = "票據打印機\n";
			// esc.addText(message,"BIG5");
			esc.addText(message, "GB2312");
			esc.addPrintAndLineFeed();*/

            /* 绝对位置 具体详细信息请查看GP58编程手册 */
			/*esc.addText("Printer");
			esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
			esc.addSetAbsolutePrintPosition((short) 6);
			esc.addText("Printer");
			esc.addSetAbsolutePrintPosition((short) 10);
			esc.addText("Printer");
			esc.addPrintAndLineFeed();*/

            /* 打印图片 */
			/*esc.addText("Print bitmap!\n"); // 打印文字
			Bitmap b = BitmapFactory.decodeResource(getResources(), R.raw.hani);
			esc.addRastBitImage(b, 384, 0); // 打印图片*/

            /* 打印一维条码 */
			/*esc.addText("Print code128\n"); // 打印文字
			esc.addSelectPrintingPositionForHRICharacters(HRI_POSITION.BELOW);//
			// 设置条码可识别字符位置在条码下方
			esc.addSetBarcodeHeight((byte) 60); // 设置条码高度为60点
			esc.addSetBarcodeWidth((byte) 1); // 设置条码单元宽度为1
			esc.addCODE128(esc.genCodeB("Printer")); // 打印Code128码
			esc.addPrintAndLineFeed();*/

			//denish qork qrcode start here
            String BarcodeData=printText+","+mobileno;
             //* QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
            esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印左对齐
            esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽
			//esc.addText("Arpit Shah\n"); // 打印文字
            esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
            if (printText.length()<=13){
                esc.addText(printText+"\n");
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
            }else{
                esc.addText(printText+"\n");
                esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
            }
            //esc.addSetBarcodeHeight((byte) 30);
			esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); // 设置纠错等级
			esc.addSelectSizeOfModuleForQRCode((byte) 5);// 设置qrcode模块大小
			esc.addStoreQRCodeData(BarcodeData);// 设置qrcode内容
			esc.addPrintQRCode();// 打印QRCode
            //Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.katimg);
            /*esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印左对齐
            Bitmap bitmap1= BitmapFactory.decodeResource(getResources(), R.drawable.yash);
            esc.addRastBitImageWithMethod(bitmap1,100,1,1);*/
			//esc.addPrintAndLineFeed();

             //打印文字

			//esc.addText("Completed!\r\n"); // 打印结束
			// 开钱箱
			esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
			esc.addPrintAndFeedLines((byte) 3);

			//denish qork qrcode end here

            Vector<Byte> datas = esc.getCommand(); // 发送数据
            byte[] bytes = PrinterUtils.ByteTo_byte(datas);
            String sss = Base64.encodeToString(bytes, Base64.DEFAULT);
            int rs;
            try {
                rs = mPService.sendEscCommand(mPrinterIndex, sss);
                PrinterCom.ERROR_CODE r = PrinterCom.ERROR_CODE.values()[rs];
                if (r != PrinterCom.ERROR_CODE.SUCCESS) {
                    Toast.makeText(getApplicationContext(), PrinterCom.getErrorText(r), Toast.LENGTH_SHORT).show();
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);

        connection();
        new MethodChannel(getFlutterView(), CHANNEL).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                        if (call.method.equals("startNewActivity")) {
                            /*try {
                                mPService.isUserExperience(false);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }*/

                            openPortDialogueClicked();
                        }
                    }
                });

        new MethodChannel(getFlutterView(), CHANNEL2).setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                if (methodCall.method.equals("printName")){
                    printText=methodCall.argument("text");
                    chapterName=methodCall.argument("chapter");
                    categoryName=methodCall.argument("categoryName");
                    inviteBy=methodCall.argument("InviteBy");
                    inviteBy=methodCall.argument("InviteBy");
                    mobileno=methodCall.argument("mobileno");
                    sendReceipt();
                    result.success(mDeviceAddress);
                }
            }
        });

        registerReceiver(mBroadcastReceiver, new IntentFilter(PrinterCom.ACTION_DEVICE_REAL_STATUS));
        /**
         * 票据模式下，可注册该广播，在需要打印内容的最后加入addQueryPrinterStatus()，在打印完成后会接收到
         * action为PrinterCom.ACTION_DEVICE_STATUS的广播，特别用于连续打印，
         * 可参照该sample中的sendReceiptWithResponse方法与广播中的处理
         **/
        registerReceiver(mBroadcastReceiver, new IntentFilter(PrinterCom.ACTION_RECEIPT_RESPONSE));
        /**
         * 标签模式下，可注册该广播，在需要打印内容的最后加入addQueryPrinterStatus(RESPONSE_MODE mode)
         * ，在打印完成后会接收到，action为PrinterCom.ACTION_LABEL_RESPONSE的广播，特别用于连续打印，
         * 可参照该sample中的sendLabelWithResponse方法与广播中的处理
         **/
        registerReceiver(mBroadcastReceiver, new IntentFilter(PrinterCom.ACTION_LABEL_RESPONSE));

    }
    private void connection() {
        conn = new PrinterServiceConnection();
        Intent intent = new Intent(this, PrinterPrintService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
    }

    public void openPortDialogueClicked() {
        if (mPService == null) {
            Toast.makeText(this, "Print Service is not start, please check it", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(DEBUG_TAG, "openPortConfigurationDialog ");
        Intent intent = new Intent(this, PrinterConnectDialog.class);
        boolean[] state = getConnectState();
        intent.putExtra(CONNECT_STATUS, state);
        this.startActivity(intent);
    }


    public boolean[] getConnectState() {
        boolean[] state = new boolean[PrinterPrintService.MAX_PRINTER_CNT];
        for (int i = 0; i < PrinterPrintService.MAX_PRINTER_CNT; i++) {
            state[i] = false;
        }
        for (int i = 0; i < PrinterPrintService.MAX_PRINTER_CNT; i++) {
            try {
                if (mPService.getPrinterConnectStatus(i) == PrinterDevice.STATE_CONNECTED) {
                    state[i] = true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return state;
    }

    public void printReceiptClicked(View view) {
        try {
            int type = mPService.getPrinterCommandType(mPrinterIndex);
            if (type == PrinterCom.ESC_COMMAND) {
                mPService.queryPrinterStatus(mPrinterIndex, 1000, REQUEST_PRINT_RECEIPT);
                //mPService.sendEscCommand(mPrinterIndex,"Hello");
            } else {
                Toast.makeText(this, "Printer is not receipt mode", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }
}
