package com.printer.sample;

import java.util.Vector;

import com.printer.aidl.PService;
import com.printer.command.EscCommand;
import com.printer.command.EscCommand.CODEPAGE;
import com.printer.command.EscCommand.ENABLE;
import com.printer.command.EscCommand.FONT;
import com.printer.command.EscCommand.HRI_POSITION;
import com.printer.command.EscCommand.JUSTIFICATION;
import com.printer.command.LabelCommand;
import com.printer.command.LabelCommand.BARCODETYPE;
import com.printer.command.LabelCommand.BITMAP_MODE;
import com.printer.command.LabelCommand.DIRECTION;
import com.printer.command.LabelCommand.EEC;
import com.printer.command.LabelCommand.FONTMUL;
import com.printer.command.LabelCommand.FONTTYPE;
import com.printer.command.LabelCommand.MIRROR;
import com.printer.command.LabelCommand.READABEL;
import com.printer.command.LabelCommand.RESPONSE_MODE;
import com.printer.command.LabelCommand.ROTATION;
import com.printer.command.PrinterCom;
import com.printer.command.PrinterUtils;
import com.printer.io.PrinterDevice;
import com.printer.service.PrinterPrintService;
import com.sample.R;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	private PService mPService = null;
	public static final String CONNECT_STATUS = "connect.status";
	private static final String DEBUG_TAG = "MainActivity";
	private PrinterServiceConnection conn = null;
	private int mPrinterIndex = 0;
	private int mTotalCopies = 0;
	private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;
	private static final int REQUEST_PRINT_LABEL = 0xfd;
	private static final int REQUEST_PRINT_RECEIPT = 0xfc;

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
						sendLabel();
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
				if (--mTotalCopies > 0) {
					sendReceiptWithResponse();
				}
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

				if (--mTotalCopies > 0 && d.charAt(1) == 0x00) {
					sendLabelWithResponse();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.e(DEBUG_TAG, "onCreate");
		connection();
		CheckBox checkbox = (CheckBox) findViewById(R.id.btCheckBox);
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				try {
					mPService.isUserExperience(isChecked);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

		});

		// 注册实时状态查询广播
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

	public void openPortDialogueClicked(View view) {
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

	public void printTestPageClicked(View view) {
		try {
			int rel = mPService.printeTestPage(mPrinterIndex); //
			Log.i("ServiceConnection", "rel " + rel);
			PrinterCom.ERROR_CODE r = PrinterCom.ERROR_CODE.values()[rel];
			if (r != PrinterCom.ERROR_CODE.SUCCESS) {
				Toast.makeText(getApplicationContext(), PrinterCom.getErrorText(r), Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	public void getPrinterStatusClicked(View view) {
		try {
			mTotalCopies = 0;
			mPService.queryPrinterStatus(mPrinterIndex, 500, MAIN_QUERY_PRINTER_STATUS);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void getPrinterCommandTypeClicked(View view) {
		try {
			int type = mPService.getPrinterCommandType(mPrinterIndex);
			if (type == PrinterCom.ESC_COMMAND) {
				Toast.makeText(getApplicationContext(), "打印机使用ESC命令", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "打印机使用TSC命令", Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void printArabicReceiptClicked(View view) {
		EscCommand esc = new EscCommand();
		// init printer
		esc.addInitializePrinter();
		// cancel Kanji
		esc.addCancelKanjiMode();
		// set paper width
		PrinterUtils.setPaperWidth(PrinterUtils.PAPER_58_WIDTH);
		// select codepage which is arabic
		esc.addSelectCodePage(CODEPAGE.ARABIC);
		esc.addArabicText("الهاتف المحمول معطوب و يحتاج إلى إصلاح");
		esc.addPrintAndFeedLines((byte) 5);
		Vector<Byte> datas = esc.getCommand(); // send data
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

	}

	void sendReceipt() {

		try {
			EscCommand esc = new EscCommand();
			esc.addInitializePrinter();
			esc.addPrintAndFeedLines((byte) 3);
			esc.addSelectJustification(JUSTIFICATION.CENTER);// 设置打印居中
			esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.ON, ENABLE.ON, ENABLE.OFF);// 设置为倍高倍宽
			//esc.addText("Denish\n","GB2312");
			esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_4, EscCommand.HEIGHT_ZOOM.MUL_4);
			esc.addText("HAHA");
			esc.addSetBarcodeHeight((byte) 30);// 打印文字
			esc.addPrintAndLineFeed();

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

			/*
			 * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
			 */
			/*esc.addText("Print QRcode\n"); // 打印文字
			esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); // 设置纠错等级
			esc.addSelectSizeOfModuleForQRCode((byte) 3);// 设置qrcode模块大小
			esc.addStoreQRCodeData("www.printer.cc");// 设置qrcode内容
			esc.addPrintQRCode();// 打印QRCode
			esc.addPrintAndLineFeed();*/

			/* 打印文字 */
			/*esc.addSelectJustification(JUSTIFICATION.CENTER);// 设置打印左对齐
			esc.addText("Completed!\r\n"); // 打印结束
			// 开钱箱
			esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
			esc.addPrintAndFeedLines((byte) 8);*/

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

	void sendReceiptWithResponse() {
		EscCommand esc = new EscCommand();
		esc.addInitializePrinter();
		esc.addPrintAndFeedLines((byte) 3);
		esc.addSelectJustification(JUSTIFICATION.CENTER);// 设置打印居中
		esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.ON, ENABLE.ON, ENABLE.OFF);// 设置为倍高倍宽
		esc.addText("Hello H1\n"); // 打印文字
		esc.addPrintAndLineFeed();

		/* 打印文字 */
		esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF, ENABLE.OFF, ENABLE.OFF, ENABLE.OFF);// 取消倍高倍宽
		esc.addSelectJustification(JUSTIFICATION.LEFT);// 设置打印左对齐
		esc.addText("Print text\n"); // 打印文字
		esc.addText("Welcome to use printer!\n"); // 打印文字

		/* 打印繁体中文 需要打印机支持繁体字库 */
		String message = "Hello\n";
		// esc.addText(message,"BIG5");
		esc.addText(message, "GB2312");
		esc.addPrintAndLineFeed();

		/* 绝对位置 具体详细信息请查看GP58编程手册 */
		esc.addText("Printer");
		esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
		esc.addSetAbsolutePrintPosition((short) 6);
		esc.addText("Printer");
		esc.addSetAbsolutePrintPosition((short) 10);
		esc.addText("Printer");
		esc.addPrintAndLineFeed();

		/* 打印图片 */
		// esc.addText("Print bitmap!\n"); // 打印文字
		// Bitmap b = BitmapFactory.decodeResource(getResources(),
		// R.drawable.gprinter);
		// esc.addRastBitImage(b, 384, 0); // 打印图片

		/* 打印一维条码 */
		esc.addText("Print code128\n"); // 打印文字
		esc.addSelectPrintingPositionForHRICharacters(HRI_POSITION.BELOW);//
		// 设置条码可识别字符位置在条码下方
		esc.addSetBarcodeHeight((byte) 60); // 设置条码高度为60点
		esc.addSetBarcodeWidth((byte) 1); // 设置条码单元宽度为1
		esc.addCODE128(esc.genCodeB("Printer")); // 打印Code128码
		esc.addPrintAndLineFeed();

		/*
		 * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
		 */
		esc.addText("Print QRcode\n"); // 打印文字
		esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); // 设置纠错等级
		esc.addSelectSizeOfModuleForQRCode((byte) 3);// 设置qrcode模块大小
		esc.addStoreQRCodeData("www.Printer.cc");// 设置qrcode内容
		esc.addPrintQRCode();// 打印QRCode
		esc.addPrintAndLineFeed();

		/* 打印文字 */
		esc.addSelectJustification(JUSTIFICATION.CENTER);// 设置打印左对齐
		esc.addText("Completed!\r\n"); // 打印结束
		// 开钱箱
		esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
		esc.addPrintAndFeedLines((byte) 8);

		// 加入查询打印机状态，打印完成后，此时会接收到PrinterCom.ACTION_DEVICE_STATUS广播
		esc.addQueryPrinterStatus();

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
	}

	void sendLabel() {
		LabelCommand tsc = new LabelCommand();
		tsc.addSize(60, 60); // 设置标签尺寸，按照实际尺寸设置
		tsc.addGap(0); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
		tsc.addDirection(DIRECTION.BACKWARD, MIRROR.NORMAL);// 设置打印方向
		tsc.addReference(0, 0);// 设置原点坐标
		tsc.addTear(ENABLE.ON); // 撕纸模式开启
		tsc.addCls();// 清除打印缓冲区
		// 绘制简体中文
		tsc.addText(20, 20, FONTTYPE.SIMPLIFIED_CHINESE, ROTATION.ROTATION_0, FONTMUL.MUL_1, FONTMUL.MUL_1,
				"Welcome to use printer!");
		// 绘制图片
		Bitmap b = BitmapFactory.decodeResource(getResources(), R.raw.hani);
		tsc.addBitmap(20, 50, BITMAP_MODE.OVERWRITE, b.getWidth(), b);

		tsc.addQRCode(250, 80, EEC.LEVEL_L, 5, ROTATION.ROTATION_0, " www.Printer.cc");
		// 绘制一维条码
		tsc.add1DBarcode(20, 250, BARCODETYPE.CODE128, 100, READABEL.EANBEL, ROTATION.ROTATION_0, "SMARNET");
		tsc.addPrint(1, 1); // 打印标签
		tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
		tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
		Vector<Byte> datas = tsc.getCommand(); // 发送数据
		byte[] bytes = PrinterUtils.ByteTo_byte(datas);
		String str = Base64.encodeToString(bytes, Base64.DEFAULT);
		int rel;
		try {
			rel = mPService.sendLabelCommand(mPrinterIndex, str);
			PrinterCom.ERROR_CODE r = PrinterCom.ERROR_CODE.values()[rel];
			if (r != PrinterCom.ERROR_CODE.SUCCESS) {
				Toast.makeText(getApplicationContext(), PrinterCom.getErrorText(r), Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	void sendLabelWithResponse() {
		LabelCommand tsc = new LabelCommand();
		tsc.addSize(60, 60); // 设置标签尺寸，按照实际尺寸设置
		tsc.addGap(0); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
		tsc.addDirection(DIRECTION.BACKWARD, MIRROR.NORMAL);// 设置打印方向
		tsc.addReference(0, 0);// 设置原点坐标
		tsc.addTear(ENABLE.ON); // 撕纸模式开启
		tsc.addCls();// 清除打印缓冲区
		// 绘制简体中文
		tsc.addText(20, 20, FONTTYPE.SIMPLIFIED_CHINESE, ROTATION.ROTATION_0, FONTMUL.MUL_1, FONTMUL.MUL_1,
				"Welcome to use printer!");
		// 绘制图片
		Bitmap b = BitmapFactory.decodeResource(getResources(), R.raw.hani);
		tsc.addBitmap(20, 50, BITMAP_MODE.OVERWRITE, b.getWidth(), b);

		tsc.addQRCode(250, 80, EEC.LEVEL_L, 5, ROTATION.ROTATION_0, " www.Printer.cc");
		// 绘制一维条码
		tsc.add1DBarcode(20, 250, BARCODETYPE.CODE128, 100, READABEL.EANBEL, ROTATION.ROTATION_0, "SMARNET");
		tsc.addPrint(1, 1); // 打印标签
		tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
		tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
		// 开启带Response的打印，用于连续打印
		tsc.addQueryPrinterStatus(RESPONSE_MODE.ON);

		Vector<Byte> datas = tsc.getCommand(); // 发送数据
		byte[] bytes = PrinterUtils.ByteTo_byte(datas);
		String str = Base64.encodeToString(bytes, Base64.DEFAULT);
		int rel;
		try {
			rel = mPService.sendLabelCommand(mPrinterIndex, str);
			PrinterCom.ERROR_CODE r = PrinterCom.ERROR_CODE.values()[rel];
			if (r != PrinterCom.ERROR_CODE.SUCCESS) {
				Toast.makeText(getApplicationContext(), PrinterCom.getErrorText(r), Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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

	public void printLabelClicked(View view) {
		try {
			int type = mPService.getPrinterCommandType(mPrinterIndex);
			if (type == PrinterCom.LABEL_COMMAND) {
				mPService.queryPrinterStatus(mPrinterIndex, 1000, REQUEST_PRINT_LABEL);
			} else {
				Toast.makeText(this, "Printer is not label mode", Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	public void printTestClicked(View view) {
		try {
			int type = mPService.getPrinterCommandType(mPrinterIndex);
			if (type == PrinterCom.ESC_COMMAND) {
				EditText etCopies = (EditText) findViewById(R.id.etPrintCopies);
				String str = etCopies.getText().toString();
				int copies = 0;
				if (!str.equals("")) {
					copies = Integer.parseInt(str);
					mTotalCopies = copies;
				}
				sendReceiptWithResponse();

			} else if (type == PrinterCom.LABEL_COMMAND) {
				EditText etCopies = (EditText) findViewById(R.id.etPrintCopies);
				String str = etCopies.getText().toString();
				int copies = 0;
				if (!str.equals("")) {
					copies = Integer.parseInt(str);
					mTotalCopies = copies;
				}
				sendLabelWithResponse();
			} else {
				Toast.makeText(this, "Printer is not receipt mode", Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	public void customerDisplayerClicked(View view) {
		Intent intent = new Intent(this, CustomerDiaplayActivity.class);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		Log.e(DEBUG_TAG, "onDestroy");
		super.onDestroy();
		if (conn != null) {
			unbindService(conn); // unBindService
		}
		unregisterReceiver(mBroadcastReceiver);
	}

}
