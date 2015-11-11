/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.sinpo.xnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.sinpo.xnfc.mail.EmailService;
import com.sinpo.xnfc.model.TagModel;
import com.sinpo.xnfc.nfc.NfcManager;
import com.sinpo.xnfc.nfc.ShellUtils;
import com.sinpo.xnfc.nfc.Util;
import com.sinpo.xnfc.ui.AboutPage;
import com.sinpo.xnfc.ui.MainPage;
import com.sinpo.xnfc.ui.NfcPage;
import com.sinpo.xnfc.ui.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.initDownPath(HetDoorActivity.Root);
		setContentView(R.layout.activity_main);
		//当前应用的代码执行目录
//		Util.upgradeRootPermission(getPackageCodePath());

		if(ShellUtils.checkRootPermission()){
			Toast.makeText(this,"root成功..",Toast.LENGTH_SHORT).show();
		}

		initViews();

		nfc = new NfcManager(this);

		onNewIntent(getIntent());
	}

	@Override
	public void onBackPressed() {
		if (isCurrentPage(SPEC.PAGE.ABOUT))
			loadDefaultPage();
		else if (safeExit)
			super.onBackPressed();
	}

	@Override
	public void setIntent(Intent intent) {
		if (NfcPage.isSendByMe(intent))
			loadNfcPage(intent);
		else if (AboutPage.isSendByMe(intent))
			loadAboutPage();
		else
			super.setIntent(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		nfc.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		nfc.onResume();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			if (nfc.updateStatus())
				loadDefaultPage();

			// 有些ROM将关闭系统状态下拉面板的BACK事件发给最顶层窗口
			// 这里加入一个延迟避免意外退出
			board.postDelayed(new Runnable() {
				public void run() {
					safeExit = true;
				}
			}, 800);
		} else {
			safeExit = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadDefaultPage();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		System.out.println("this is card..." + intent.toString());
		if (!nfc.readCard(intent, new NfcPage(this)))
			loadDefaultPage();
	}

	public void onSwitch2DefaultPage(View view) {
		if (!isCurrentPage(SPEC.PAGE.DEFAULT))
			loadDefaultPage();
	}

	public void onSwitch2AboutPage(View view) {
		TextView tv = (TextView) view;
		String msg = tv.getText().toString();
		System.out.println(msg);
		if (msg.contains("NFCard")){
			if (!isCurrentPage(SPEC.PAGE.ABOUT))
				loadAboutPage();
		}else{
			Intent ret = new Intent();
			ret.setClass(this,HetDoorActivity.class);
			ret.putExtra("TAGID", tagid);
			startActivity(ret);
		}
	}

	public void onSwitch2Card(View view){
		Intent ret = new Intent();
		ret.setClass(this,HetDoorActivity.class);
		ret.putExtra("TAGID", tagid);
		startActivity(ret);
	}


	public void onCopyPageContent(View view) {
		toolbar.copyPageContent(getFrontPage());
	}

	public void onSharePageContent(View view) {
		toolbar.sharePageContent(getFrontPage());
	}

	private void loadDefaultPage() {
		toolbar.show(null);
		setDefautUI();
		TextView ta = getBackPage();

		resetTextArea(ta, SPEC.PAGE.DEFAULT, Gravity.CENTER);
		ta.setText(MainPage.getContent(this));

		board.showNext();
	}

	private void loadAboutPage() {
		toolbar.show(R.id.btnBack);

		TextView ta = getBackPage();

		resetTextArea(ta, SPEC.PAGE.ABOUT, Gravity.LEFT);
		ta.setText(AboutPage.getContent(this));

		board.showNext();
	}

	private void loadNfcPage(Intent intent) {
		final CharSequence info = NfcPage.getContent(this, intent);
		tagid = intent.getStringExtra("TAGID");
		tagid = initTagId(tagid);
		TextView ta = getBackPage();

		if (NfcPage.isNormalInfo(intent)) {
			toolbar.show(R.id.btnCopy, R.id.btnShare, R.id.btnReset);
			resetTextArea(ta, SPEC.PAGE.INFO, Gravity.LEFT);
		} else {
			setHetUI();
			toolbar.show(R.id.btnBack);
			resetTextArea(ta, SPEC.PAGE.INFO, Gravity.CENTER);
		}

		ta.setText(info);

		board.showNext();
	}

	private String initTagId(String tagid){
		if (tagid == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for (int j = 0;j < tagid.length()/2; j++){
			sb.append(tagid.substring(j*2, j*2 + 2));
			if(j < tagid.length() / 2 -1 ){
				sb.append(":");
			}
		}
		tagid = sb.toString();
		System.out.println("aaaaaaaaaaa"+sb.toString());
		onSavaTagId(tagid);
		return tagid;
	}

	public void onSavaTagId(String tagid){
		try {
			if (!read(tagid)) {
				TagModel tagModel = new TagModel();
				tagModel.setName("H&T");
				tagModel.setSave("false");
				tagModel.setTag(tagid);
				tagModel.setTime(Util.getTime());
				data.add(tagModel);
				Util.writeFileSdcardFile(HetDoorActivity.tagIdFilePath, tagModel.toString(), true);
			}
			saveAndSendEmail(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveAndSendEmail(List<TagModel> nListDta){
		if (nListDta != null && nListDta.size() > 0){
			boolean sendEmail = false;
			StringBuffer sb = new StringBuffer();
			for (TagModel item : nListDta){
				sb.append(item.toString());
//				sb.append(item.getName());
//				sb.append("=");
//				sb.append(item.getTag());
//				sb.append("=");
//				sb.append(item.isSave());
//				sb.append("=");
//				sb.append(item.getTime());
//				sb.append("\n");
				if (item.isSave().equals("false")){
					sendEmail = true;
				}
			}
			if (sendEmail){
				sendEmail(sb.toString());
			}
		}
	}

	private void sendEmail(String tagIdStr){
		Intent mm = new Intent(this,EmailService.class);
		mm.putExtra(EmailService.HETID, tagIdStr);
		startService(mm);
	}


	List<TagModel> data = new ArrayList<TagModel>();
	private boolean read(String tagid){
		data.clear();
		boolean find = false;
		try {
			String readStr = Util.readFileSdcardFile(HetDoorActivity.tagIdFilePath);
			if (readStr == null && "".equals(readStr)){
				return false;
			}
			final String[] lines = readStr.split("\n");
			for (String item : lines){
				if (item != null) {
					String[] tags = item.split("=");
					if (item != null && item.length() >= 4) {
						TagModel tagModel = new TagModel();
						tagModel.setName(tags[0]);
						tagModel.setTag(tags[1]);
						tagModel.setSave(tags[2]);
						tagModel.setTime(tags[3]);
						data.add(tagModel);
						if (tagid.equalsIgnoreCase(tags[1])){
							find = true;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return find;
	}

	private void setHetUI(){
		TextView txtName = (TextView) findViewById(R.id.txtAppName);
		txtName.setText("门禁卡");
	}

	private void setDefautUI(){
		TextView txtName = (TextView) findViewById(R.id.txtAppName);
		txtName.setText("NFCard");
	}

	private boolean isCurrentPage(SPEC.PAGE which) {
		Object obj = getFrontPage().getTag();

		if (obj == null)
			return which.equals(SPEC.PAGE.DEFAULT);

		return which.equals(obj);
	}

	private void resetTextArea(TextView textArea, SPEC.PAGE type, int gravity) {

		((View) textArea.getParent()).scrollTo(0, 0);

		textArea.setTag(type);
		textArea.setGravity(gravity);
	}

	private TextView getFrontPage() {
		return (TextView) ((ViewGroup) board.getCurrentView()).getChildAt(0);
	}

	private TextView getBackPage() {
		return (TextView) ((ViewGroup) board.getNextView()).getChildAt(0);
	}

	private void initViews() {
		board = (ViewSwitcher) findViewById(R.id.switcher);

		Typeface tf = ThisApplication.getFontResource(R.string.font_oem1);
		txtAppName = (TextView) findViewById(R.id.txtAppName);
		txtAppName.setTypeface(tf);

		tf = ThisApplication.getFontResource(R.string.font_oem2);

		txtAppName = getFrontPage();
		txtAppName.setMovementMethod(LinkMovementMethod.getInstance());
		txtAppName.setTypeface(tf);

		txtAppName = getBackPage();
		txtAppName.setMovementMethod(LinkMovementMethod.getInstance());
		txtAppName.setTypeface(tf);

		toolbar = new Toolbar((ViewGroup) findViewById(R.id.toolbar));
	}

	private ViewSwitcher board;
	private Toolbar toolbar;
	private NfcManager nfc;
	private boolean safeExit;

	private TextView txtAppName;
	private String tagid;
}
